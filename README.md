# Jenkins QA pipelines

### Deploy CI

1. run jenkins-server with docker-compose
2. create jobs with jenkins job builder

### Pipeline concepts/workflow

Concepts:

* Platform is automatically defined by the job name, the name must
be named `platform-job` (ex: vmware-auto-conformance)
* The common parameters for the jobs are defined in
`jobs_parameter_files/common.yaml`

There is a visual representation of the
[auto-conformance pipeline](./docs/graph-conformance-pipeline.png)

Main pipeline steps:

1. Define job parameters
2. Load common methods
3. Load platform methods
4. Merge parameters from pipeline and default parameters
5. Run stages


### Project structure

```
├── container
│   ├── jenkins-server
│   └── jenkins-slaves
├── Jenkinsfiles
│   └── methods
│       ├── common
│       ├── common.groovy
│       ├── hyperv.groovy
│       ├── openstack.groovy
│       └── vmware.groovy
├── jenkins_job_builder
├── jobs_parameter_files
└── scripts
```

**container**: Everything related to containers.

**Jenkinsfiles**: Jenkinsfiles definitions and the methods
used in CI (common, platform...).

**Jenkinsfiles/common.groovy**: "list" of available common methods
which point to the files in Jenkinsfiles/common/

**Jenkinsfiles/{hyperv,kvm,openstack,vmware,xen}.groovy**: Available
methods for a give platforms.

**jenkins_job_builder**: Defintions of jobs to create
with Jenkins Job Builder (JJB).

**job_parameter_files**: Configuration/parameter files
that can be consumed by Jenkinsfiles.

**scripts**: Contains scripts that can be used in CI.


### Involved steps to add a new platform

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
