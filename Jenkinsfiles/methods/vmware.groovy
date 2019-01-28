def pushImage(Map jobParams) {
    dir("scripts") {
        timeout(120) {
            withEnv(["VC_USERNAME=${jobParams.credentials.vmware_username}", "VC_PASSWORD=${jobParams.credentials.vmware_password}"]) {
                sh(script: "set -o pipefail; export VC_HOST=${jobParams.platformEndpoint}; ./upload-image-vmware.sh ${jobParams.varFile} ${jobParams.imageSourceUrl} ${WORKSPACE}/caasp-vmware/caasp-vmware.py 2>&1 | tee ${WORKSPACE}/logs/caasp-vmware.log")
            }
        }
    }
}

def createEnvironment(Map jobParams) {
    dir("caasp-vmware") {
        timeout(120) {
            withEnv(["VC_USERNAME=${jobParams.credentials.vmware_username}", "VC_PASSWORD=${jobParams.credentials.vmware_password}"]) {
                sh(script: "set -o pipefail; export VC_HOST=${jobParams.platformEndpoint}; python3 ./caasp-vmware.py deploy --var-file ${jobParams.varFile} --media ${jobParams.image} --stack-name ${jobParams.stackName} --admin-ram ${jobParams.adminRam} --admin-cpu ${jobParams.adminCpu} --master-count ${jobParams.masterCount} --master-ram ${jobParams.masterRam} --master-cpu ${jobParams.masterCpu} --worker-count ${jobParams.workerCount} --worker-ram ${jobParams.workerRam} --worker-cpu ${jobParams.workerCpu} 2>&1 | tee ${WORKSPACE}/logs/caasp-vmware.log")
            }
        }
    
        // Extract state from log file and generate environment.json
        sh(script: "cp -v ./caasp-vmware.state ../../logs")
        // Required as caasp-vmware is not yet in kubic-project/automation
        sh(script: "export SSH_KEY=${WORKSPACE}/automation/misc-files/id_shared && ./tools/generate-environment")
        sh(script: "${WORKSPACE}/automation/misc-tools/generate-ssh-config ./environment.json")
        sh(script: "cp -v environment.json ${WORKSPACE}/environment.json")
        sh(script: "cat ${WORKSPACE}/environment.json")
    }
    archiveArtifacts(artifacts: 'environment.json', fingerprint: true)
}

def destroyEnvironment(Map jobParams) {
    dir("caasp-vmware") {
        withEnv(["VC_USERNAME=${jobParams.credentials.vmware_username}", "VC_PASSWORD=${jobParams.credentials.vmware_password}"]) {
            sh(script: "set -o pipefail; export VC_HOST=${jobParams.platformEndpoint}; python3 ./caasp-vmware.py destroy --var-file ${jobParams.varFile} --stack-name ${jobParams.stackName} 2>&1 | tee ${WORKSPACE}/logs/caasp-vmware.log")
        }
    }
}

return this;
