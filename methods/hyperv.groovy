def pushImage(Map conf) {
    timeout(120) {
        withCredentials([usernamePassword(credentialsId: conf.credentialsId, usernameVariable: 'SSHUSER', passwordVariable: 'SSHPASS')]) {
            sh(script: "set -o pipefail; sshpass -e ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \"${SSHUSER}\"@${conf.platformEndpoint} 'Get-ChildItem Env:;git checkout ${BRANCH_NAME}; git pull; caasp-hyperv.ps1 fetchimage -caaspImageSourceUrl ${conf.imageSourceUrl} -nochecksum' 2>&1 | tee ${WORKSPACE}/logs/caasp-hyperv.log")
        }
    }
}

def createEnvironment(Map conf) {
    timeout(120) {
        // https://github.com/PowerShell/Win32-OpenSSH/issues/1049 -> Use SSH password
        withCredentials([usernamePassword(credentialsId: conf.credentialsId, usernameVariable: 'SSHUSER', passwordVariable: 'SSHPASS')]) {
            sh(script: "set -o pipefail; sshpass -e ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \"${SSHUSER}\"@${conf.platformEndpoint} 'Get-ChildItem Env:; git checkout ${conf.branchName}; git pull; caasp-hyperv.ps1 deploy -caaspImage ${conf.image} -stackName ${conf.stackName} -adminRam ${conf.adminRam} -adminCpu ${conf.adminCpu} -masters ${conf.masterCount} -masterRam ${conf.masterRam} -masterCpu ${conf.masterCpu} -workers ${conf.workerCount} -workerRam ${conf.workerRam} -workerCpu ${conf.workerCpu} -Force' 2>&1 | tee ${WORKSPACE}/logs/caasp-hyperv.log")
        }

        // Extract state from log file and generate environment.json
        dir("automation/caasp-hyperv") {
            sh(script: "sed '/^===/,/^===/!d ; /^===.*/d' ${WORKSPACE}/logs/caasp-hyperv.log > ./caasp-hyperv.hvstate ; jq '.' ./caasp-hyperv.hvstate > /dev/null 2>&1")
            sh(script: "cp ./caasp-hyperv.hvstate ../../logs")
            sh(script: "./tools/generate-environment")
            sh(script: "../misc-tools/generate-ssh-config ./environment.json")
            sh(script: "cp environment.json ${WORKSPACE}/environment.json")
            sh(script: "cat ${WORKSPACE}/environment.json")
        }
        archiveArtifacts(artifacts: 'environment.json', fingerprint: true)
    }
}

def destroyEnvironment(Map conf) {
    timeout(30) {
        withCredentials([usernamePassword(credentialsId: conf.credentialsId, usernameVariable: 'SSHUSER', passwordVariable: 'SSHPASS')]) {
            sh(script: "set -o pipefail; sshpass -e ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \"${SSHUSER}\"@${conf.platformEndpoint} 'Get-ChildItem Env:; git checkout ${conf.branchName}; git pull; caasp-hyperv.ps1 destroy -caaspImage ${conf.image} -stackName ${conf.stackName} -masters ${conf.masterCount} -workers ${conf.workerCount} -Force' 2>&1 | tee ${WORKSPACE}/logs/caasp-hyperv.log")
        }
    }
}

return this;
