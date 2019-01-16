// Get platform from job name 'platform-job'
def PLATFORM = currentBuild.projectName.split('-')[0]

// Configure the build properties
properties([
    buildDiscarder(logRotator(numToKeepStr: '15', daysToKeepStr: '31')),
    //disableConcurrentBuilds(),
    parameters([
        string(name: 'IMAGE', defaultValue: '', description: 'CaaSP image'),
        string(name: 'IMAGE_URL', defaultValue: '', description: 'CaaSP image URL'),

        string(name: 'ADMIN_RAM', defaultValue: '', description: 'Memory of admin node'),
        string(name: 'ADMIN_CPU', defaultValue: '', description: 'vCPU of admin node'),

        string(name: 'MASTER_COUNT', defaultValue: '', description: 'Number of master nodes'),
        string(name: 'MASTER_RAM', defaultValue: '', description: 'Memory of master nodes'),
        string(name: 'MASTER_CPU', defaultValue: '', description: 'vCPU of master nodes'),

        string(name: 'WORKER_COUNT', defaultValue: '', description: 'Number of worker nodes'),
        string(name: 'WORKER_RAM', defaultValue: '', description: 'Memory of worker nodes'),
        string(name: 'WORKER_CPU', defaultValue: '', description: 'vCPU of worker nodes'),

        string(name: 'PLATFORM_ENDPOINT', defaultValue: '', description: 'Endpoint to connect to'),
        string(name: 'CREDENTIALS_ID', defaultValue: '', description: 'Jenkins credentials ID'),

        string(name: 'ADMIN_FLAVOR', defaultValue: '', description: 'OpenStack admin flavor'),
        string(name: 'MASTER_FLAVOR', defaultValue: '', description: 'OpenStack master flavor'),
        string(name: 'WORKER_FLAVOR', defaultValue: '', description: 'OpenStack worker flavor'),

        booleanParam(name: 'CHOOSE_CRIO', defaultValue: false, description: 'Use crio as container engine ?'),
        booleanParam(name: 'ENVIRONMENT_DESTROY', defaultValue: true, description: 'Destroy env once done ? if false, manual action is required'),
        booleanParam(name: 'WORKSPACE_CLEANUP', defaultValue: true, description: 'Cleanup workspace once done ?')
    ])
])

//TODO remove all zypper steps, pssh, velum-interactions

node("docker-${PLATFORM}") {
    checkout scm

    // workaround to get/initialize the parameters available in the job
    if (currentBuild.number == 1) {
        return
    }

    def common = load('./Jenkinsfiles/methods/common.groovy')
    def platform = load("./Jenkinsfiles/methods/${PLATFORM}.groovy")

    def defaultJobParametersMap = common.readDefaultJobParameters()
    def jobParametersMap = common.readJobParameters(PLATFORM, params, defaultJobParametersMap)

    stage('preparation') {
        stage('node Info') {
            common.nodeInfo()
        }

        stage('set up workspace') {
            common.setUpWorkspace(jobParametersMap)
        }

        stage('clone Kubic repos') {
            common.cloneKubicRepos()
        }
    }

    stage('push image') {
        if (!jobParametersMap.imageSourceUrl) {
            echo 'No image source URL provided, skipping task...'
        } else {
            platform.pushImage(jobParametersMap)
        }
    }

    try {
        stage('create environment') {
            platform.createEnvironment(jobParametersMap)
        }

        stage('configure environment') {
            common.configureEnvironment(jobParametersMap)
        }

        stage('run Sonobuoy conformance tests') {
            common.runSonobuoyConformanceTests()
        }
    } catch (Exception exc) {
        // do not delete a failed environment so we can investigate
        jobParametersMap.environmentDestroy = false
        throw exc
    } finally {
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
}
