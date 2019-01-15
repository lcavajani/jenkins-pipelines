FROM jenkins-slave/base:stable

ARG terraform_version=0.11.1

RUN zypper ref && \
    zypper -n in git curl iproute2 pssh sshpass jq gawk ruby python3 python3-pip which && \
    zypper clean -a

RUN pip3 install --no-cache-dir --upgrade pip

RUN pip3 install requests pyyaml

RUN curl -sSL https://releases.hashicorp.com/terraform/${terraform_version}/terraform_${terraform_version}_linux_amd64.zip -o /usr/local/bin/terraform

