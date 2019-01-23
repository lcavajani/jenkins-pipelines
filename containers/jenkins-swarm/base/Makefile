NAME   := jenkins-swarm/base
TAG    := $$(git --no-pager log -1 --pretty=%H $(pwd))
IMG    := ${NAME}:${TAG}
LATEST := ${NAME}:latest

build:
	@podman build -t ${IMG} .
	@podman tag ${IMG} ${LATEST}
