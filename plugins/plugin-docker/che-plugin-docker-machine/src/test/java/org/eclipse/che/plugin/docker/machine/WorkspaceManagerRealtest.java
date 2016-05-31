/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.machine;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.MachineInstanceProviders;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.MachineRegistry;
import org.eclipse.che.api.machine.server.dao.SnapshotDao;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.api.machine.server.util.RecipeRetriever;
import org.eclipse.che.api.machine.server.wsagent.WsAgentLauncher;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.env.impl.che.CheEnvStartStrategy;
import org.eclipse.che.api.workspace.server.env.impl.che.CheEnvironmentEngine;
import org.eclipse.che.api.workspace.server.env.impl.che.CheEnvironmentValidator;
import org.eclipse.che.api.workspace.server.env.impl.che.LinksBasedCheEnvStartStrategy;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.InitialAuthConfig;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.helper.DefaultNetworkFinder;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.machine.local.LocalDockerNode;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

/**
 * author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class WorkspaceManagerRealtest {

    @Mock
    private EventService                 eventService;
    @Mock
    private WorkspaceDao                 workspaceDao;
    @Mock
    private UserManager                  userManager;
    @Mock
    private MachineInstanceProviders     machineInstanceProviders;
    @Mock
    private SnapshotDao                  snapshotDao;
    @Mock
    private WsAgentLauncher              wsAgentLauncher;
    @Mock
    private DockerInstanceStopDetector   stopDetector;
    @Mock
    private WorkspaceFolderPathProvider  workspaceFolderPathProvider;
    @Mock
    private DockerContainerNameGenerator containerNameGenerator;
    @Mock
    private DockerFactory                dockerFactory;

    private RecipeDownloader             recipeDownloader;
    private RecipeRetriever              recipeRetriever;
    private DockerConnectorConfiguration connectorConfiguration;
    private CheEnvStartStrategy          envStartStrategy;
    private DockerConnector              dockerConnector;
    private DockerInstanceProvider       instanceProvider;
    private MachineRegistry              machineRegistry;
    private MachineManager               machineManager;
    private CheEnvironmentValidator      environmentValidator;
    private CheEnvironmentEngine         engine;
    private WorkspaceRuntimes            runtimes;
    private WorkspaceManager             workspaceManager;

    @BeforeMethod
    public void setUp() throws Exception {
        connectorConfiguration = new DockerConnectorConfiguration(new InitialAuthConfig(),
                                                                  new DefaultNetworkFinder());
        dockerConnector = new DockerConnector(connectorConfiguration, new DockerConnectionFactory(connectorConfiguration));
        envStartStrategy = new LinksBasedCheEnvStartStrategy();
        recipeDownloader = new RecipeDownloader(new URI("http://localhost:8080/api"));
        recipeRetriever = new RecipeRetriever(recipeDownloader);
        instanceProvider = new DockerInstanceProvider(dockerConnector,
                                                      connectorConfiguration,
                                                      dockerFactory,
                                                      stopDetector,
                                                      containerNameGenerator,
                                                      recipeRetriever,
                                                      Collections.emptySet(),
                                                      Collections.emptySet(),
                                                      Collections.emptySet(),
                                                      Collections.emptySet(),
                                                      "",
                                                      workspaceFolderPathProvider,
                                                      "/home/gaal/workspace",
                                                      false,
                                                      false,
                                                      Collections.emptySet(),
                                                      Collections.emptySet(),
                                                      false);

        machineRegistry = new MachineRegistry();
        machineManager = new MachineManager(snapshotDao,
                                            machineRegistry,
                                            machineInstanceProviders,
                                            "/tmp/che-logs",
                                            eventService,
                                            2048,
                                            wsAgentLauncher);
        environmentValidator = new CheEnvironmentValidator(machineInstanceProviders);
        engine = new CheEnvironmentEngine(machineManager, environmentValidator, envStartStrategy);
        runtimes = new WorkspaceRuntimes(eventService, Collections.singletonMap(CheEnvironmentEngine.ENVIRONMENT_TYPE, engine));
        workspaceManager = new WorkspaceManager(workspaceDao,
                                                runtimes,
                                                eventService,
                                                machineManager,
                                                userManager,
                                                false,
                                                false);

        EnvironmentContext environmentContext = new EnvironmentContext();
        environmentContext.setWorkspaceId("wsId");
        environmentContext.setWorkspaceName("wsName");
        environmentContext.setSubject(new SubjectImpl("sName", "sId", "token", Collections.emptyList(), false));
        EnvironmentContext.setCurrent(environmentContext);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        EnvironmentContext.reset();
    }

    @Test
    public void should() throws Exception {
        WorkspaceImpl workspace = createWs();
        when(workspaceDao.get(eq("wsId"))).thenReturn(workspace);
        when(machineManager.getSnapshots(anyString(), anyString())).thenReturn(Collections.emptyList());
        when(machineInstanceProviders.getProviderTypes()).thenReturn(Collections.singleton("docker"));
        when(machineInstanceProviders.getProvider("docker")).thenReturn(instanceProvider);

        WorkspaceImpl ws = workspaceManager.startWorkspace("wsId", null, null);

        boolean starting = true;
        while (starting) {
            Thread.currentThread().sleep(1000);

            WorkspaceImpl ws1 = workspaceManager.getWorkspace("wsId");

            starting = ws1.getStatus() != WorkspaceStatus.RUNNING;
        }

        assertTrue(true);
    }

    private WorkspaceImpl createWs() {
        MachineConfigImpl machine = MachineConfigImpl.builder()
                                                     .setDev(true)
                                                     .setName("machineName")
                                                     .setType("docker")
                                                     .setSource(new MachineSourceImpl("dockerfile", "https://gist.githubusercontent.com/garagatyi/74ed87761d927985875b3500c7a621f2/raw/e20ce8427c6a9f3ab50f48b88382ceb7ed496ea3/Dockerfile"))
                                                     .build();
        return WorkspaceImpl.builder()
                            .generateId()
                            .setNamespace("namespace1")
                            .setTemporary(false)
                            .setStatus(WorkspaceStatus.STOPPED)
                            .setConfig(WorkspaceConfigImpl.builder()
                                                          .setDefaultEnv("defEnv")
                                                          .setName("wsName")
                                                          .setEnvironments(singletonList(new EnvironmentImpl("defEnv",
                                                                                                             null,
                                                                                                             singletonList(machine))))
                                                          .build())
                            .build();
    }

    private class DockerFactory implements DockerMachineFactory {

        @Override
        public InstanceProcess createProcess(@Assisted Command command, @Assisted("container") String container,
                                             @Assisted("outputChannel") String outputChannel, @Assisted("pid_file_path") String pidFilePath,
                                             @Assisted int pid) throws MachineException {
            return new DockerProcess(dockerConnector, command, container, outputChannel, pidFilePath, pid);
        }

        @Override
        public Instance createInstance(@Assisted Machine machine, @Assisted("container") String container, @Assisted("image") String image,
                                       @Assisted DockerNode node, @Assisted LineConsumer outputConsumer) throws MachineException {
            return new DockerInstance(dockerConnector, "localhost:5000", this, machine, container, image, node, outputConsumer, null, null, false);
        }

        @Override
        public DockerNode createNode(@Assisted("workspace") String workspaceId, @Assisted("container") String containerId)
                throws MachineException {
            try {
                return new LocalDockerNode("wsId",
                                           workspaceFolderPathProvider,
                                           connectorConfiguration);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        public DockerInstanceRuntimeInfo createMetadata(@Assisted ContainerInfo containerInfo, @Assisted String containerHost,
                                                        @Assisted MachineConfig machineConfig) {
            return new DockerInstanceRuntimeInfo(containerInfo, containerHost, machineConfig, Collections.emptySet(),
                                                 Collections.emptySet());
        }
    }
}
