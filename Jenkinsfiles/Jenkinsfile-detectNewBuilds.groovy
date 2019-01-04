// Configure the build properties
properties([
    buildDiscarder(logRotator(numToKeepStr: '15', daysToKeepStr: '31')),
    disableConcurrentBuilds(),
    parameters([
        string(name: 'IMAGES_REPO', defaultValue: 'http://download.suse.de/ibs/Devel:/CASP:/Head:/ControllerNode/images-sle15/', description: 'URL of the image download repository'),
        string(name: 'RESULTS_REPO', defaultValue: 'gitlab@gitlab.suse.de:lcavajani/caasp-builds.git', description: 'Git repository to store the results of availabe builds')
    ])
])

def configurationMap = [
    imagesRepo: params.get('IMAGES_REPO'),
    resultsGitRepo: params.get('RESULTS_REPO'),
    resultsDir: "caasp-builds",
    resultsFile: "files_repo.json",
    branchName: 'master',
    credentialsId: 'jenkins-gitlab'
]

node {
    checkout scm

    def common = load("./methods/common.groovy")

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

        stage('clone caasp-build repos') {
            sh(script: "mkdir -p ${WORKSPACE}/caasp-builds")

            dir("caasp-builds") {
                checkout([$class: 'GitSCM', branches: [[name: "*/${configurationMap.branchName}"]],
                userRemoteConfigs: [[credentialsId: configurationMap.credentialsId, url: configurationMap.resultsGitRepo]],
                extensions: [[$class: 'CleanCheckout']]])
            }
        }
    }

    stage('retrieve available builds') {
        dir("${WORKSPACE}/automation/misc-tools") {
            sh(script: "./list_image_repo.py -u ${configurationMap.imagesRepo} -o ${WORKSPACE}/${configurationMap.resultsDir}/${configurationMap.resultsFile} -d -c ${WORKSPACE}/${configurationMap.resultsDir}")
        }
    }

    stage('list changed files') {
        dir("${WORKSPACE}/caasp-builds") {
            sh(script: "git add *")
            sh(script: "git status --short  | awk '{ print $2 }'")
        }
    }
    //stage('push results to GitLab') {
    //    withCredentials([sshUserPrivateKey(credentialsId: configurationMap.credentialsId, keyFileVariable: "SSH_KEY_PATH")]) {


    //        sh(script: "git --no-pager diff --name-only \$GIT_PREVIOUS_COMMIT \$GIT_COMMIT")
    //    }
    //}
}
