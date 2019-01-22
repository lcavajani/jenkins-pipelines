node('qa-caasp') {
    checkout scm

    def jobParametersMap = [
        imagesRepo: params.get('IMAGES_REPO'),
        resultsGitRepo: params.get('RESULTS_REPO'),
        jobsCiFile: params.get('JOB_CI_FILE'),
        triggerJobMode: params.get('MODE'),
        triggerJobDryRun: params.get('DRY_RUN'),
        workspaceCleanup: params.get('WORKSPACE_CLEANUP'),
    ]

    def common = load('./Jenkinsfiles/methods/common.groovy')
    def defaultJobParametersMap = common.readDefaultJobParameters()

    def credentials = common.loadCredentialsFromSlave()
    jobParametersMap.credentials = credentials

    stage('preparation') {
        stage('node Info') {
            common.nodeInfo()
        }

        stage('set up workspace') {
            common.setUpWorkspace(jobParametersMap)
        }

        stage('clone caasp-build repos') {
            sh(script: "mkdir -p ${WORKSPACE}/caasp-builds")

            dir("caasp-builds") {
                checkout([$class: 'GitSCM', branches: [[name: "*/master"]],
                userRemoteConfigs: [[url: jobParametersMap.resultsGitRepo]],
                extensions: [[$class: 'CleanCheckout']]])
            }
        }
    }

    stage('retrieve available builds') {
        common.retrieveAvailableBuilds(jobParametersMap, defaultJobParametersMap)
    }

    stage('trigger jobs') {
        common.triggerJenkinsJobs(jobParametersMap, defaultJobParametersMap)
    }

    //stage('push results to GitLab') {
    //        sh(script: "git add & commit")
    //    }
    //}

    stage('Workspace cleanup') {
        if (jobParametersMap.workspaceCleanup) {
            common.workspaceCleanup()
        } else {
            echo "Skipping Cleanup as request was made to NOT cleanup the workspace"
        }
    }
}
