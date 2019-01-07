// Configure the build properties
def PLATFORM = sh(returnStdout: true, script: "echo ${JOB_BASE_NAME} | awk -F'-' '{ print $1 }'")

properties([
    buildDiscarder(logRotator(numToKeepStr: '15', daysToKeepStr: '31')),
    //disableConcurrentBuilds(),
    parameters([
        string(name: 'IMAGE', defaultValue: 'SUSE-CaaS-Platform-3.0-for-MS-HyperV.x86_64-3.0.0-GM', description: "CaaSP ${PLATFORM} image"),
        string(name: 'IMAGE_URL', defaultValue: '', description: "CaaSP ${PLATFORM} image URL"),

        string(name: 'ADMIN_RAM', defaultValue: '', description: 'Memory of admin node'),
        string(name: 'ADMIN_CPU', defaultValue: '', description: 'vCPU of admin node'),

        string(name: 'MASTER_COUNT', defaultValue: '', description: 'Number of master nodes'),
        string(name: 'MASTER_RAM', defaultValue: '', description: 'Memory of master nodes'),
        string(name: 'MASTER_CPU', defaultValue: '', description: 'vCPU of master nodes'),

        string(name: 'WORKER_COUNT', defaultValue: '', description: 'Number of worker nodes'),
        string(name: 'WORKER_RAM', defaultValue: '', description: 'Memory of worker nodes'),
        string(name: 'WORKER_CPU', defaultValue: '', description: 'vCPU of worker nodes'),

        string(name: 'PLATFORM_ENDPOINT', defaultValue: '', description: "Endpoint ${PLATFORM} to connect to"),
        string(name: 'CREDENTIALS_ID', defaultValue: '', description: "Jenkins ${PLATFORM} credentials ID"),

        booleanParam(name: 'CHOOSE_CRIO', defaultValue: false, description: 'Use crio as container engine ?'),
        booleanParam(name: 'ENVIRONMENT_DESTROY', defaultValue: true, description: 'Destroy env once done ? if false, manual action is required'),
        booleanParam(name: 'WORKSPACE_CLEANUP', defaultValue: true, description: 'Cleanup workspace once done ?')
    ])
])

//TODO remove all zypper steps, pssh, velum-interactions

node {
    checkout scm

    def common = load("./Jenkinsfiles/methods/common.groovy")
    def defaultJobParametersMap = common.readDefaultJobParameters()
    def jobParametersMap = common.readJobParameters(PLATFORM, params, defaultJobParametersMap)
    def platform = load("./Jenkinsfiles/methods/${PLATFORM}.groovy")

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
            platform.pushImage(jobParametersMap)
        }
    }

    stage('create environment') {
        platform.createEnvironment(jobParametersMap)
    }

    stage('configure environment') {
        common.configureEnvironment(jobParametersMap)
    }

    stage('run Sonobuoy conformance tests') {
        common.runSonobuoyConformanceTests()
    }

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

