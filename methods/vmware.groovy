def pushImage(Map conf) {
    dir("automation/caasp-vmware") {
        timeout(120) {
            withCredentials([usernamePassword(credentialsId: conf.credentialsId, usernameVariable: 'VC_USERNAME', passwordVariable: 'VC_PASSWORD')]) {
                sh(script: "set -o pipefail; export VC_HOST=${conf.platformEndpoint}; python3 ./caasp-vmware.py pushimages --source-media ${conf.imageSourceUrl} 2>&1 | tee ${WORKSPACE}/logs/caasp-vmware.log")
            }
        }
    }
}


def createEnvironment(Map conf) {
    dir("automation/caasp-vmware") {
        timeout(120) {
            withCredentials([usernamePassword(credentialsId: conf.credentialsId, usernameVariable: 'VC_USERNAME', passwordVariable: 'VC_PASSWORD')]) {
                sh(script: "set -o pipefail; export VC_HOST=${conf.platformEndpoint}; python3 ./caasp-vmware.py deploy --media ${conf.image} --stack-name ${conf.stackName} --admin-ram ${conf.adminRam} --admin-cpu ${conf.adminCpu} --master-count ${conf.masterCount} --master-ram ${conf.masterRam} --master-cpu ${conf.masterCpu} --worker-count ${conf.workerCount} --worker-ram ${conf.workerRam} --worker-cpu ${conf.workerCpu} 2>&1 | tee ${WORKSPACE}/logs/caasp-vmware.log")
            }
        }
    
        // Extract state from log file and generate environment.json
        sh(script: "cp ./caasp-vmware.state ../../logs")
        sh(script: "./tools/generate-environment")
        sh(script: "../misc-tools/generate-ssh-config ./environment.json")
        sh(script: "cp environment.json ${WORKSPACE}/environment.json")
        sh(script: "cat ${WORKSPACE}/environment.json")
    }
    archiveArtifacts(artifacts: 'environment.json', fingerprint: true)
}

def destroyEnvironment(Map conf) {
    withCredentials([usernamePassword(credentialsId: conf.credentialsId, usernameVariable: 'VC_USERNAME', passwordVariable: 'VC_PASSWORD')]) {
        sh(script: "set -o pipefail; export VC_HOST=${conf.platformEndpoint}; python3 ./caasp-vmware.py destroy --stack-name ${conf.stackName} 2>&1 | tee ${WORKSPACE}/logs/caasp-vmware.log")
    }
}

return this;
