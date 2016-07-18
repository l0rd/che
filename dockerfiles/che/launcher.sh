#!/bin/sh

init_logging() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  NC='\033[0m'
}

init_global_variables() {

  CHE_SERVER_CONTAINER_NAME="che"
  CHE_SERVER_IMAGE_NAME="codenvy/che"
  CHE_LAUNCER_IMAGE_NAME="codenvy/che-launcher"

  # Possible Docker install types are:
  #     native, boot2docker or moby
  DOCKER_INSTALL_TYPE=$(get_docker_install_type)

  # User configurable variables
  DEFAULT_DOCKER_HOST_IP=$(get_docker_host_ip)
  DEFAULT_CHE_HOSTNAME=$(get_che_hostname)
  DEFAULT_CHE_PORT="8080"
  DEFAULT_CHE_VERSION="latest"
  DEFAULT_CHE_RESTART_POLICY="always"
  DEFAULT_CHE_USER="root"
  DEFAULT_CHE_LOG_LEVEL="info"
  DEFAULT_CHE_DATA_FOLDER="/home/user/che"

  CHE_HOSTNAME=${CHE_HOSTNAME:-${DEFAULT_CHE_HOSTNAME}}
  CHE_PORT=${CHE_PORT:-${DEFAULT_CHE_PORT}}
  CHE_VERSION=${CHE_VERSION:-${DEFAULT_CHE_VERSION}}
  CHE_RESTART_POLICY=${CHE_RESTART_POLICY:-${DEFAULT_CHE_RESTART_POLICY}}
  CHE_USER=${CHE_USER:-${DEFAULT_CHE_USER}}
  CHE_HOST_IP=${DOCKER_HOST_IP:-${DEFAULT_DOCKER_HOST_IP}}
  CHE_LOG_LEVEL=${CHE_LOG_LEVEL:-${DEFAULT_CHE_LOG_LEVEL}}
  CHE_DATA_FOLDER=${CHE_DATA_FOLDER:-${DEFAULT_CHE_DATA_FOLDER}}

  # CHE_CONF_ARGS are the Docker run options that need to be used if users set CHE_CONF_FOLDER:
  #   - empty if CHE_CONF_FOLDER is not set
  #   - -v ${CHE_CONF_FOLDER}:/conf -e "CHE_LOCAL_CONF_DIR=/conf" if CHE_CONF_FOLDER is set
  CHE_CONF_ARGS=${CHE_CONF_FOLDER:+-v ${CHE_CONF_FOLDER}:/conf -e \"CHE_LOCAL_CONF_DIR=/conf\"}


  USAGE="
Usage:
  docker run -v /var/run/docker.sock:/var/run/docker.sock ${CHE_LAUNCER_IMAGE_NAME} [COMMAND]
     start                              Starts Che server
     stop                               Stops Che server
     restart                            Restart Che server
     update                             Pull latest version of ${CHE_SERVER_IMAGE_NAME}
     info                               Print some debugging information

Docs: http://eclipse.org/che/getting-started.
"
}

usage () {
  printf "%s" "${USAGE}"
}

info() {
  printf  "${GREEN}INFO:${NC} %s\n" "${1}"
}

debug() {
  printf  "${BLUE}DEBUG:${NC} %s\n" "${1}"
}

error() {
  printf  "${RED}ERROR:${NC} %s\n" "${1}"
}

error_exit() {
  echo  "---------------------------------------"
  error "!!!"
  error "!!! ${1}"
  error "!!!"
  echo  "---------------------------------------"
  exit 1
}

print_debug_info() {
  debug "---------------------------------------"
  debug "---------  CHE DEBUG INFO   -----------"
  debug "---------------------------------------"
  debug ""
  debug "DOCKER_INSTALL_TYPE       = ${DOCKER_INSTALL_TYPE}"
  debug ""
  debug "CHE_SERVER_CONTAINER_NAME = ${CHE_SERVER_CONTAINER_NAME}"
  debug "CHE_SERVER_IMAGE_NAME     = ${CHE_SERVER_IMAGE_NAME}"
  debug ""
  VAL=$(if che_container_exist;then echo "YES"; else echo "NO"; fi)
  debug "CHE CONTAINER EXISTS?     ${VAL}"
  VAL=$(if che_container_is_running;then echo "YES"; else echo "NO"; fi)
  debug "CHE CONTAINER IS RUNNING? ${VAL}"
  VAL=$(if che_container_is_stopped;then echo "YES"; else echo "NO"; fi)
  debug "CHE CONTAINER IS STOPED?  ${VAL}"
  VAL=$(if server_is_booted;then echo "YES"; else echo "NO"; fi)
  debug "CHE SERVER IS BOOTED?     ${VAL}"
  debug ""
  debug "CHE_PORT                  = ${CHE_PORT}"
  debug "CHE_VERSION               = ${CHE_VERSION}"
  debug "CHE_RESTART_POLICY        = ${CHE_RESTART_POLICY}"
  debug "CHE_USER                  = ${CHE_USER}"
  debug "CHE_HOST_IP               = ${DOCKER_HOST_IP}"
  debug "CHE_LOG_LEVEL             = ${CHE_LOG_LEVEL}"
  debug "CHE_HOSTNAME              = ${CHE_HOSTNAME}"
  debug "CHE_DATA_FOLDER           = ${CHE_DATA_FOLDER}"
  debug "CHE_CONF_FOLDER           = ${CHE_CONF_FOLDER:-not set}"
  debug "---------------------------------------"
  debug "---------------------------------------"
  debug "---------------------------------------"
}

get_docker_install_type() {
  if uname -r | grep -q 'boot2docker'; then
    echo "boot2docker"
  elif uname -r | grep -q 'moby'; then
    echo "moby"
  else
    echo "native"
  fi
}

get_docker_host_ip() {
  NETWORK_IF="eth0"
  INSTALL_TYPE=$(get_docker_install_type)
  if [ "${INSTALL_TYPE}" = "boot2docker" ]; then
    NETWORK_IF="eth1"
  fi

  docker run --rm --net host \
            alpine sh -c \
            "ip a show ${NETWORK_IF}" | \
            grep 'inet ' | \
            cut -d/ -f1 | \
            awk '{ print $2}'
}

get_che_hostname() {
  INSTALL_TYPE=$(get_docker_install_type)
  CHE_IP=$(get_docker_host_ip)

  if [ "${INSTALL_TYPE}" = "boot2docker" ]; then
    echo "${CHE_IP}"
  elif [[ "${INSTALL_TYPE}" = "moby" && "${CHE_IP}" = "10.0.75.2" ]]; then
    echo "${CHE_IP}"
  else
    echo "localhost"
  fi
}

check_docker() {
  if [ ! -S /var/run/docker.sock ]; then
    error_exit "Docker socket (/var/run/docker.sock) hasn't be mounted \
inside the container. Verify the syntax of the \"docker run\" command."
  fi

  if ! docker ps > /dev/null 2>&1; then
    output=$(docker ps)
    error_exit "Error when running \"docker ps\": ${output}"
  fi
}

che_container_exist() {
  if [ "$(docker ps -aq  -f "name=${CHE_SERVER_CONTAINER_NAME}" | wc -l)" = "0" ]; then
    return 1
  else
    return 0
  fi
}

che_container_is_running() {
  if [ "$(docker ps -qa -f "status=running" -f "name=${CHE_SERVER_CONTAINER_NAME}" | wc -l)" = "0" ]; then
    return 1
  else
    return 0
  fi
}

che_container_is_stopped() {
  if [ "$(docker ps -qa -f "status=exited" -f "name=${CHE_SERVER_CONTAINER_NAME}" | wc -l)" = "0" ]; then
    return 1
  else
    return 0
  fi
}

wait_until_container_is_running() {
  CONTAINER_START_TIMEOUT=${1}

  ELAPSED=0
  until che_container_is_running || [ ${ELAPSED} -eq "${CONTAINER_START_TIMEOUT}" ]; do
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

server_is_booted() {
  if ! curl -v http://"${CHE_HOST_IP}":"${CHE_PORT}"/dashboard > /dev/null 2>&1 ; then
    return 1
  else
    return 0
  fi
}

wait_until_server_is_booted () {
  SERVER_BOOT_TIMEOUT=${1}

  ELAPSED=0
  until server_is_booted || [ ${ELAPSED} -eq "${SERVER_BOOT_TIMEOUT}" ]; do
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

parse_command_line () {
  if [ $# -eq 0 ]; then
    usage
    exit
  fi

  for command_line_option in "$@"; do
    case ${command_line_option} in
      start|stop|restart|update|info)
        CHE_SERVER_ACTION=${command_line_option}
      ;;
      -h|--help)
        usage
        exit
      ;;
      *)
        # unknown option
        error_exit "You passed an unknown command line option."
      ;;
    esac
  done
}

start_che_server() {
  if che_container_exist; then
    error_exit "A container named \"${CHE_SERVER_CONTAINER_NAME}\" already exists. Please remove it manually (docker rm -f ${CHE_SERVER_CONTAINER_NAME}) and try again."
  fi

  update_che_server

  info "ECLIPSE CHE: CONTAINER STARTING"
  docker run -d --name "${CHE_SERVER_CONTAINER_NAME}" \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v "${CHE_DATA_FOLDER}"/lib:/home/user/che/lib-copy \
    -v "${CHE_DATA_FOLDER}"/workspaces:/home/user/che/workspaces \
    -v "${CHE_DATA_FOLDER}"/storage:/home/user/che/storage \
    -p "${CHE_PORT}":8080 \
    --restart="${CHE_RESTART_POLICY}" \
    --user="${CHE_USER}" ${CHE_CONF_ARGS} \
    "${CHE_SERVER_IMAGE_NAME}":"${CHE_VERSION}" \
                --remote:"${CHE_HOST_IP}" \
                -s:uid \
                run > /dev/null 2>&1

  wait_until_container_is_running 10
  if ! che_container_is_running; then
    error_exit "ECLIPSE CHE: Timeout waiting Che container to start."
  fi

  info "ECLIPSE CHE: SERVER LOGS AT \"docker logs -f che\""
  info "ECLIPSE CHE: SERVER BOOTING..."
  wait_until_server_is_booted 20

  if server_is_booted; then
    info "ECLIPSE CHE: BOOTED AND REACHABLE"
    info "ECLIPSE CHE: http://${CHE_HOSTNAME}:${CHE_PORT}"
  else
    error_exit "ECLIPSE CHE: Timeout waiting Che server to boot. Run \"docker logs che\" to see the logs."
  fi
}

execute_command_with_progress() {
  local progress=$1
  local command=$2
  shift 2
      
  local pid=""
  printf "\n"     

  case "$progress" in
    extended)
      $command "$@"  
      ;;
    basic|*)
      $command "$@" &>/dev/null &
      pid=$!
      while kill -0 "$pid" >/dev/null 2>&1; do
        printf "#"
        sleep 10
      done
      wait $pid # return pid's exit code
      printf "\n"
    ;;
  esac
  printf "\n"     
}

stop_che_server() {
  if ! che_container_is_running; then
    info "-------------------------------------------------------"
    info "ECLIPSE CHE: CONTAINER IS NOT RUNNING. NOTHING TO DO."
    info "-------------------------------------------------------"
  else
    info "ECLIPSE CHE: STOPPING SERVER..."
    docker exec ${CHE_SERVER_CONTAINER_NAME} /home/user/che/bin/che.sh -c stop > /dev/null 2>&1
    sleep 5
    info "ECLIPSE CHE: REMOVING CONTAINER"
    docker rm -f che > /dev/null 2>&1
    info "ECLIPSE CHE: STOPPED"
  fi
}

restart_che_server() {
  if che_container_is_running; then
    stop_che_server
  fi
  start_che_server
}

update_che_server() {
  if [ -z "${CHE_VERSION}" ]; then
    CHE_VERSION=${DEFAULT_CHE_VERSION}
  fi

  info "ECLIPSE CHE: PULLING IMAGE ${CHE_SERVER_IMAGE_NAME}:${CHE_VERSION}"
  execute_command_with_progress extended docker pull ${CHE_SERVER_IMAGE_NAME}:${CHE_VERSION}
  info "ECLIPSE CHE: IMAGE ${CHE_SERVER_IMAGE_NAME}:${CHE_VERSION} INSTALLED"
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u

init_logging
check_docker
init_global_variables
parse_command_line "$@"

case ${CHE_SERVER_ACTION} in
  start)
    start_che_server
  ;;
  stop)
    stop_che_server
  ;;
  restart)
    restart_che_server
  ;;
  update)
    update_che_server
  ;;
  info)
    print_debug_info
  ;;
esac
