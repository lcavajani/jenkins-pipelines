FROM jenkins-swarm/base
LABEL maintainer="lcavajani@suse.com"

ENV USER=jenkins
ENV LEAP_VERSION=15.0

# zypper
RUN zypper ref && \
    zypper -n in git curl iproute2 pssh sshpass jq tar unzip which gawk ruby \
        python3 python3-pip python3-openstackclient python3-pyaml python3-requests && \
    zypper -n install -t pattern devel_basis && \
    zypper clean -a

# openssh
COPY ssh_config /etc/ssh/ssh_config

# pip
RUN pip3 install --no-cache-dir --upgrade pip

ENV PYVOMI_VERSION=6.7.0.2018.9
RUN pip3 install pyvmomi==${PYVOMI_VERSION}

# terraform
ENV TERRAFORM_VERSION=0.11.1
RUN curl -sSL https://releases.hashicorp.com/terraform/${terraform_version}/terraform_${terraform_version}_linux_amd64.zip -o terraform.zip  && \
    unzip terraform.zip && rm -vf terraform.zip && \
    mv ./terraform /usr/local/bin/terraform && \
    chmod +x /usr/local/bin/terraform

# sonobuoy
ENV SONOBUOY_VERSION=0.11.6
RUN curl -ssL https://github.com/heptio/sonobuoy/releases/download/v${SONOBUOY_VERSION}/sonobuoy_${SONOBUOY_VERSION}_linux_amd64.tar.gz -o sonobuoy.tar.gz && \
    tar zxvf sonobuoy.tar.gz && rm -vf sonobuoy.tar.gz && \
    mv ./sonobuoy /usr/local/bin/sonobuoy && \
    chmod +x /usr/local/bin/sonobuoy

# velum-interaction requirements
ENV RUBY_VERSION=2.5

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
