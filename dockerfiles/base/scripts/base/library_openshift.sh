#!/bin/sh
# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

deployment_config_exist_by_name(){
  oc get dc ${1} > /dev/null 2>&1
  if [ "$?" == "0" ]; then
    return 0
  else
    return 1
  fi
}

get_server_pod_id() {
  log "oc get po --selector=\"deploymentconfig=${1}\" -o jsonpath='{.items[].metadata.name}'"
  oc get po --selector="deploymentconfig=${1}" -o jsonpath='{.items[].metadata.name}' 2>&1 || false
}

wait_until_pod_is_available() {
  CONTAINER_START_TIMEOUT=${1}

  ELAPSED=0
  until pod_is_available ${2} || [ ${ELAPSED} -eq "${CONTAINER_START_TIMEOUT}" ]; do
    log "sleep 1"
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

pod_is_available() {
  if [ "$(oc get dc/${1} -o jsonpath=\"{$.status.conditions[?(@.type == \"Available\")].status}\")" != "True" ]; then
    return 1
  else
    return 0
  fi
}
