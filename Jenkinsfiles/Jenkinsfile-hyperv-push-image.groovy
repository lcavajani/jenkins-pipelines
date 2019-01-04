def PLATFORM = "hyperv"

// Configure the build properties
properties([
    buildDiscarder(logRotator(numToKeepStr: '15', daysToKeepStr: '31')),
    disableConcurrentBuilds(),
    parameters([
        string(name: 'IMAGE', description: "CaaSP ${PLATFORM} Image To Use"),
        string(name: 'IMAGE_URL', description: "CaaSP ${PLATFORM} Image URL"),

        string(name: 'PLATFORM_ENDPOINT', description: "Endpoint ${PLATFORM} to connect to"),
        string(name: 'CREDENTIALS_ID', description: "Jenkins ${PLATFORM} credentials ID"),

        booleanParam(name: 'WORKSPACE_CLEANUP', defaultValue: true, description: 'Cleanup workspace once done ?')
    ])
])

//TODO remove all zypper steps, pssh, velum-interactions
node {
    checkout scm

    def common = load("./Jenkinsfiles/methods/common.groovy")
    def defaultParameters = common.readDefaultJobParameters()
    def configurationMap = common.readJobParameters(PLATFORM, params, defaultParameters)

    stage('preparation') {
        stage('node Info') {
            common.nodeInfo()
        }

        stage('set up workspace') {
            common.setUpWorkspace()
        }

        stage('clone Kubic repos') {
            common.cloneKubicRepos(configurationMap)
        }
    }

    stage('push image') {
        if (!configurationMap.imageSourceUrl) {
            echo 'No image source URL provided, skipping task...'
        } else {
            echo configurationMap.image
            echo configurationMap.imageSourceUrl
            //platform.pushImage(configurationMap)
        }
    }

    stage('trigger jobs') {
        dir('scripts/trigger_jenkins_job') {
            withCredentials([usernamePassword(credentialsId: defaultParameters.jenkins.credentials_id, usernameVariable: 'JENKINS_USER', passwordVariable: 'JENKINS_PASSWORD')]) {
                jenkinsCrumb = sh(returnStdout: true, script: "curl -u \"${JENKINS_USER}:${JENKINS_PASSWORD}\" '${JENKINS_URL}/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)'")
                println jenkinsCrumb
                // TODO: manage ci-job filenames
                sh(script: "./trigger_jenkins_job.py -c test-images.yaml -p ${PLATFORM} -i ${configurationMap.image} -u ${configurationMap.imageSourceUrl} --dry-run")
            }
        }
    }

    stage('Workspace cleanup') {
        if (configurationMap.workspaceCleanup) {
            common.workspaceCleanup()
        } else {
            echo "Skipping Cleanup as request was made to NOT cleanup the workspace"
        }
    }
}

