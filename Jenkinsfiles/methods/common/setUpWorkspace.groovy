def call(jobParametersMap) {
    sh(script: "mkdir -p ${WORKSPACE}/logs")

    // required since it is not yet in kubic-project/automation
    if (jobParametersMap.platform == 'vmware') {
        sh(script: "mkdir -p ${WORKSPACE}/caasp-vmware")
        dir("caasp-vmware") {
             checkout([$class: 'GitSCM', branches: [[name: "*/master"]],
             userRemoteConfigs: [[url: ('https://github.com/lcavajani/caasp-vmware.git')]], extensions: [[$class: 'CleanCheckout']]])
        }
    }
}

return this;
