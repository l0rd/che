#!/bin/sh

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
trap exit SIGHUP SIGINT SIGTERM

init_global_variables () {

  # For coloring console output
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  NC='\033[0m'

  ### Define various error and usage messages
  USAGE="
Usage:
  `basename "$0"` [COMMAND]
     start                              Starts Che server in new console
     stop                               Stops Che server
     restart                            Restart Che server
     update                             Pull latest version of codenvy/che (restart
                                        is needed to complete update)

Docs: http://eclipse.org/che/getting-started.
"

  DOCKER_HOST_IP=""
  DEBUG=false
  CHE_LIB_VOLUME_PATH="/home/user/che/lib"
  CHE_WS_VOLUME_PATH="/home/user/che/workspaces"
  CHE_STG_VOLUME_PATH="/home/user/che/storage"
  DOCKER_SOCKET="/var/run/docker.sock"
  CHE_SERVER_PORT="8080"
  CHE_IMAGE_VERSION="nightly"
}

usage () {
  echo "${USAGE}"
}

error_exit () {
  echo
  echo "!!!"
  echo -e "!!! ${1}"
  echo "!!!"
  echo
}

find_docker_host_ip() {
  DOCKER_HOST_IP=$(docker run --rm --net host \
                    alpine sh -c \
                    "ip a show eth0" | \
                    grep 'inet ' | \
                    cut -d/ -f1 | \
                    awk '{ print $2}')
}

parse_command_line () {

  if [ $# -eq 0 ]; then
    usage
    return
  fi

  for command_line_option in "$@"; do
    case ${command_line_option} in
      start|stop|restart|update)
        CHE_SERVER_ACTION=${command_line_option}
      ;;
      -h|--help)
        usage
        return
      ;;
      *)
        # unknown option
        error_exit "You passed an unknown command line option."
        return
      ;;
    esac
  done
}


launch_che_server() {
  docker run -d --name che \
    -v $DOCKER_SOCKET:$DOCKER_SOCKET \
    -v $CHE_LIB_VOLUME_PATH:/home/user/che/lib-copy \
    -v $CHE_WS_VOLUME_PATH:$CHE_WS_VOLUME_PATH \
    -v $CHE_STG_VOLUME_PATH:$CHE_STG_VOLUME_PATH \
    -p $CHE_SERVER_PORT:8080 \
    codenvy/che:$CHE_IMAGE_VERSION --remote:$DOCKER_HOST_IP run
}

stop_che_server() {
  docker exec che /home/user/che/bin/che.sh -c stop
}

restart_che_server() {
  stop_che_server
  sleep 5
  docker rm -f che
  launch_che_server
}

update_che_server() {
  docker pull codenvy/che:$CHE_IMAGE_VERSION
}

init_global_variables
parse_command_line "$@"
find_docker_host_ip

case ${CHE_SERVER_ACTION} in
  start)
    launch_che_server
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
esac
