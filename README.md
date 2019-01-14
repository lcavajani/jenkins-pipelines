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



* use empty default valus in pipeline to avoid null param


rename params to jobParams

### Involved step to add a new platform

**Jenkins methods**

Create groovy deployment file: `Jenkinsfiles/methods/openstack.groovy`

```groovy
def pushImage(Map jobParams) {}
def createEnvironment(Map jobParams) {}
def destroyEnvironment(Map jobParams) {}
```


Add new parameters in parmeters Map: Jenkinsfiles/methods/common/readJobParameters.groovy

```groovy
adminFlavor: (p.get('ADMIN_FLAVOR') == '') ? dp.default.admin_flavor : p.get('ADMIN_FLAVOR'),
masterFlavor: (p.get('MASTER_FLAVOR') == '') ? dp.default.master_flavor : p.get('MASTER_FLAVOR'),
workerFlavor: (p.get('WORKER_FLAVOR') == '') ? dp.default.worker_flavor : p.get('WORKER_FLAVOR'),
```

**Docker slave container**

Create container image: `container/jenkins-slaves/openstack/Dockerfile`

```
FROM jenkins-slave/default:stable

# Install openstack requirements
RUN zypper ref && \
    zypper -n in python3-openstackclient && \
    zypper clean -a
```

Add a docker slave for the platform: `container/jenkins-server/jenkins.yaml`

```yaml
  clouds:
    - docker:
        name: "docker"
        dockerApi:
          dockerHost:
            uri: "unix:///var/run/docker.sock"
        templates:
          - labelString: "docker-openstack"
            dockerTemplateBase:
              image: "jenkins-slave/openstack:stable"
            pullStrategy: "PULL_NEVER"
            remoteFs: "/home/jenkins/"
            connector:
              attach:
                user: "jenkins"
            mode: EXCLUSIVE
            instanceCapStr: "5"
```

**Jenkins credentials**

Create docker secret to mount in instance with docker-compose: `container/jenkins-server/docker-compose.yaml`

```yaml
services:
  jenkins-server:
    [...]
    secrets:
      - openstack-openrc

secrets:
  openstack-openrc:
    file: ./secrets/openstack-openrc
```

Create credentials in Jenkins: `container/jenkins-server/jenkins.yaml`

```yaml
credentials:
  system:
    domainCredentials:
      - credentials:
        - file:
              scope: GLOBAL
              id: "ecp-openrc"
              fileName: ${openstack-openrc}
              description: "Username/Password Credentials for Engineering Cloud in Provo"
```

**Jenkins pipeline**

Add default values: `jobs_parameter_files/common.yaml`

```yaml
openstack:
  platform_endpoint: "https://engcloud.prv.suse.net:5000/v3"
  credentials_id: "ecp-openrc"
  internal_net: "caasp-qa-ci-net"
  external_net: "floating"
```

**Jenkins Job Builder**

Add platform jobs: `jenkins_job_builder/pipelines.yaml`

```yaml
# OpenStack
- project:
    name: openstack
    jobs:
      - '{name}-push-image'
      - '{name}-{setup}-conformance':
          setup: 1m2w
      - '{name}-{setup}-conformance':
          setup: 3m2w
```

**Misc**

Add a script to upload an image to OpenStack: `scripts/upload-image-openstack.sh`
