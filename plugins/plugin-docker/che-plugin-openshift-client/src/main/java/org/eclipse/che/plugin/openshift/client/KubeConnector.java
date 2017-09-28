/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.openshift.client;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.ImageStreamTag;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.DockerApiVersionPathPrefixProvider;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.HostConfig;
import org.eclipse.che.plugin.docker.client.json.ImageConfig;
import org.eclipse.che.plugin.docker.client.json.ImageInfo;
import org.eclipse.che.plugin.docker.client.json.NetworkSettings;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.eclipse.che.plugin.docker.client.json.network.EndpointConfig;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.InspectImageParams;
import org.eclipse.che.plugin.docker.client.params.PullParams;
import org.eclipse.che.plugin.docker.client.params.TagParams;
import org.eclipse.che.plugin.openshift.client.exception.OpenShiftException;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesContainer;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesEnvVar;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesLabelConverter;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesService;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Client for OpenShift API.
 *
 * @author Mario Loriedo (mloriedo@redhat.com)
 */
@Singleton
public class KubeConnector extends OpenShiftConnector {
  private static final Logger LOG = LoggerFactory.getLogger(KubeConnector.class);
  private static final int OPENSHIFT_IMAGESTREAM_WAIT_DELAY = 2000;
  private static final int OPENSHIFT_IMAGESTREAM_MAX_WAIT_COUNT = 30;

  private final String openShiftCheProjectName;
  private final int openShiftLivenessProbeDelay;
  private final int openShiftLivenessProbeTimeout;
  private final String workspacesPersistentVolumeClaim;
  private final String workspacesPvcQuantity;
  private final String cheWorkspaceStorage;
  private final String cheWorkspaceProjectsStorage;
  private final String cheServerExternalAddress;
  private final String cheWorkspaceMemoryLimit;
  private final String cheWorkspaceMemoryRequest;
  private final boolean secureRoutes;
  private final boolean createWorkspaceDirs;
  private final OpenShiftPvcHelper openShiftPvcHelper;
  private final OpenShiftRouteCreator openShiftRouteCreator;
  private String imageName;

  @Inject
  public KubeConnector(
      DockerConnectorConfiguration connectorConfiguration,
      DockerConnectionFactory connectionFactory,
      DockerRegistryAuthResolver authResolver,
      DockerApiVersionPathPrefixProvider dockerApiVersionPathPrefixProvider,
      OpenShiftPvcHelper openShiftPvcHelper,
      OpenShiftRouteCreator openShiftRouteCreator,
      KubeDeploymentCleaner kubeDeploymentCleaner,
      EventService eventService,
      @Nullable @Named("che.docker.ip.external") String cheServerExternalAddress,
      @Named("che.openshift.project") String openShiftCheProjectName,
      @Named("che.openshift.liveness.probe.delay") int openShiftLivenessProbeDelay,
      @Named("che.openshift.liveness.probe.timeout") int openShiftLivenessProbeTimeout,
      @Named("che.openshift.workspaces.pvc.name") String workspacesPersistentVolumeClaim,
      @Named("che.openshift.workspaces.pvc.quantity") String workspacesPvcQuantity,
      @Named("che.workspace.storage") String cheWorkspaceStorage,
      @Named("che.workspace.projects.storage") String cheWorkspaceProjectsStorage,
      @Nullable @Named("che.openshift.workspace.memory.request") String cheWorkspaceMemoryRequest,
      @Nullable @Named("che.openshift.workspace.memory.override") String cheWorkspaceMemoryLimit,
      @Named("che.openshift.secure.routes") boolean secureRoutes,
      @Named("che.openshift.precreate.workspace.dirs") boolean createWorkspaceDirs) {

    super(
        connectorConfiguration,
        connectionFactory,
        authResolver,
        dockerApiVersionPathPrefixProvider,
        openShiftPvcHelper,
        openShiftRouteCreator,
        kubeDeploymentCleaner,
        eventService,
        cheServerExternalAddress,
        openShiftCheProjectName,
        openShiftLivenessProbeDelay,
        openShiftLivenessProbeTimeout,
        workspacesPersistentVolumeClaim,
        workspacesPvcQuantity,
        cheWorkspaceStorage,
        cheWorkspaceProjectsStorage,
        cheWorkspaceMemoryRequest,
        cheWorkspaceMemoryLimit,
        secureRoutes,
        createWorkspaceDirs);

    this.cheServerExternalAddress = cheServerExternalAddress;
    this.openShiftCheProjectName = openShiftCheProjectName;
    this.openShiftLivenessProbeDelay = openShiftLivenessProbeDelay;
    this.openShiftLivenessProbeTimeout = openShiftLivenessProbeTimeout;
    this.workspacesPersistentVolumeClaim = workspacesPersistentVolumeClaim;
    this.workspacesPvcQuantity = workspacesPvcQuantity;
    this.cheWorkspaceStorage = cheWorkspaceStorage;
    this.cheWorkspaceProjectsStorage = cheWorkspaceProjectsStorage;
    this.cheWorkspaceMemoryRequest = cheWorkspaceMemoryRequest;
    this.cheWorkspaceMemoryLimit = cheWorkspaceMemoryLimit;
    this.secureRoutes = secureRoutes;
    this.createWorkspaceDirs = createWorkspaceDirs;
    this.openShiftPvcHelper = openShiftPvcHelper;
    this.openShiftRouteCreator = openShiftRouteCreator;
  }

  /**
   * Creates an ImageStream that tracks the repository.
   *
   * <p>Note: This method does not cause the relevant image to actually be pulled to the local
   * repository, but creating the ImageStream is necessary as it is used to obtain the address of
   * the internal Docker registry later.
   *
   * @see DockerConnector#pull(PullParams, ProgressMonitor)
   */
  @Override
  public void pull(final PullParams params, final ProgressMonitor progressMonitor)
      throws IOException {
    this.imageName = params.getFullRepo();
  }

  @Override
  public void tag(final TagParams params)
      throws IOException {
  }

  /**
   * @param createContainerParams
   * @return
   * @throws IOException
   */
  @Override
  public ContainerCreated createContainer(CreateContainerParams createContainerParams)
          throws IOException {
    String containerName =
            KubernetesStringUtils.convertToContainerName(createContainerParams.getContainerName());
    String workspaceID = getCheWorkspaceId(createContainerParams);

    // imageForDocker is the docker version of the image repository. It's needed for other
    // OpenShiftConnector API methods, but is not acceptable as an OpenShift name
    String imageForDocker = this.imageName; //createContainerParams.getContainerConfig().getImage();

    ContainerConfig containerConfig = createContainerParams.getContainerConfig();
    ImageConfig imageConfig = inspectImage(InspectImageParams.create(imageForDocker)).getConfig();

    final Set<String> exposedPorts = getExposedPorts(containerConfig, imageConfig);
    final Map<String, String> labels = getLabels(containerConfig, imageConfig);
    Map<String, String> portsToRefName = getPortsToRefName(labels, exposedPorts);

    String[] envVariables = createContainerParams.getContainerConfig().getEnv();
    String[] volumes = createContainerParams.getContainerConfig().getHostConfig().getBinds();

    Map<String, String> additionalLabels = createContainerParams.getContainerConfig().getLabels();
    String networkName =
            createContainerParams.getContainerConfig().getHostConfig().getNetworkMode();
    EndpointConfig endpointConfig =
            createContainerParams
                    .getContainerConfig()
                    .getNetworkingConfig()
                    .getEndpointsConfig()
                    .get(networkName);
    String[] endpointAliases = endpointConfig != null ? endpointConfig.getAliases() : new String[0];

    Map<String, Quantity> resourceLimits = new HashMap<>();
    if (!isNullOrEmpty(cheWorkspaceMemoryLimit)) {
      LOG.info(
              "Che property 'che.openshift.workspace.memory.override' "
                      + "used to override workspace memory limit to {}.",
              cheWorkspaceMemoryLimit);
      resourceLimits.put("memory", new Quantity(cheWorkspaceMemoryLimit));
    } else {
      long memoryLimitBytes =
              createContainerParams.getContainerConfig().getHostConfig().getMemory();
      String memoryLimit = Long.toString(memoryLimitBytes / 1048576) + "Mi";
      LOG.info("Creating workspace pod with memory limit of {}.", memoryLimit);
      resourceLimits.put("memory", new Quantity(cheWorkspaceMemoryLimit));
    }

    Map<String, Quantity> resourceRequests = new HashMap<>();
    if (!isNullOrEmpty(cheWorkspaceMemoryRequest)) {
      resourceRequests.put("memory", new Quantity(cheWorkspaceMemoryRequest));
    }

    String deploymentName;
    String serviceName;
    if (isDevMachine(createContainerParams)) {
      serviceName = deploymentName = CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID;
    } else {
      if (endpointAliases.length > 0) {
        serviceName = endpointAliases[0];
        deploymentName = CHE_OPENSHIFT_RESOURCES_PREFIX + serviceName;
      } else {
        // Should never happen
        serviceName =
                deploymentName =
                        CHE_OPENSHIFT_RESOURCES_PREFIX + KubernetesStringUtils.generateWorkspaceID();
      }
    }
    
    String containerID;
    KubernetesClient kubernetesClient = new DefaultKubernetesClient();
    try {
      createKuberntesService(
          deploymentName,
          serviceName,
          exposedPorts,
          portsToRefName,
          additionalLabels,
          endpointAliases);
      createKubernetesDeployment(
          deploymentName,
          imageForDocker,
          containerName,
          exposedPorts,
          portsToRefName,
          envVariables,
          volumes,
          resourceLimits,
          resourceRequests);

      containerID = waitAndRetrieveContainerID(deploymentName);
      if (containerID == null) {
        throw new OpenShiftException(
            "Failed to get the ID of the container running in the OpenShift pod");
      }
    } catch (IOException | KubernetesClientException e) {
      // Make sure we clean up deployment and service in case of an error -- otherwise Che can end up
      // in an inconsistent state.
      LOG.info("Error while creating Pod, removing deployment");
      LOG.info(e.getMessage());
      new KubeDeploymentCleaner().cleanDeploymentResources(deploymentName, openShiftCheProjectName);
      throw e;
    } finally {
      kubernetesClient.close();
    }

    return new ContainerCreated(containerID, null);

  }

  private void createKuberntesService(
      String deploymentName,
      String serviceName,
      Set<String> exposedPorts,
      Map<String, String> portsToRefName,
      Map<String, String> additionalLabels,
      String[] endpointAliases) {

    Map<String, String> selector =
          Collections.singletonMap(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);
      List<ServicePort> ports = KubernetesService.getServicePortsFrom(exposedPorts, portsToRefName);

      try (KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
        Service service =
            kubernetesClient
                .services()
                .inNamespace(this.openShiftCheProjectName)
                .createNew()
                .withNewMetadata()
                .withName(serviceName)
                .withLabels(new HashMap<>()).addToLabels("expose","true")
                .withAnnotations(KubernetesLabelConverter.labelsToNames(additionalLabels))
                .endMetadata()
                .withNewSpec()
                .withSelector(selector)
                .withPorts(ports)
                .withType("NodePort")
                .endSpec()
                .done();

        LOG.info("OpenShift service {} created", service.getMetadata().getName());
      }
  }

  private void createKubernetesDeployment(
      String deploymentName,
      String imageName,
      String sanitizedContainerName,
      Set<String> exposedPorts,
      Map<String, String> portsToRefName,
      String[] envVariables,
      String[] volumes,
      Map<String, Quantity> resourceLimits,
      Map<String, Quantity> resourceRequests)
      throws OpenShiftException {

    LOG.info("Creating OpenShift deployment {}", deploymentName);

    Map<String, String> selector =
        Collections.singletonMap(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);

    LOG.info(
        "Adding container {} to OpenShift deployment {}", sanitizedContainerName, deploymentName);

    Container container =
        new ContainerBuilder()
            .withName(sanitizedContainerName)
            .withImage(imageName)
            .withEnv(KubernetesEnvVar.getEnvFrom(envVariables))
            .withPorts(KubernetesContainer.getContainerPortsFrom(exposedPorts, portsToRefName))
            .withImagePullPolicy(OPENSHIFT_IMAGE_PULL_POLICY_IFNOTPRESENT)
            .withNewSecurityContext()
            .withPrivileged(false)
            .endSecurityContext()
            .withLivenessProbe(getLivenessProbeFrom(exposedPorts))
            .withVolumeMounts(getVolumeMountsFrom(volumes))
            .withNewResources()
            .withLimits(resourceLimits)
            .withRequests(resourceRequests)
            .endResources()
            .build();

    PodSpec podSpec =
        new PodSpecBuilder()
            .withContainers(container)
            .withVolumes(getVolumesFrom(volumes))
            .withTerminationGracePeriodSeconds(OPENSHIFT_POD_TERMINATION_GRACE_PERIOD)
            .build();

    Deployment deployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName(deploymentName)
            .withNamespace(this.openShiftCheProjectName)
            .endMetadata()
            .withNewSpec()
            .withReplicas(1)
            .withNewSelector()
            .withMatchLabels(selector)
            .endSelector()
            .withNewTemplate()
            .withNewMetadata()
            .withLabels(selector)
            .endMetadata()
            .withSpec(podSpec)
            .endTemplate()
            .endSpec()
            .build();

    try (OpenShiftClient openShiftClient = new DefaultOpenShiftClient()) {
      deployment =
          openShiftClient
              .extensions()
              .deployments()
              .inNamespace(this.openShiftCheProjectName)
              .create(deployment);
    }

    LOG.info("OpenShift deployment {} created", deploymentName);

  }

  @Override
  public ImageInfo inspectImage(InspectImageParams params) throws IOException {
    ImageConfig imageConfig = new ImageConfig();
    imageConfig.setCmd(new String[]{"tail", "-f", "/dev/null"});
    ImageInfo imageInfo = new ImageInfo();
    imageInfo.setConfig(imageConfig);
    return imageInfo;
  }

  @Override
  public ContainerInfo inspectContainer(String containerId) throws IOException {

    Pod pod = getChePodByContainerId(containerId);
    if (pod == null) {
      LOG.warn("No Pod found by container ID {}", containerId);
      return null;
    }

    String deploymentName = pod.getMetadata().getLabels().get(OPENSHIFT_DEPLOYMENT_LABEL);
    if (deploymentName == null) {
      LOG.warn(
          "No label {} found for Pod {}", OPENSHIFT_DEPLOYMENT_LABEL, pod.getMetadata().getName());
      return null;
    }

    Deployment deployment;
    try (OpenShiftClient client = new DefaultOpenShiftClient()) {
      deployment = client.extensions().deployments().withName(deploymentName).get();
      if (deployment == null) {
        LOG.warn(
            "No deployment matching label {}={} found", OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);
        return null;
      }
    }

    List<Container> deploymentContainers =
        deployment.getSpec().getTemplate().getSpec().getContainers();
    if (deploymentContainers.size() > 1) {
      throw new OpenShiftException("Multiple Containers found in Pod.");
    } else if (deploymentContainers.size() < 1
        || isNullOrEmpty(deploymentContainers.get(0).getImage())) {
      throw new OpenShiftException(String.format("Container %s not found", containerId));
    }
    String podPullSpec = deploymentContainers.get(0).getImage();

    String tagName = KubernetesStringUtils.getTagNameFromPullSpec(podPullSpec);

    Service svc = getCheServiceBySelector(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);
    if (svc == null) {
      LOG.warn("No Service found by selector {}={}", OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);
      return null;
    }

    return createContainerInfo(svc, pod, containerId);
  }

  /**
   * Collects the relevant information from a Service, and a Pod into a docker
   * ContainerInfo JSON object. The returned object is what would be returned by executing {@code
   * docker inspect <container>}, with fields filled as available.
   *
   * @param svc
   * @param pod
   * @param containerId
   * @return
   * @throws OpenShiftException
   */
  private ContainerInfo createContainerInfo(
      Service svc, Pod pod, String containerId) throws OpenShiftException {

    // In Che on OpenShift, we only have one container per pod.
    Container container = pod.getSpec().getContainers().get(0);

    // HostConfig
    HostConfig hostConfig = new HostConfig();
    hostConfig.setBinds(new String[0]);

    // Env vars
    List<String> env =
        container
            .getEnv()
            .stream()
            .map(e -> String.format("%s=%s", e.getName(), e.getValue()))
            .collect(Collectors.toList());
//    String[] env = Stream.concat(imageEnv.stream(), containerEnv.stream()).toArray(String[]::new);

    // Exposed Ports
    Map<String, List<PortBinding>> ports = getCheServicePorts(svc);
    Map<String, Map<String, String>> exposedPorts = new HashMap<>();
    for (String key : ports.keySet()) {
      exposedPorts.put(key, Collections.emptyMap());
    }

    // Labels

    /* vertx only */
    Map<String, String> vertxLabels = new HashMap<>();
    vertxLabels.put("che:server:8080:ref","vertx");
    vertxLabels.put("che:server:8080:protocol","http");
    vertxLabels.put("che:server:5005:ref","vertx-debug");
    vertxLabels.put("che:server:5005:protocol","http");
    Map<String, String> annotations =
        KubernetesLabelConverter.namesToLabels(svc.getMetadata().getAnnotations());
    Map<String, String> labels =
        Stream.concat(annotations.entrySet().stream(), vertxLabels.entrySet().stream())
            .filter(e -> e.getKey().startsWith(KubernetesLabelConverter.getCheServerLabelPrefix()))
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));


    // ContainerConfig
    ContainerConfig config = new ContainerConfig();
    config.setHostname(svc.getMetadata().getName());
    config.setEnv(env.toArray(new String[0]));
    config.setExposedPorts(exposedPorts);
    config.setLabels(labels);
    config.setImage(container.getImage());

    // NetworkSettings
    NetworkSettings networkSettings = new NetworkSettings();
    networkSettings.setIpAddress(svc.getSpec().getClusterIP());
    networkSettings.setGateway(svc.getSpec().getClusterIP());
    networkSettings.setPorts(ports);

    // Make final ContainerInfo
    ContainerInfo info = new ContainerInfo();
    info.setId(containerId);
    info.setConfig(config);
    info.setNetworkSettings(networkSettings);
    info.setHostConfig(hostConfig);
    info.setImage(this.imageName);

    // In Che on OpenShift, we only have one container per pod.
    info.setState(getContainerStates(pod).get(0));
    return info;
  }


}
