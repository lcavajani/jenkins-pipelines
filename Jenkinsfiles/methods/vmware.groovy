def pushImage(Map jobParams) {
    dir("caasp-vmware") {
        timeout(120) {
            withCredentials([usernamePassword(credentialsId: jobParams.credentialsId, usernameVariable: 'VC_USERNAME', passwordVariable: 'VC_PASSWORD')]) {
                try {
                    sh(script: "set -o pipefail; curl -OL ${jobParams.imageSourceUrl} 2>&1 | tee ${WORKSPACE}/logs/caasp-vmware.log")
                    sh(script: "set -o pipefail; export VC_HOST=${jobParams.platformEndpoint}; python3 ./caasp-vmware.py pushimages --source-media ${jobParams.image} 2>&1 | tee ${WORKSPACE}/logs/caasp-vmware.log")
                } finally {
                    sh(script: "set -o pipefail; rm -vf ${jobParams.image} 2>&1 | tee ${WORKSPACE}/logs/caasp-vmware.log")
                }
            }
        }
    }
}


def createEnvironment(Map jobParams) {
    dir("caasp-vmware") {
        timeout(120) {
            withCredentials([usernamePassword(credentialsId: jobParams.credentialsId, usernameVariable: 'VC_USERNAME', passwordVariable: 'VC_PASSWORD')]) {
                sh(script: "set -o pipefail; export VC_HOST=${jobParams.platformEndpoint}; python3 ./caasp-vmware.py deploy --media ${jobParams.image} --stack-name ${jobParams.stackName} --admin-ram ${jobParams.adminRam} --admin-cpu ${jobParams.adminCpu} --master-count ${jobParams.masterCount} --master-ram ${jobParams.masterRam} --master-cpu ${jobParams.masterCpu} --worker-count ${jobParams.workerCount} --worker-ram ${jobParams.workerRam} --worker-cpu ${jobParams.workerCpu} 2>&1 | tee ${WORKSPACE}/logs/caasp-vmware.log")
            }
        }
    
        // Extract state from log file and generate environment.json
        sh(script: "cp -v ./caasp-vmware.state ../../logs")
        sh(script: "./tools/generate-environment")
        sh(script: "../misc-tools/generate-ssh-config ./environment.json")
        sh(script: "cp -v environment.json ${WORKSPACE}/environment.json")
        sh(script: "cat ${WORKSPACE}/environment.json")
    }
    archiveArtifacts(artifacts: 'environment.json', fingerprint: true)
}

def destroyEnvironment(Map jobParams) {
    dir("caasp-vmware") {
        withCredentials([usernamePassword(credentialsId: jobParams.credentialsId, usernameVariable: 'VC_USERNAME', passwordVariable: 'VC_PASSWORD')]) {
            sh(script: "set -o pipefail; export VC_HOST=${jobParams.platformEndpoint}; python3 ./caasp-vmware.py destroy --stack-name ${jobParams.stackName} 2>&1 | tee ${WORKSPACE}/logs/caasp-vmware.log")
        }
    }
}

return this;
