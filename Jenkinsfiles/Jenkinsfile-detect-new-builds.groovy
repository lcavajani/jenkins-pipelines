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
    resultsDir: "caasp-builds",
    resultsFile: "files_repo.json",
    jobCiFile: "push-images.yaml",
    branchName: 'master',
    credentialsId: 'jenkins-gitlab'
]

node {
    checkout scm

    def common = load("./Jenkinsfiles/methods/common.groovy")
    def defaultParameters = common.readDefaultJobParameters()

    stage('preparation') {
        stage('node Info') {
            common.nodeInfo()
        }

        stage('set up workspace') {
            common.setUpWorkspace()
        }

        //stage('clone Kubic repos') {
        //    common.cloneKubicRepos(configurationMap)
        //}

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
        //dir('automation/misc-tools') {
        dir('scripts') {
            sh(script: "./list_image_repo.py -u ${configurationMap.imagesRepo} -o ${WORKSPACE}/${configurationMap.resultsDir}/${configurationMap.resultsFile} -d -c ${WORKSPACE}/${configurationMap.resultsDir}")
        }
    }

    stage('trigger jobs') {
        dir('scripts/trigger_jenkins_job') {
            withCredentials([usernamePassword(credentialsId: defaultParameters.jenkins.credentials_id, usernameVariable: 'JENKINS_USER', passwordVariable: 'JENKINS_PASSWORD')]) {
                jenkinsCrumb = sh(returnStdout: true, script: "curl -u \"${JENKINS_USER}:${JENKINS_PASSWORD}\" '${JENKINS_URL}/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)'")
                // TODO: manage ci-job filenames in defaultParameters
                sh(script: "export JENKINS_CRUMB=${jenkinsCrumb};./trigger_jenkins_job.py -f ${WORKSPACE}/${configurationMap.resultsDir}/${configurationMap.resultsFile} -c ${configurationMap.jobCiFile} -d ${WORKSPACE}/${configurationMap.resultsDir} --auto")
            }
        }
    }

    //stage('push results to GitLab') {
    //    withCredentials([sshUserPrivateKey(credentialsId: configurationMap.credentialsId, keyFileVariable: "SSH_KEY_PATH")]) {


    //        sh(script: "git add & commit")
    //    }
    //}
}
