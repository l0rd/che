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

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.ReplicaSet;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.IOException;
import java.util.List;
import javax.inject.Singleton;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class KubeDeploymentCleaner extends OpenShiftDeploymentCleaner {
  private static final Logger LOG = LoggerFactory.getLogger(KubeDeploymentCleaner.class);
  private static final int KUBE_POD_DELETION_TIMEOUT = 120;
  private static final int KUBE_WAIT_POD_DELAY = 1000;

  @Override
  public void cleanDeploymentResources(final String deploymentName, final String namespace)
      throws IOException {
    cleanUpWorkspaceResources(deploymentName, namespace);
    super.waitUntilWorkspacePodIsDeleted(deploymentName, namespace);
  }

  private void cleanUpWorkspaceResources(final String deploymentName, final String namespace)
      throws IOException {
    Deployment deployment = KubernetesResourceUtil.getDeploymentByName(deploymentName, namespace);
    Service service =
        KubernetesResourceUtil.getServiceBySelector(
            OpenShiftConnector.OPENSHIFT_DEPLOYMENT_LABEL, deploymentName, namespace);
    List<ReplicaSet> replicaSets =
        KubernetesResourceUtil.getReplicaSetByLabel(
            OpenShiftConnector.OPENSHIFT_DEPLOYMENT_LABEL, deploymentName, namespace);

    try (KubernetesClient kubernetesClient = new DefaultKubernetesClient()) {
      if (service != null) {
        LOG.info("Removing OpenShift Service {}", service.getMetadata().getName());
        kubernetesClient.resource(service).delete();
      }

      if (deployment != null) {
        LOG.info("Removing OpenShift Deployment {}", deployment.getMetadata().getName());
        kubernetesClient.resource(deployment).delete();
      }

      if (replicaSets != null && replicaSets.size() > 0) {
        LOG.info("Removing OpenShift ReplicaSets for deployment {}", deploymentName);
        replicaSets.forEach(rs -> kubernetesClient.resource(rs).delete());
      }
    }
  }
}
