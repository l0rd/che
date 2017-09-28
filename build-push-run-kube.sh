#!/bin/bash

set -e

cd plugins
mvn clean install -Pfast
cd ../assembly/assembly-main/
mvn clean install
cd ../..

export DOCKER_HUB_USER=mariolet
export CHE_IMAGE_REPO=${DOCKER_HUB_USER}/che-server
export CHE_IMAGE_TAG=kube-$(date "+%Y%m%d%H%M")
bash ./dockerfiles/che/build.sh --organization:${DOCKER_HUB_USER} --tag:${CHE_IMAGE_TAG}

docker push ${CHE_IMAGE_REPO}:${CHE_IMAGE_TAG}
bash ./dockerfiles/init/modules/k8s/files/scripts/deploy_che.sh