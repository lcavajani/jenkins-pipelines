node('qa-caasp') {
    checkout scm

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
            platform.pushImage(jobParametersMap)
        }
    }

    try {
        stage('create environment') {
            platform.createEnvironment(jobParametersMap)
        }

        stage('configure environment') {
            common.configureEnvironment(jobParametersMap)
        }

        stage('run Sonobuoy conformance tests') {
            common.runSonobuoyConformanceTests()
        }
    } catch (Exception exc) {
        // do not delete a failed environment so we can investigate
        jobParametersMap.environmentDestroy = false
        throw exc
    } finally {
        stage('destroy environment') {
            if (!jobParametersMap.environmentDestroy) {
                input(message: "Proceed to environment destroy ?")
            }

            platform.destroyEnvironment(jobParametersMap)
        }

        stage('Workspace cleanup') {
            if (jobParametersMap.workspaceCleanup) {
                common.workspaceCleanup()
            } else {
                echo "Skipping Cleanup as request was made to NOT cleanup the workspace"
            }
        }
    }
}
