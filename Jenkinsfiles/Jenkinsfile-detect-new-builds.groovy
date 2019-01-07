// Configure the build properties
properties([
    buildDiscarder(logRotator(numToKeepStr: '15', daysToKeepStr: '31')),
    disableConcurrentBuilds(),
    parameters([
        string(name: 'IMAGES_REPO', defaultValue: 'http://download.suse.de/ibs/Devel:/CASP:/Head:/ControllerNode/images-sle15/', description: 'URL of the image download repository'),
        //string(name: 'RESULTS_REPO', defaultValue: 'gitlab@gitlab.suse.de:lcavajani/caasp-builds.git', description: 'Git repository to store the results of availabe builds')
        string(name: 'RESULTS_REPO', defaultValue: 'git@github.com:lcavajani/caasp-builds.git', description: 'Git repository to store the results of availabe builds')
    ])
])

// manage file name in parameters file

def configurationMap = [
    imagesRepo: params.get('IMAGES_REPO'),
    resultsGitRepo: params.get('RESULTS_REPO'),
    jobsCiFile: 'push-images.yaml',
    triggerJobMode: 'auto',
    // TODO: change
    branchName: 'master'
]

node {
    checkout scm

    def common = load('./Jenkinsfiles/methods/common.groovy')
    def defaultParameters = common.readDefaultJobParameters()

    stage('preparation') {
        stage('node Info') {
            common.nodeInfo()
        }

        stage('set up workspace') {
            common.setUpWorkspace()
        }

        stage('clone caasp-build repos') {
            sh(script: "mkdir -p ${WORKSPACE}/caasp-builds")

            dir("caasp-builds") {
                checkout([$class: 'GitSCM', branches: [[name: "*/${configurationMap.branchName}"]],
                userRemoteConfigs: [[credentialsId: defaultParameters.git.credentials_id, url: configurationMap.resultsGitRepo]],
                extensions: [[$class: 'CleanCheckout']]])
            }
        }
    }

    stage('retrieve available builds') {
        common.retrieveAvailableBuilds(configurationMap, defaultParameters)
    }

    stage('trigger jobs') {
        dir('scripts/trigger_jenkins_job') {
            common.triggerJenkinsJobs(configurationMap, defaultParameters)
        }
    }

    //stage('push results to GitLab') {
    //    withCredentials([sshUserPrivateKey(credentialsId: configurationMap.credentialsId, keyFileVariable: "SSH_KEY_PATH")]) {


    //        sh(script: "git add & commit")
    //    }
    //}
}
