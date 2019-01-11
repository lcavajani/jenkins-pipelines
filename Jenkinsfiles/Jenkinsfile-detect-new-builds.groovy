// Configure the build properties
properties([
    buildDiscarder(logRotator(numToKeepStr: '15', daysToKeepStr: '31')),
    disableConcurrentBuilds(),
    parameters([
        string(name: 'IMAGES_REPO', defaultValue: 'http://download.suse.de/ibs/Devel:/CASP:/Head:/ControllerNode/images-sle15/', description: 'URL of the image download repository'),
        //string(name: 'RESULTS_REPO', defaultValue: 'gitlab@gitlab.suse.de:lcavajani/caasp-builds.git', description: 'Git repository to store the results of availabe builds')
        string(name: 'RESULTS_REPO', defaultValue: 'git@github.com:lcavajani/caasp-builds.git', description: 'Git repository to store the results of availabe builds'),

        string(name: 'JOB_CI_FILE', defaultValue: 'detect-new-builds_trigger-jobs.yaml', description: 'CI job configuration file for trigger_jenkins_jobs script'),
        booleanParam(name: 'DRY_RUN', defaultValue: false, description: 'Use dry-run mode when launching the jobs'),

        booleanParam(name: 'WORKSPACE_CLEANUP', defaultValue: true, description: 'Cleanup workspace once done ?')
    ])
])

// manage file name in parameters file

def jobParametersMap = [
    imagesRepo: params.get('IMAGES_REPO'),
    resultsGitRepo: params.get('RESULTS_REPO'),
    jobsCiFile: params.get('JOB_CI_FILE'),
    triggerJobMode: 'auto',
    triggerJobDryRun: params.get('DRY_RUN'),
    workspaceCleanup: params.get('WORKSPACE_CLEANUP'),
    // TODO: change
    branchName: 'master'
]

node {
    checkout scm

    def common = load('./Jenkinsfiles/methods/common.groovy')
    def defaultJobParametersMap = common.readDefaultJobParameters()

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
                checkout([$class: 'GitSCM', branches: [[name: "*/${jobParametersMap.branchName}"]],
                userRemoteConfigs: [[credentialsId: defaultJobParametersMap.git.credentials_id, url: jobParametersMap.resultsGitRepo]],
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
    //    withCredentials([sshUserPrivateKey(credentialsId: jobParametersMap.credentialsId, keyFileVariable: "SSH_KEY_PATH")]) {


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
