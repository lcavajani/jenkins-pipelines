FROM jenkins-swarm/base:stable

ARG USER=jenkins
ARG PYVOMI_VERSION=6.7.0.2018.9
ARG TERRAFORM_VERSION=0.11.1
ARG RUBY_VERSION=2.5
ARG LEAP_VERSION=15.0

# zypper
RUN zypper ref && \
    zypper -n in git curl iproute2 pssh sshpass jq gawk ruby \
        python3 python3-pip which python3-openstackclient && \
    zypper -n install -t pattern devel_basis && \
    zypper clean -a

# openssh
COPY ssh_config /etc/ssh/ssh_config

# pip
RUN pip3 install --no-cache-dir --upgrade pip

RUN pip3 install requests pyyaml pyvmomi==${PYVOMI_VERSION}

# terraform
RUN curl -sSL https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip -o /usr/local/bin/terraform

# velum-interaction requirements
RUN zypper ar https://download.opensuse.org/repositories/devel:/tools/openSUSE_Leap_${LEAP_VERSION}/devel:tools.repo

RUN zypper --gpg-auto-import-keys ref && \
    zypper -n in ruby${RUBY_VERSION}-rubygem-bundler ruby${RUBY_VERSION}-devel phantomjs libxml2-devel libxslt-devel && \
    zypper clean -a

RUN git clone https://github.com/kubic-project/automation && \
    cd automation/velum-bootstrap && \
    sed -i '/^BUNDLE_PATH/d' .bundle/config && \
    bundle install --system && \
    cd ../../ && rm -Rf automation/

USER $USER
