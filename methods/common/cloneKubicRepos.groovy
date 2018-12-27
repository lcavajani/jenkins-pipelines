def call(Map conf) {
    def kubicRepo = 'https://github.com/kubic-project/'

    timeout(5) {
        dir("automation") {
            checkout([$class: 'GitSCM', branches: [[name: "*/${conf.branchName}"]],
            userRemoteConfigs: [[url: (kubicRepo + 'automation.git')]], extensions: [[$class: 'CleanCheckout']]])
        }
        dir("salt") {
            checkout([$class: 'GitSCM', branches: [[name: "*/${conf.branchName}"]],
            userRemoteConfigs: [[url: (kubicRepo + 'salt.git')]], extensions: [[$class: 'CleanCheckout']]])
        }
        dir("velum") {
            checkout([$class: 'GitSCM', branches: [[name: "*/${conf.branchName}"]],
            userRemoteConfigs: [[url: (kubicRepo + 'velum.git')]], extensions: [[$class: 'CleanCheckout']]])
        }
        dir("caasp-container-manifests") {
            checkout([$class: 'GitSCM', branches: [[name: "*/${conf.branchName}"]],
            userRemoteConfigs: [[url: (kubicRepo + 'caasp-container-manifests.git')]], extensions: [[$class: 'CleanCheckout']]])
        }
        dir("caasp-services") {
            checkout([$class: 'GitSCM', branches: [[name: "*/${conf.branchName}"]],
            userRemoteConfigs: [[url: (kubicRepo + 'caasp-services.git')]], extensions: [[$class: 'CleanCheckout']]])
        }
    }
}

return this;
