def PLATFORM = "vmware"

// Configure the build properties
properties([
    buildDiscarder(logRotator(numToKeepStr: '15', daysToKeepStr: '31')),
    disableConcurrentBuilds(),
    parameters([
        string(name: 'IMAGE', defaultValue: '', description: "CaaSP ${PLATFORM} Image To Use"),
        string(name: 'IMAGE_URL', defaultValue: '', description: "CaaSP ${PLATFORM} Image URL"),

        string(name: 'PLATFORM_ENDPOINT', description: "Endpoint ${PLATFORM} to connect to"),
        string(name: 'CREDENTIALS_ID', description: "Jenkins ${PLATFORM} credentials ID"),

        booleanParam(name: 'WORKSPACE_CLEANUP', defaultValue: true, description: 'Cleanup workspace once done ?')
    ])
])

//TODO remove all zypper steps, pssh, velum-interactions
node {
    checkout scm

    def common = load("./Jenkinsfiles/methods/common.groovy")
    def defaultJobParametersMap = common.readDefaultJobParameters()
    def jobParametersMap = common.readJobParameters(PLATFORM, params, defaultJobParametersMap)

    stage('preparation') {
        stage('node Info') {
            common.nodeInfo()
        }

        stage('set up workspace') {
            common.setUpWorkspace()

            // SPECIFIC, should be changed when merged
            sh(script: "mkdir -p ${WORKSPACE}/caasp-vmware")
            dir("caasp-vmware") {
                checkout([$class: 'GitSCM', branches: [[name: "*/master"]],
                userRemoteConfigs: [[url: ('https://github.com/lcavajani/caasp-vmware.git')]], extensions: [[$class: 'CleanCheckout']]])
            }
            sh(script: "cp -Rf ${WORKSPACE}/caasp-vmware ${WORKSPACE}/automation/")
        }

        stage('clone Kubic repos') {
            common.cloneKubicRepos(jobParametersMap)
        }
    }

    stage('push image') {
        if (!jobParametersMap.imageSourceUrl) {
            echo 'No image source URL provided, skipping task...'
        } else {
            echo jobParametersMap.image
            echo jobParametersMap.imageSourceUrl
            //platform.pushImage(jobParametersMap)
        }
    }

    stage('Workspace cleanup') {
        if (jobParametersMap.workspaceCleanup) {
            common.workspaceCleanup()
        } else {
            echo "Skipping Cleanup as request was made to NOT cleanup the workspace"
        }
    }
}

