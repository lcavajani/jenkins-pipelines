# Deploy Jenkins Swarm with Podman

1. Install requirements

```console
zypper ar https://download.opensuse.org/repositories/devel:/CaaSP:/Head:/ControllerNode/openSUSE_Leap_15.0/devel:CaaSP:Head:ControllerNode.repo
zypper in podman git make
```

2. Create the configuration directory `/etc/jenkins-swarm`

3. Create configuration file [/etc/jenkins-swarm/config](./config).

```
CONTAINER_NAME=jenkins-swarm-prod
JENKINS_DATA_PATH=/data/jenkins
SECRETS_DIR_PATH=/etc/jenkins-swarm/secrets
ENV_VAR_FILE=/etc/jenkins-swarm/container_env_vars
```

`JENKINS_DATA_PATH`: Directory on the host that will be mounted
as the home directory of the container `/var/jenkins_home`. This
is basically where the workspaces will be created.  

To create the directory on the host, it is required to set
the permissions to allow the jenkins user (uid=1234, gid=5678)
to access the datas.

```console
JENKINS_DATA_PATH=/data/jenkins
mkdir -p $JENKINS_DATA_PATH
chmod 770 $JENKINS_DATA_PATH
chown 1234.5678 $JENKINS_DATA_PATH
```


`SECRETS_DIR_PATH`: Directory that contains the secrets required
in CI.

This file must contain the following secrets:

```
secrets/
├── hyperv_password
├── hyperv_username
├── jenkins_api_password
├── jenkins_api_username
├── jenkins_ssh_privkey
├── openstack_rc
├── vmware_password
└── vmware_username
```


`ENV_VAR_FILE`: Environment variables required by Jenkins Swarm
plugin to connect to the master (ci.suse.de).

An example can be found [here](./container_env_vars).


4. Configure Systemd

Create unit file [/etc/systemd/system/jenkins-swarm.service](./jenkins-swarm.service).

Enable and start the service

```console
systemctl daemon-reload
systemctl enable --now jenkins-swarm.service
```





