FROM jenkins-slave/base:stable

ARG terraform_version=0.11.1
ARG ruby_version=2.5
ARG leap_version=15.0

# zypper
RUN zypper ref && \
    zypper -n in git curl iproute2 pssh sshpass jq gawk ruby python3 python3-pip which && \
    zypper -n install -t pattern devel_basis && \
    zypper clean -a

# pip
RUN pip3 install --no-cache-dir --upgrade pip

RUN pip3 install requests pyyaml

# terraform
RUN curl -sSL https://releases.hashicorp.com/terraform/${terraform_version}/terraform_${terraform_version}_linux_amd64.zip -o /usr/local/bin/terraform

# velum-interaction requirements
RUN zypper ar https://download.opensuse.org/repositories/devel:/tools/openSUSE_Leap_${leap_version}/devel:tools.repo

RUN zypper --gpg-auto-import-keys ref && \
    zypper -n in ruby${ruby_version}-rubygem-bundler ruby${ruby_version}-devel phantomjs libxml2-devel libxslt-devel && \
    zypper clean -a

RUN git clone https://github.com/kubic-project/automation && \
    cd automation/velum-bootstrap && \
    sed -i '/^BUNDLE_PATH/d' .bundle/config && \
    bundle install --system && \
    cd ../../ && rm -f automation/
