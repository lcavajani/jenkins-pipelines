// Configure the build properties
properties([
    buildDiscarder(logRotator(numToKeepStr: '15', daysToKeepStr: '31')),
    //disableConcurrentBuilds(),
    parameters([
        string(name: 'IMAGE', defaultValue: 'SUSE-CaaS-Platform-3.0-for-MS-HyperV.x86_64-3.0.0-GM', description: 'CaaSP Hyperv Image To Use'),
        string(name: 'IMAGE_URL', defaultValue: '', description: 'CaaSP Hyperv Image URL'),

        string(name: 'ADMIN_RAM', defaultValue: '8192', description: 'Memory of Admin Node'),
        string(name: 'ADMIN_CPU', defaultValue: '4', description: 'VCPU of Admin Node'),

        string(name: 'MASTER_COUNT', defaultValue: '1', description: 'Number of Master Nodes'),
        string(name: 'MASTER_RAM', defaultValue: '4096', description: 'Memory of Master Nodes'),
        string(name: 'MASTER_CPU', defaultValue: '2', description: 'VCPU of Master Nodes'),

        string(name: 'WORKER_COUNT', defaultValue: '2', description: 'Number of Worker Nodes'),
        string(name: 'WORKER_RAM', defaultValue: '2048', description: 'Memory of Worker Nodes'),
        string(name: 'WORKER_CPU', defaultValue: '1', description: 'VCPU of Worker Nodes'),

        string(name: 'PLATFORM_ENDPOINT', defaultValue: '10.84.149.23', description: 'Hyperv endpoint to connect to'),
        string(name: 'CREDENTIALS_ID', defaultValue: 'hvcore-ssh', description: 'Jenkins Hyperv credentials ID for SSH'),

        booleanParam(name: 'CHOOSE_CRIO', defaultValue: false, description: 'Use crio as container engine ?'),
        booleanParam(name: 'ENVIRONMENT_DESTROY', defaultValue: false, description: 'Destroy env once done ? if false, manual action is required'),
        booleanParam(name: 'WORKSPACE_CLEANUP', defaultValue: true, description: 'Cleanup workspace once done ?')


    ])
])

//TODO remove all zypper steps, pssh, velum-interactions

def PLATFORM = "hyperv"

def configurationMap = [
    platformEndpoint: params.get('PLATFORM_ENDPOINT'),
    credentialsId: params.get('CREDENTIALS_ID'),
    stackName: "${JOB_NAME}-${BUILD_NUMBER}".replace("/", "-"),
    branchName: 'master',

    image: params.get('IMAGE'),
    imageSourceUrl: params.get('IMAGE_URL'),
    adminRam: params.get('ADMIN_RAM'),
    adminCpu: params.get('ADMIN_CPU').toInteger(),
    masterRam: params.get('MASTER_RAM'),
    masterCpu: params.get('MASTER_CPU').toInteger(),
    masterCount: params.get('MASTER_COUNT').toInteger(),
    workerRam: params.get('WORKER_RAM'),
    workerCpu: params.get('WORKER_CPU').toInteger(),
    workerCount: params.get('WORKER_COUNT').toInteger(),

    chooseCrio: params.get('CHOOSE_CRIO'),
    environmentDestroy: params.get('ENVIRONMENT_DESTROY'),
    workspaceCleanup: params.get('WORKSPACE_CLEANUP')
]

node {
    checkout scm

    def common = load("${WORKSPACE}/methods/common.groovy")
    def platform = load("${WORKSPACE}/methods/${PLATFORM}.groovy")

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
    }

    stage('push image') {
        if (!configurationMap.imageSourceUrl) {
            echo 'No image source URL provided, skipping task...'
        } else {
            platform.pushImage(configurationMap)
        }
    }

    stage('create environment') {
        platform.createEnvironment(configurationMap)
    }

    stage('configure environment') {
        common.configureEnvironment(configurationMap)
    }

    stage('run Sonobuoy conformance tests') {
        common.runSonobuoyConformanceTests()
    }

    stage('destroy environment') {
        if (!configurationMap.environmentDestroy) {
            input(message: "Proceed to environment destroy ?")
        }

        platform.destroyEnvironment(configurationMap)
    }

    stage('Workspace cleanup') {
        if (configurationMap.workspaceCleanup) {
            common.workspaceCleanup()
        } else {
            echo "Skipping Cleanup as request was made to NOT cleanup the workspace"
        }
    }
}

