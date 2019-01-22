//properties([
//    parameters([
//        credentials(name: 'jenkins-api')
//    ])
//])

node('qa-caasp') {
    checkout scm

    def common = load('./Jenkinsfiles/methods/common.groovy')
    def defaultJobParametersMap = common.readDefaultJobParameters()
    def credentials = common.loadCredentialsFromSlave()
    println credentials

    stage('preparation') {
        stage('node Info') {
            common.nodeInfo()
//            withCredentials([usernamePassword(credentialsId: '${jenkins-api}', usernameVariable: 'JENKINS_USER', passwordVariable: 'JENKINS_PASSWORD')]) {
                sh(script: "echo \$JENKINS_USER")
//            }
        }
    }
}
