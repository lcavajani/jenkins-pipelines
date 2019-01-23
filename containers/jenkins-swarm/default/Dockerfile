FROM jenkins-swarm/base
LABEL maintainer="lcavajani@suse.com"

ARG USER=jenkins
ARG LEAP_VERSION=15.0

# zypper
RUN zypper ref && \
    zypper -n in git curl iproute2 pssh sshpass jq which gawk ruby \
        python3 python3-pip python3-openstackclient python3-pyaml python3-requests && \
    zypper -n install -t pattern devel_basis && \
    zypper clean -a

# openssh
COPY ssh_config /etc/ssh/ssh_config

# pip
RUN pip3 install --no-cache-dir --upgrade pip

ARG PYVOMI_VERSION=6.7.0.2018.9
RUN pip3 install pyvmomi==${PYVOMI_VERSION}

# terraform
ARG TERRAFORM_VERSION=0.11.1
RUN curl -sSL https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip -o /usr/local/bin/terraform && chmod +x /usr/local/bin/terraform

# sonobuoy
ARG SONOBUOY_VERSION=0.11.6
RUN curl -ssL https://github.com/heptio/sonobuoy/releases/download/v${SONOBUOY_VERSION}/sonobuoy_${SONOBUOY_VERSION}_linux_amd64.tar.gz -o /usr/local/bin/sonobuoy && chmod +x /usr/local/bin/sonobuoy

# velum-interaction requirements
ARG RUBY_VERSION=2.5

RUN zypper ar https://download.opensuse.org/repositories/devel:/tools/openSUSE_Leap_${LEAP_VERSION}/devel:tools.repo && \
    zypper --gpg-auto-import-keys ref && \
    zypper -n in ruby${RUBY_VERSION}-rubygem-bundler ruby${RUBY_VERSION}-devel phantomjs libxml2-devel libxslt-devel && \
    zypper clean -a

RUN git clone https://github.com/kubic-project/automation && \
    cd automation/velum-bootstrap && \
    sed -i '/^BUNDLE_PATH/d' .bundle/config && \
    bundle install --system && \
    cd ../../ && rm -Rf automation/

USER $USER
