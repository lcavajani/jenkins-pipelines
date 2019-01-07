def PLATFORM = currentBuild.projectName.split('-')[0]

// Configure the build properties
properties([
    buildDiscarder(logRotator(numToKeepStr: '15', daysToKeepStr: '31')),
    disableConcurrentBuilds(),
    parameters([
        string(name: 'IMAGE', description: "CaaSP ${PLATFORM} Image To Use"),
        string(name: 'IMAGE_URL', description: "CaaSP ${PLATFORM} Image URL"),

        string(name: 'PLATFORM_ENDPOINT', defaultValue: '', description: "Endpoint ${PLATFORM} to connect to"),
        string(name: 'CREDENTIALS_ID', defaultValue: '', description: "Jenkins ${PLATFORM} credentials ID"),

        booleanParam(name: 'WORKSPACE_CLEANUP', defaultValue: true, description: 'Cleanup workspace once done ?')
    ])
])

//TODO remove all zypper steps, pssh, velum-interactions
node {
    checkout scm

    def common = load("./Jenkinsfiles/methods/common.groovy")
    def defaultJobParametersMap = common.readDefaultJobParameters()
    def jobParametersMap = common.readJobParameters(PLATFORM, params, defaultJobParametersMap)
    jobParametersMap.jobsCiFile = 'test-images.yaml'
    jobParametersMap.triggerJobDryRun = true

    // workaround to get/initialize the parameters available in the job
    if (currentBuild.number == 1) {
        return
    }

    stage('preparation') {
        stage('node Info') {
            common.nodeInfo()
        }

        stage('set up workspace') {
            common.setUpWorkspace()
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

    stage('trigger jobs') {
        common.triggerJenkinsJobs(jobParametersMap, defaultJobParametersMap)
    }

    stage('Workspace cleanup') {
        if (jobParametersMap.workspaceCleanup) {
            common.workspaceCleanup()
        } else {
            echo "Skipping Cleanup as request was made to NOT cleanup the workspace"
        }
    }
}

