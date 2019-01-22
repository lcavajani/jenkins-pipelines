ARG LEAP_VERSION=15.0
FROM opensuse/leap:$LEAP_VERSION
LABEL maintainer="lcavajani@suse.com"

ENV JENKINS_SWARM_VERSION=3.15

ARG USER=jenkins
ARG GROUP=jenkins
ARG UID=1000
ARG GID=1000
ARG JENKINS_HOME=/var/jenkins_home

RUN mkdir -p $JENKINS_HOME \
  && chown $UID:$GID $JENKINS_HOME \
  && groupadd -g $GID $GROUP \
  && useradd -d $JENKINS_HOME -u $UID -g $GID -m -s /bin/bash $USER

RUN zypper ref && \
    zypper -n in curl java-1_8_0-openjdk && \
    zypper clean -a

RUN curl --create-dirs -sSL https://repo.jenkins-ci.org/releases/org/jenkins-ci/plugins/swarm-client/${JENKINS_SWARM_VERSION}/swarm-client-${JENKINS_SWARM_VERSION}.jar -o /usr/share/jenkins/swarm-client-${JENKINS_SWARM_VERSION}.jar \
  && chmod 755 /usr/share/jenkins

COPY jenkins-slave.sh /usr/local/bin/jenkins-slave.sh
RUN chmod +x /usr/local/bin/jenkins-slave.sh

VOLUME $JENKINS_HOME

WORKDIR $JENKINS_HOME

ENTRYPOINT ["/usr/local/bin/jenkins-slave.sh"]
