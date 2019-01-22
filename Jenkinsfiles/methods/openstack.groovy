def pushImage(Map jobParams) {
    timeout(120) {
        withEnv(["OPENRC=${jobParams.credentials.openstack_rc}"]) {
            dir('scripts' {
                sh(script: "./upload-image-openstack.sh ${OPENRC} ${jobParams.imageSourceUrl} --insecure 2>&1 | tee ${WORKSPACE}/logs/caasp-openstack.log")
            }
        }
    }
}

def createEnvironment(Map jobParams) {
    timeout(120) {
        dir("automation/caasp-openstack-terraform") {
            withEnv(["OPENRC=${jobParams.credentials.openstack_rc}"]) {
                // TODO: manage terraform init with local plugins
                sh(script: "source ${OPENRC}; set -o pipefail; terraform init 2>&1 | tee ${WORKSPACE}/logs/caasp-openstack.log"
                // TODO: rename/manage network
                sh(script: "source ${OPENRC}; set -o pipefail; terraform apply -var stack_name=${jobParams.stackName} -var image_name=${jobParams.image} -var internal_net=${jobParams.openstack.internalNet} -var external_net=${jobParams.openstack.externalNet} -var admin_size=${jobParams.openstack.adminFlavor} -var masters=${jobParams.masterCount} -var master_size=${jobParams.openstack.masterFlavor} -var workers=${jobParams.workerCount} -var worker_size=${jobParams.openstack.workerFlavor}  -auto-approve 2>&1 | tee ${WORKSPACE}/logs/caasp-openstack.log"
            }
        }
    }
}

def destroyEnvironment(Map jobParams) {
    timeout(30) {
        dir("automation/caasp-openstack-terraform") {
            withEnv(["OPENRC=${jobParams.credentials.openstack_rc}"]) {
                sh(script: "source ${OPENRC}; set -o pipefail; terraform destroy -auto-approve 2>&1 | tee ${WORKSPACE}/logs/caasp-openstack.log"
            }
        }
    }
}

return this;
