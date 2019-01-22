NAME   := jenkins-swarm/default
TAG    := $$(git log -1 --pretty=%H ./)
IMG    := ${NAME}:${TAG}
LATEST := ${NAME}:latest
 
build:
	@podman build -t ${IMG} .
	@podman tag ${IMG} ${LATEST}
