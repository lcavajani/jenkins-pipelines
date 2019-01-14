#!/usr/bin/env bash

set -euo pipefail

OPENRC=$1
IMAGE_URL=$2
INSECURE=${3:-""}

#${INSECURE:+--insecure}

source $OPENRC

IMAGE_NAME="$(basename $IMAGE_URL)"
IMAGE_PATH="$(pwd)/$(basename $IMAGE_URL)"
IMAGE_BUILD=$(echo $IMAGE_NAME | sed -n 's/.*\(Build.*\).qcow2/\1/p')
IMAGE_VERSION=$(echo $IMAGE_NAME | sed -n 's/.*CaaSP-\(.*\)\.x86_64-15.0-CaaSP-Stack-OpenStack-Cloud.*/\1/p')

echo "INFO: Checking if we already have this image remotely: $IMAGE_NAME"

if ! openstack ${INSECURE:+--insecure} image list --private | grep -q " $IMAGE_NAME "; then
    echo "INFO: Image does not exist remotely"

    echo "INFO: Checking if we need to download image locally: $IMAGE_PATH"
    if [[ ! -f $IMAGE_PATH ]]; then
        echo "INFO: Image does not exist locally, downloading..."
        echo "INFO: Downloading image: $IMAGE_URL"
        curl -sSL $IMAGE_URL -o $IMAGE_PATH
    else
        echo "INFO: Image found locally, skipping..."
    fi

    echo "INFO: Uploading image: $IMAGE_NAME"
    openstack --insecure image create $IMAGE_NAME --private
        --disk-format qcow2 --container-format bare
        --min-disk 40 --file $IMAGE_NAME \
        --property caasp-version="$IMAGE_VERSION" \
        --property caasp-build="$IMAGE_BUILD"
else
    echo "INFO: Image already exists..."
    openstack ${INSECURE:+--insecure} image show $IMAGE_NAME
    echo "INFO: Skipping upload..."
fi

if [[ -f $IMAGE_PATH ]]; then
    echo "INFO: Cleaning local image"
    rm -vf $IMAGE_PATH
fi
