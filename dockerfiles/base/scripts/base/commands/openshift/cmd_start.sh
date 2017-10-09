#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Mario Loriedo - Initial Implementation
#

help_cmd_start() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} start [PARAMETERS]\n"
  text "\n"
  text "Starts ${CHE_MINI_PRODUCT_NAME} and verifies its operation\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --follow                          Displays server logs to console and blocks until user interrupts\n"
  text "  --force                           Uses 'docker rmi' and 'docker pull' to forcibly retrieve latest images\n"
  text "  --no-force                        Updates images if matching tag not found in local cache\n"
  text "  --pull                            Uses 'docker pull' to check for new remote versions of images\n"
  text "  --skip:config                     Skip re-generation of config files placed into /instance\n"
  text "  --skip:preflight                  Skip preflight checks\n"
  text "  --skip:postflight                 Skip postflight checks\n"
  text "\n"  
}

pre_cmd_start() {
  CHE_SKIP_CONFIG=false
  CHE_SKIP_PREFLIGHT=false
  CHE_SKIP_POSTFLIGHT=false
  CHE_FOLLOW_LOGS=false
  FORCE_UPDATE="--no-force"

  while [ $# -gt 0 ]; do
    case $1 in
      --skip:config)
        CHE_SKIP_CONFIG=true
        shift ;;
      --skip:preflight)
        CHE_SKIP_PREFLIGHT=true
        shift ;;
      --skip:postflight)
        CHE_SKIP_POSTFLIGHT=true
        shift ;;
      --follow)
        CHE_FOLLOW_LOGS=true
        shift ;;
      --force)
        FORCE_UPDATE="--force"
        shift ;;
      --no-force)
        FORCE_UPDATE="--no-force"
        shift ;;
      --pull)
        FORCE_UPDATE="--pull"
        shift ;;
      *)
        shift ;;
    esac
  done
}

post_cmd_start() {
  :
}


cmd_start() {
  # If already running, just display output again
  check_if_booted

  if server_is_booted; then
    return 1
  fi

  # Always regenerate puppet configuration from environment variable source, whether changed or not.
  # If the current directory is not configured with an .env file, it will initialize
  if skip_config; then
    cmd_lifecycle config $FORCE_UPDATE --skip:config
  else
    cmd_lifecycle config $FORCE_UPDATE
  fi

  # Preflight checks
  #   a) Check for open ports
  #   b) Test simulated connections for failures
  if ! is_fast && ! skip_preflight; then
    info "start" "Preflight checks"
    cmd_start_check_preflight
    text "\n"
  fi

  # Start ${CHE_FORMAL_PRODUCT_NAME}
  # Note bug in docker requires relative path, not absolute path to compose file
  info "start" "Starting containers..."
  OC_APPLY_COMMAND="oc apply --force=true -f=\"${REFERENCE_OPENSHIFT_FILE}\""

  ## validate the compose file (quiet mode)
  if local_repo; then
    oc --file=${REFERENCE_OPENSHIFT_FILE} --dry-run > /dev/null 2>&1 || (error "Invalid OpenShift yaml file content at ${REFERENCE_OPENSHIFT_FILE}" && return 2)
  fi

  if ! debug_server; then
    OC_APPLY_COMMAND+=" >> \"${LOGS}\" 2>&1"
  fi

  log ${OC_APPLY_COMMAND}
  eval ${OC_APPLY_COMMAND} || (error "Error during 'oc apply' - printing 30 line tail of ${CHE_HOST_CONFIG}/cli.log:" && tail -30 ${LOGS} && return 2)

  wait_until_booted

  if ! server_is_booted; then
    error "(${CHE_MINI_PRODUCT_NAME} start): Timeout waiting for server. Run \"docker logs ${CHE_CONTAINER_NAME}\" to inspect."
    return 2
  fi

  if ! is_fast && ! skip_postflight; then
    cmd_start_check_postflight
  fi

  check_if_booted
}

cmd_start_check_host_resources() {
  info "start" "Preflight check host resources for OpenShift not implemented yet"
}

cmd_start_check_ports() {
  info "start" "Preflight check ports for OpenShift not implemented yet"
}

# See cmd_network.sh for utilities for unning these tests
cmd_start_check_agent_network() {
  info "start" "Preflight check agent network for OpenShift not implemented yet"
}

cmd_start_check_preflight() {
  cmd_start_check_host_resources
  cmd_start_check_ports
  cmd_start_check_agent_network
}

cmd_start_check_postflight() {
  true
}

wait_until_booted() {

  wait_until_pod_is_available 30 ${CHE_DEPLOYMENT_CONFIG_NAME}
  if ! pod_is_available ${CHE_DEPLOYMENT_CONFIG_NAME}; then
    error "(${CHE_MINI_PRODUCT_NAME} start): Timeout waiting for ${CHE_MINI_PRODUCT_NAME} pod to be available."
    return 2
  fi

  info "start" "Services booting..."

  # CHE-3546 - if in development mode, then display the che server logs to STDOUT
  #            automatically kill the streaming of the log output when the server is booted
  if debug_server || follow_logs; then
    DOCKER_LOGS_COMMAND="oc logs -f dc/${CHE_DEPLOYMENT_CONFIG_NAME}"

    if debug_server; then 
      DOCKER_LOGS_COMMAND+=" &"
    fi

    eval $DOCKER_LOGS_COMMAND
    LOG_PID=$!
  else
    info "start" "Server logs at \"oc logs -f dc/${CHE_DEPLOYMENT_CONFIG_NAME}\""
  fi

  wait_until_server_is_booted 60 ${CURRENT_CHE_SERVER_CONTAINER_ID}

  if debug_server; then
    kill $LOG_PID > /dev/null 2>&1
    info ""
  fi
}

check_if_booted() {
  if deployment_config_exist_by_name ${CHE_DEPLOYMENT_CONFIG_NAME}; then
    local CURRENT_CHE_SERVER_POD_ID=$(get_server_pod_id $CHE_DEPLOYMENT_CONFIG_NAME)
    if server_is_booted; then
      DISPLAY_URL=$(get_display_url)
      info "start" "Booted and reachable"
      info "start" "Ver: $(get_installed_version)"
      info "start" "Use: ${DISPLAY_URL}"
      info "start" "API: ${DISPLAY_URL}/swagger"
      if debug_server; then
        DISPLAY_DEBUG_URL=$(get_debug_display_url)
        info "start" "Debug: ${DISPLAY_DEBUG_URL}"
      fi
    fi
  fi
}

skip_preflight() {
  if [ "${CHE_SKIP_PREFLIGHT}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

skip_postflight() {
  if [ "${CHE_SKIP_POSTFLIGHT}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

follow_logs() {
  if [ "${CHE_FOLLOW_LOGS}" = "true" ]; then
    return 0
  else
    return 1
  fi
}
