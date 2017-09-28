#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

echo "[CHE] This script is going to wait until Che is deployed and available"

command -v kubectl >/dev/null 2>&1 || { echo >&2 "[CHE] [ERROR] Command line tool kubectl is required but it's not installed. Aborting."; exit 1; }
command -v jq >/dev/null 2>&1 || { echo >&2 "[CHE] [ERROR] Command line tool jq (https://stedolan.github.io/jq) is required but it's not installed. Aborting."; exit 1; }

if [ -z "${CHE_API_ENDPOINT+x}" ]; then
    echo -n "[CHE] Inferring \$CHE_API_ENDPOINT..."
    che_host=$(minikube ip)
    if [ -z "${che_host}" ]; then echo >&2 "[CHE] [ERROR] Failed to infer environment variable \$CHE_API_ENDPOINT. Aborting. Please set it and run ${0} script again."; exit 1; fi
    che_port=$(kubectl get service che-host -o json | jq .spec.ports[].nodePort)
    if [ -z "${che_port}" ]; then echo >&2 "[CHE] [ERROR] Failed to infer environment variable \$CHE_API_ENDPOINT. Aborting. Please set it and run ${0} script again."; exit 1; fi
    protocol="http"
    CHE_API_ENDPOINT="${protocol}://${che_host}:${che_port}/api"
    echo "done (${CHE_API_ENDPOINT})"
fi

available=$(kubectl get deployment che -o json | jq '.status.conditions[] | select(.type == "Available") | .status')

DEPLOYMENT_TIMEOUT_SEC=120
POLLING_INTERVAL_SEC=5
end=$((SECONDS+DEPLOYMENT_TIMEOUT_SEC))
while [ "${available}" != "\"True\"" ] && [ ${SECONDS} -lt ${end} ]; do
  available=$(kubectl get deployment che -o json | jq '.status.conditions[] | select(.type == "Available") | .status')
  timeout_in=$((end-SECONDS))
  echo "[CHE] Deployment is in progress...(Available.status=${available}, Timeout in ${timeout_in}s)"
  sleep ${POLLING_INTERVAL_SEC}
done

if [ "${available}" == "\"True\"" ]; then
  echo "[CHE] Che deployed successfully"
elif [ ${SECONDS} -lt ${end} ]; then
  echo "[CHE] [ERROR] Deployment timeout. Aborting."
  exit 1
fi

che_http_status=$(curl -s -o /dev/null -I -w "%{http_code}" "${CHE_API_ENDPOINT}/system/state")
if [ "${che_http_status}" == "200" ]; then  
  echo "[CHE] Che is up and running"
else
  echo "[CHE] [ERROR] Che is not reponding (HTTP status= ${che_http_status})"
  exit 1
fi

echo
echo
