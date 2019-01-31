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
            sh(script: "git config --global user.email 'jenkinsci@caasp.suse'")
            sh(script: "git config --global user.name 'Jenkins CI'")
            sh(script: "git clone ${jobParametersMap.resultsGitRepo} ${defaultJobParametersMap.available_builds.results_dir}")
        }
    }

    stage('retrieve available builds') {
        common.retrieveAvailableBuilds(jobParametersMap, defaultJobParametersMap)
    }

    stage('trigger jobs') {
        common.triggerJenkinsJobs(jobParametersMap, defaultJobParametersMap)
    }

    stage('push results to git') {
        dir("${defaultJobParametersMap.available_builds.results_dir}") {
            sh(script: "ls -la")
            sh(script: "git add ./*CaaSP-Stack*.sha256")
            sh(script: "git add ${defaultJobParametersMap.available_builds.results_file}")
            sh(script: "git commit -m \$(date +'%Y%m%d-%H%M%S')")
            sh(script: "git status")
            sh(script: "git push -u origin master")
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
