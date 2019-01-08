# Jenkins job builder

TODO: Dockerized
TODO: Automate job creation in Jenkins

Official documentation: https://docs.openstack.org/infra/jenkins-job-builder/index.html

Installation:

```console
$ virtualenv .venv
$ source .venv/bin/activate
$ pip install -r test-requirements.txt -e .
```

## CLI

Test the jobs:

```console
jenkins-jobs --conf jenkins_jobs.ini  --user $USER --password $PASSWORD test pipelines.yaml
```

Update/create the jobs:

```console
jenkins-jobs --conf jenkins_jobs.ini  --user $USER --password $PASSWORD update pipelines.yaml
```

Delete the jobs managed by JJB:

```console
jenkins-jobs --conf jenkins_jobs.ini  --user $USER --password $PASSWORD delete pipelines.yaml
```

Delete all the jobs:

```console
jenkins-jobs --conf jenkins_jobs.ini  --user $USER --password $PASSWORD delete-all
```
