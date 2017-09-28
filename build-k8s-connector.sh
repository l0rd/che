#!/usr/bin/env bash

#!/bin/bash

set -e



DEFAULT_CHE_IMAGE_ORG=mariolet
DEFAULT_CHE_IMAGE_TAG=k8s

export CHE_IMAGE_ORG=${CHE_IMAGE_ORG:-${DEFAULT_CHE_IMAGE_ORG}}
export CHE_IMAGE_REPO=${CHE_IMAGE_ORG}/che-server
export CHE_IMAGE_TAG=${CHE_IMAGE_TAG:-${DEFAULT_CHE_IMAGE_TAG}}

mvnche() {
  mvn -Dskip-enforce -Dskip-validate-sources -DskipTests -Dfindbugs.skip -Dgwt.compiler.localWorkers=2 -T 1C $@
}

./dockerfiles/init/modules/k8s/files/scripts/deploy_che.sh -c cleanup

cd plugins/plugin-docker
mvnche install
cd ../..

cd assembly/assembly-wsmaster-war
mvnche install
cd ../..

# Uncomment this to rebuild stacks.json
#
# cd ide
# mv ide/src/main/resources/stacks.json ide/src/main/resources/stacks.json.orig
# cp ide/src/main/resources/stacks.json.centos ide/src/main/resources/stacks.json
# mvnche install
# mv ide/src/main/resources/stacks.json.orig ide/src/main/resources/stacks.json
# cd ../assembly/assembly-ide-war
# mvnche install
# cd ..

cd assembly/assembly-main/
mvnche install
cd ../..

cd dockerfiles/che/
mv Dockerfile Dockerfile.alpine
cp Dockerfile.centos Dockerfile
./build.sh --organization:${CHE_IMAGE_ORG} --tag:${CHE_IMAGE_TAG}
mv Dockerfile.alpine Dockerfile
#docker tag eclipse/che-server:${CHE_IMAGE_TAG} ${CHE_IMAGE_REPO}:${CHE_IMAGE_TAG}

cd ../../dockerfiles/init/modules/k8s/files/scripts/
bash ./deploy_che.sh