# Jenkins server/slaves container images

On Docker host:

Install SUSE CA Certificate Setup

```console
zypper ar --refresh http://download.suse.de/ibs/SUSE:/CA/openSUSE_Leap_15.0/SUSE:CA.repo
zypper in ca-certificates-suse p11-kit-nss-trust
```

Install docker

```console
zypper in docker
```

Add userid 1000 (jenkins) to the docker socket so
Jenkins can start Docker slaves

```console
chown 1000.docker /var/run/docker.sock
```
