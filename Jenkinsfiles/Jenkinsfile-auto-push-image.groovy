node('qa-caasp') {
    checkout scm

    jobParametersMap << [
        jobsCiFile: params.get('JOB_CI_FILE'),
        triggerJobDryRun: params.get('DRY_RUN')
    ]

    def common = load('./Jenkinsfiles/methods/common.groovy')

    def PLATFORM = params.get('PLATFORM')
    def platform = load("./Jenkinsfiles/methods/${PLATFORM}.groovy")

    def credentials = common.loadCredentialsFromSlave()

    def defaultJobParametersMap = common.readDefaultJobParameters()
    def jobParametersMap = common.readJobParameters(PLATFORM, params, defaultJobParametersMap)
    jobParametersMap.credentials = credentials


    stage('preparation') {
        stage('node Info') {
            common.nodeInfo()
        }

        stage('set up workspace') {
            common.setUpWorkspace(jobParametersMap)
        }

        stage('clone Kubic repos') {
            common.cloneKubicRepos()
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

