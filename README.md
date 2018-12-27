# Jenkins QA pipelines

## Images

**Shared**

zypper in pssh, jq

velum-interactions --setup 
bundle install --without=travis_ci --system
ruby_version=$(ruby --version | cut -d ' ' -f2 | cut -d '.' -f1-2)
INTERACTION_PACKAGES="ruby${ruby_version}-rubygem-bundler \
                      ruby${ruby_version}-devel \
                      phantomjs \
                      libxml2-devel \
                      libxslt-devel"

**HyperV**


**VMware**

pip3 install --no-cache-dir pyvmomi==6.7.0.2018.9 pyyaml

zypper in genisomage

**OpenStack**

zypper in python-glanceclient python-novaclient terraform

**KVM**

zypper in python-glanceclient python-novaclient terraform-libvirt
