#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# This script is meant for quick & easy install of Che on OpenShift via:
#
#  ``` bash
#   DEPLOY_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/master/dockerfiles/cli/scripts/openshift/deploy_che.sh
#   curl -fsSL ${DEPLOY_SCRIPT_URL} -o get-che.sh
#   WAIT_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/master/dockerfiles/cli/scripts/openshift/wait_until_che_is_available.sh
#   curl -fsSL ${WAIT_SCRIPT_URL} -o wait-che.sh
#   STACKS_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/master/dockerfiles/cli/scripts/openshift/replace_stacks.sh
#   curl -fsSL ${STACKS_SCRIPT_URL} -o stacks-che.sh
#   bash get-che.sh && wait-che.sh && stacks-che.sh
#   ```
#
# For more deployment options: https://www.eclipse.org/che/docs/setup/openshift/index.html

set -e

# --------------
# Print Che logo 
# --------------

echo
cat <<EOF
[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;52m1[38;5;94m0[38;5;136m1[38;5;215m0[38;5;215m0[38;5;136m0[38;5;94m0[38;5;58m0[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m 
[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;58m0[38;5;136m1[38;5;179m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;179m1[38;5;136m0[38;5;58m1[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m 
[0m [0m [0m [0m [0m [0m [0m [38;5;52m0[38;5;94m1[38;5;136m0[38;5;179m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;179m0[38;5;136m1[38;5;94m1[38;5;52m0[0m [0m [0m [0m [0m [0m [0m 
[0m [0m [0m [38;5;58m1[38;5;136m1[38;5;179m0[38;5;215m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;215m0[38;5;179m0[38;5;100m0[38;5;58m1[0m [0m [0m 
[38;5;136m0[38;5;179m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;136m0[38;5;52m1
[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;215m0[38;5;179m0[38;5;179m0[38;5;215m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;215m1[38;5;179m1[38;5;100m1[38;5;58m0[0m [0m [0m 
[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;221m0[38;5;179m1[38;5;136m0[38;5;94m0[38;5;52m1[0m [0m [0m [0m [38;5;52m1[38;5;94m0[38;5;136m0[38;5;179m1[38;5;221m1[38;5;221m1[38;5;221m0[38;5;179m1[38;5;136m0[38;5;94m0[38;5;52m0[0m [0m [0m [0m [0m [0m [0m 
[38;5;221m1[38;5;221m0[38;5;221m1[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;215m1[38;5;179m0[38;5;136m0[38;5;58m0[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;58m0[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m 
[38;5;221m1[38;5;221m0[38;5;221m0[38;5;221m1[38;5;221m0[38;5;179m1[38;5;136m0[38;5;94m1[38;5;52m1[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;17m0[38;5;59m1[38;5;60m1[38;5;60m0
[38;5;221m1[38;5;179m0[38;5;180m1[38;5;138m0[38;5;102m0[38;5;60m0[38;5;23m0[38;5;17m1[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;17m1[38;5;60m1[38;5;60m0[38;5;67m1[38;5;103m1[38;5;103m1[38;5;103m1[38;5;67m1
[38;5;103m1[38;5;67m1[38;5;61m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;103m1[38;5;103m1[38;5;67m1[38;5;60m0[38;5;60m1[38;5;23m1[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;23m1[38;5;60m1[38;5;60m1[38;5;67m0[38;5;103m1[38;5;103m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m0[38;5;67m1
[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m0[38;5;103m0[38;5;103m1[38;5;103m1[38;5;67m0[38;5;60m1[38;5;59m0[38;5;17m1[0m [0m [0m [0m [0m [0m [38;5;17m0[38;5;59m0[38;5;60m0[38;5;67m0[38;5;67m0[38;5;103m1[38;5;103m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1
[38;5;103m0[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;103m0[38;5;103m0[38;5;67m0[38;5;60m0[38;5;60m0[38;5;60m0[38;5;60m1[38;5;67m0[38;5;103m1[38;5;103m1[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;103m1
[38;5;59m1[38;5;60m1[38;5;67m0[38;5;67m1[38;5;103m0[38;5;103m0[38;5;67m0[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m0[38;5;103m0[38;5;103m1[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;103m0[38;5;103m1[38;5;67m1[38;5;67m0[38;5;60m1[38;5;59m1
[0m [0m [0m [0m [38;5;23m0[38;5;60m0[38;5;60m0[38;5;67m0[38;5;103m0[38;5;103m0[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m0[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m1[38;5;103m1[38;5;103m0[38;5;67m0[38;5;60m0[38;5;60m0[38;5;23m0[0m [0m [0m [0m 
[0m [0m [0m [0m [0m [0m [0m [0m [38;5;17m0[38;5;59m0[38;5;60m0[38;5;67m1[38;5;103m1[38;5;103m0[38;5;103m1[38;5;67m0[38;5;67m1[38;5;67m1[38;5;67m0[38;5;67m1[38;5;67m0[38;5;67m0[38;5;67m0[38;5;67m1[38;5;67m0[38;5;103m0[38;5;103m0[38;5;103m1[38;5;67m1[38;5;60m1[38;5;60m1[38;5;17m0[0m [0m [0m [0m [0m [0m [0m [0m 
[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;17m1[38;5;59m1[38;5;60m0[38;5;60m1[38;5;67m0[38;5;103m1[38;5;103m1[38;5;67m1[38;5;67m0[38;5;103m0[38;5;103m0[38;5;67m0[38;5;60m1[38;5;60m0[38;5;59m0[38;5;17m1[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m 
[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [38;5;17m0[38;5;60m0[38;5;60m0[38;5;60m1[38;5;60m0[38;5;17m1[0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m [0m 
[0m
EOF
echo

# --------------------------------------------------------
# Check pre-requisites
# --------------------------------------------------------
command -v kubectl >/dev/null 2>&1 || { echo >&2 "[CHE] [ERROR] Command line tool kubectl (https://kubernetes.io/docs/tasks/tools/install-kubectl/) is required but it's not installed. Aborting."; exit 1; }

# --------------------------------------------------------
# Parse options
# --------------------------------------------------------
while [[ $# -gt 1 ]]
do
key="$1"
case $key in
    -c | --command)
    COMMAND="$2"
    shift
    ;;
    *)
            # unknown option
    ;;
esac
shift
done

DEFAULT_COMMAND="deploy"
COMMAND=${COMMAND:-${DEFAULT_COMMAND}}
DEFAULT_CHE_IMAGE_REPO="docker.io/eclipse/che-server"
CHE_IMAGE_REPO=${CHE_IMAGE_REPO:-${DEFAULT_CHE_IMAGE_REPO}}
DEFAULT_CHE_IMAGE_TAG="nightly"
CHE_IMAGE_TAG=${CHE_IMAGE_TAG:-${DEFAULT_CHE_IMAGE_TAG}}
DEFAULT_CHE_LOG_LEVEL="INFO"
CHE_LOG_LEVEL=${CHE_LOG_LEVEL:-${DEFAULT_CHE_LOG_LEVEL}}

# Keycloak production endpoints are used by default
DEFAULT_KEYCLOAK_OSO_ENDPOINT="https://sso.openshift.io/auth/realms/fabric8/broker/openshift-v3/token"
KEYCLOAK_OSO_ENDPOINT=${KEYCLOAK_OSO_ENDPOINT:-${DEFAULT_KEYCLOAK_OSO_ENDPOINT}}
DEFAULT_KEYCLOAK_GITHUB_ENDPOINT="https://sso.openshift.io/auth/realms/fabric8/broker/github/token"
KEYCLOAK_GITHUB_ENDPOINT=${KEYCLOAK_GITHUB_ENDPOINT:-${DEFAULT_KEYCLOAK_GITHUB_ENDPOINT}}

# K8S_FLAVOR minikube only
# TODO Set flavour via a parameter
DEFAULT_K8S_FLAVOR=minikube
K8S_FLAVOR=${K8S_FLAVOR:-${DEFAULT_K8S_FLAVOR}}


if [ "${K8S_FLAVOR}" == "minikube" ]; then
    # ---------------------------
    # Set minikube configuration
    # ---------------------------
    echo -n "[CHE] Checking if minikube is running..."
    minikube status | grep -q "Running" ||(echo "minikube is not running. Aborting"; exit 1)
    echo "done!"  

    DEFAULT_CHE_K8S_PROJECT="eclipse-che"
    CHE_K8S_PROJECT=${CHE_K8S_PROJECT:-${DEFAULT_CHE_K8S_PROJECT}}
    DEFAULT_K8S_NAMESPACE_URL="$(minikube ip)"
    K8S_NAMESPACE_URL=${K8S_NAMESPACE_URL:-${DEFAULT_K8S_NAMESPACE_URL}}
    DEFAULT_CHE_KEYCLOAK_DISABLED="true"
    CHE_KEYCLOAK_DISABLED=${CHE_KEYCLOAK_DISABLED:-${DEFAULT_CHE_KEYCLOAK_DISABLED}}
    DEFAULT_CHE_DEBUGGING_ENABLED="true"
    CHE_DEBUGGING_ENABLED=${CHE_DEBUGGING_ENABLED:-${DEFAULT_CHE_DEBUGGING_ENABLED}}
fi

# ---------------------------------------
# Verify that we have all env var are set
# ---------------------------------------
if [ -z "${K8S_NAMESPACE_URL+x}" ]; then echo "[CHE] **ERROR**Env var K8S_NAMESPACE_URL is unset. You need to set it to continue. Aborting"; exit 1; fi

# -------------------
# Set kubectl context 
# -------------------
echo -n "[CHE] Setting kubectl context for minikube..."
kubectl config use-context minikube &> /dev/null
echo "done!"

# --------------------------
# Create project (if needed)
# --------------------------
echo -n "[CHE] Checking if namespace \"${CHE_K8S_PROJECT}\" exists..."
if ! kubectl get namespace "${CHE_K8S_PROJECT}" &> /dev/null; then

  if [ "${COMMAND}" == "cleanup" ] || [ "${COMMAND}" == "rollupdate" ]; then echo "**ERROR** project doesn't exist. Aborting"; exit 1; fi

  echo -n "no creating it..."
  kubectl create namespace "${CHE_K8S_PROJECT}" &> /dev/null
  ## TODO we should consider kubectl apply the latest http://central.maven.org/maven2/io/fabric8/tenant/packages/fabric8-tenant-che-quotas-oso/
fi
echo "done!"

# -------------------------
# Set the current namespace 
# -------------------------
kubectl config set-context $(kubectl config current-context) --namespace=${CHE_K8S_PROJECT} &> /dev/null

# -------------------------------------------------------------
# If command == clean up then delete all k8s objects
# -------------------------------------------------------------
if [ "${COMMAND}" == "cleanup" ]; then
  echo "[CHE] Deleting all k8s objects..."
  kubectl delete all --all -n "${CHE_K8S_PROJECT}" 
  echo "[CHE] Cleanup successfully started. Use \"kubectl get all -n \"${CHE_K8S_PROJECT}\"\" to verify that all resources have been deleted."
  exit 0
# -------------------------------------------------------------
# If command == clean up then delete all k8s objects
# -------------------------------------------------------------
elif [ "${COMMAND}" == "rollupdate" ]; then 
  echo "[CHE] Rollout latest version of Che..."
  kubectl rollout latest che  -n "${CHE_K8S_PROJECT}" 
  echo "[CHE] Rollaout successfully started"
  exit 0
# ----------------------------------------------------------------
# At this point command should be "deploy" otherwise it's an error 
# ----------------------------------------------------------------
elif [ "${COMMAND}" != "deploy" ]; then 
  echo "[CHE] **ERROR**: Command \"${COMMAND}\" is not a valid command. Aborting."
  exit 1
fi

# -------------------------------------------------------------
# Verify that Che ServiceAccount has admin rights at project level
# -------------------------------------------------------------
## TODO we should create Che SA if it doesn't exist
## TODO we should check if che has admin rights before creating the role biding
## TODO if we are not in minikube we should fail if che SA doesn't have admin rights
# if [[ "${K8S_FLAVOR}" =~ ^(minikube)$ ]]; then
#   echo -n "[CHE] Setting admin role to \"che\" service account..."
#   echo "apiVersion: v1
# kind: RoleBinding
# metadata:
#   name: che
# roleRef:
#   name: admin
# subjects:
# - kind: ServiceAccount
#   name: che" | kubectl apply -n "${CHE_K8S_PROJECT}" -f -
# fi

# ----------------------------------------------
# Get latest version of fabric8 tenant templates
# ----------------------------------------------
# TODO make it possible to use a local Che template instead of always downloading it from maven central
echo -n "[CHE] Retrieving latest version of fabric8 tenant Che template..."
OSIO_VERSION=$(curl -sSL http://central.maven.org/maven2/io/fabric8/tenant/apps/che/maven-metadata.xml | grep latest | sed -e 's,.*<latest>\([^<]*\)</latest>.*,\1,g')
echo "done! (v.${OSIO_VERSION})"

# ----------------------------------------------
# Start the deployment
# ----------------------------------------------
CHE_IMAGE="${CHE_IMAGE_REPO}:${CHE_IMAGE_TAG}"
# Escape slashes in CHE_IMAGE to use it with sed later
# e.g. docker.io/rhchestage => docker.io\/rhchestage
CHE_IMAGE_SANITIZED=$(echo "${CHE_IMAGE}" | sed 's/\//\\\//g')

echo
if [ "${K8S_FLAVOR}" == "minikube" ]; then

  echo "[CHE] Deploying exposecontroller (https://github.com/fabric8io/exposecontroller)"
  EC_YML_URL=http://central.maven.org/maven2/io/fabric8/devops/apps/exposecontroller/2.2.268/exposecontroller-2.2.268-kubernetes.yml
  kubectl apply -f "${EC_YML_URL}" -n "${CHE_K8S_PROJECT}"

  echo "[CHE] Deploying Che on minikube (image ${CHE_IMAGE})"
  curl -sSL http://central.maven.org/maven2/io/fabric8/tenant/apps/che/"${OSIO_VERSION}"/che-"${OSIO_VERSION}"-kubernetes.yml | \
    if [ ! -z "${K8S_NAMESPACE_URL+x}" ]; then sed "s/    hostname-http:.*/    hostname-http: ${K8S_NAMESPACE_URL}/" ; else cat -; fi | \
    sed "s/          image:.*/          image: \"${CHE_IMAGE_SANITIZED}\"/" | \
    sed "s/    che-openshift-secure-routes: \"true\"/    che-openshift-secure-routes: \"false\"/" | \
    sed "s/    che-secure-external-urls: \"true\"/    che-secure-external-urls: \"false\"/" | \
    sed "s/    che.docker.server_evaluation_strategy.custom.external.protocol: https/    che.docker.server_evaluation_strategy.custom.external.protocol: http/" | \
    sed "s/    che.predefined.stacks.reload_on_start: \"true\"/    che.predefined.stacks.reload_on_start: \"false\"/" | \
    sed "s/    remote-debugging-enabled: \"false\"/    remote-debugging-enabled: \"${CHE_DEBUGGING_ENABLED}\"/" | \
    sed "s/    docker-connector: openshift/    docker-connector: kubernetes/" | \
    sed "s/    che-server-evaluation-strategy: docker-local-custom/    che-server-evaluation-strategy: docker-local/" | \
    sed "s|    keycloak-oso-endpoint:.*|    keycloak-oso-endpoint: ${KEYCLOAK_OSO_ENDPOINT}|" | \
    sed "s|    keycloak-github-endpoint:.*|    keycloak-github-endpoint: ${KEYCLOAK_GITHUB_ENDPOINT}|" | \
    grep -v -e "tls:" -e "insecureEdgeTerminationPolicy: Redirect" -e "termination: edge" | \
    if [ "${CHE_KEYCLOAK_DISABLED}" == "true" ]; then sed "s/    keycloak-disabled: \"false\"/    keycloak-disabled: \"true\"/" ; else cat -; fi | \
    kubectl apply -n "${CHE_K8S_PROJECT}" --force=true -f -

    # Expose k8s service using a NodePort (default is clusterIP that is not accessible externally)
    #kubectl expose -n eclipse-che deployment che --type=NodePort

    # Set label  expose=true to service che-host for exposecontroller
    kubectl label svc che-host expose=true --overwrite
fi
echo


# --------------------------------
# Setup debugging routes if needed
# --------------------------------
if [ "${CHE_DEBUGGING_ENABLED}" == "true" ]; then

  if kubectl get svc che-debug &> /dev/null; then
    echo -n "[CHE] Deleting old che-debug service..."
    kubectl delete svc che-debug
    echo "done"
  fi

  echo -n "[CHE] Creating an K8S route to debug Che wsmaster..."
  kubectl expose -n "${CHE_K8S_PROJECT}" deployment che --name=che-debug --target-port=http-debug --port=8000 --type=NodePort
  
  NodePort=$(kubectl get service che-debug -o jsonpath='{.spec.ports[0].nodePort}')
  echo "[CHE] Remote wsmaster debugging URL: $(minikube ip):${NodePort}"
fi

sleep 3
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
${DIR}/wait_until_che_is_available.sh

che_node_port=$(kubectl get svc che-host -o=json | jq .spec.ports[].nodePort)
che_route=$(minikube ip):"${che_node_port}"
echo 
echo "[CHE] Che deployment has been successufully bootstrapped"
echo "[CHE] -> To check Kubernetes events: 'kubectl get events -w'"
echo "[CHE] -> To check Che server logs: 'kubectl logs -f deployment/che'"
echo "[CHE] -> Che is available at: "
echo "[CHE]    http://${che_route}"
echo
echo
