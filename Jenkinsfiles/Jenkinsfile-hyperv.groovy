// Configure the build properties
def PLATFORM = "hyperv"

properties([
    buildDiscarder(logRotator(numToKeepStr: '15', daysToKeepStr: '31')),
    //disableConcurrentBuilds(),
    parameters([
        string(name: 'IMAGE', defaultValue: 'SUSE-CaaS-Platform-3.0-for-MS-HyperV.x86_64-3.0.0-GM', description: "CaaSP ${PLATFORM} Image To Use"),
        string(name: 'IMAGE_URL', description: "CaaSP ${PLATFORM} Image URL"),

        string(name: 'ADMIN_RAM', description: 'Memory of Admin Node'),
        string(name: 'ADMIN_CPU', description: 'VCPU of Admin Node'),

        string(name: 'MASTER_COUNT', description: 'Number of Master Nodes'),
        string(name: 'MASTER_RAM', description: 'Memory of Master Nodes'),
        string(name: 'MASTER_CPU', description: 'VCPU of Master Nodes'),

        string(name: 'WORKER_COUNT', description: 'Number of Worker Nodes'),
        string(name: 'WORKER_RAM', description: 'Memory of Worker Nodes'),
        string(name: 'WORKER_CPU', description: 'VCPU of Worker Nodes'),

        string(name: 'PLATFORM_ENDPOINT', description: "Endpoint ${PLATFORM} to connect to"),
        string(name: 'CREDENTIALS_ID', description: "Jenkins ${PLATFORM} credentials ID"),

        booleanParam(name: 'CHOOSE_CRIO', defaultValue: false, description: 'Use crio as container engine ?'),
        booleanParam(name: 'ENVIRONMENT_DESTROY', defaultValue: true, description: 'Destroy env once done ? if false, manual action is required'),
        booleanParam(name: 'WORKSPACE_CLEANUP', defaultValue: true, description: 'Cleanup workspace once done ?')
    ])
])

//TODO remove all zypper steps, pssh, velum-interactions

node {
    checkout scm

    def common = load("./Jenkinsfiles/methods/common.groovy")
    def defaultParameters = common.readDefaultJobParameters()
    def configurationMap = common.readJobParameters(PLATFORM, params, defaultParameters)
    def platform = load("./Jenkinsfiles/methods/${PLATFORM}.groovy")

    //stage('preparation') {
    //    stage('node Info') {
    //        common.nodeInfo()
    //    }

    //    stage('set up workspace') {
    //        common.setUpWorkspace()
    //    }

    //    stage('clone Kubic repos') {
    //        common.cloneKubicRepos(configurationMap)
    //    }
    //}

    //stage('push image') {
    //    if (!configurationMap.imageSourceUrl) {
    //        echo 'No image source URL provided, skipping task...'
    //    } else {
    //        platform.pushImage(configurationMap)
    //    }
    //}

    //stage('create environment') {
    //    platform.createEnvironment(configurationMap)
    //}

    //stage('configure environment') {
    //    common.configureEnvironment(configurationMap)
    //}

    //stage('run Sonobuoy conformance tests') {
    //    common.runSonobuoyConformanceTests()
    //}

    //stage('destroy environment') {
    //    if (!configurationMap.environmentDestroy) {
    //        input(message: "Proceed to environment destroy ?")
    //    }

    //    platform.destroyEnvironment(configurationMap)
    //}

    //stage('Workspace cleanup') {
    //    if (configurationMap.workspaceCleanup) {
    //        common.workspaceCleanup()
    //    } else {
    //        echo "Skipping Cleanup as request was made to NOT cleanup the workspace"
    //    }
    //}
}

