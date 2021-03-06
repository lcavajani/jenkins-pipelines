def nodeInfo() {
    def task = load("${WORKSPACE}/Jenkinsfiles/methods/common/nodeInfo.groovy")
    task()
}

def setUpWorkspace(jobParametersMap) {
    def task = load("${WORKSPACE}/Jenkinsfiles/methods/common/setUpWorkspace.groovy")
    task(jobParametersMap)
}

def cloneKubicRepos() {
    def task = load("${WORKSPACE}/Jenkinsfiles/methods/common/cloneKubicRepos.groovy")
    task()
}

def configureEnvironment(Map jobParams) {
    def task = load("${WORKSPACE}/Jenkinsfiles/methods/common/configureEnvironment.groovy")
    task(jobParams)
}

def getPlatformFromJobName(currentBuild) {
    def task = load("${WORKSPACE}/Jenkinsfiles/methods/common/getPlatformFromJobName.groovy")
    task(currentBuild)
}

def loadCredentialsFromSlave() {
    def task = load("${WORKSPACE}/Jenkinsfiles/methods/common/loadCredentialsFromSlave.groovy")
    task()
}

def readDefaultJobParameters() {
    def task = load("${WORKSPACE}/Jenkinsfiles/methods/common/readDefaultJobParameters.groovy")
    task()
}

def readJobParameters(PLATFORM, Map params, Map defaultParams) {
    def task = load("${WORKSPACE}/Jenkinsfiles/methods/common/readJobParameters.groovy")
    task(PLATFORM, params, defaultParams)
}

def retrieveAvailableBuilds(Map jobParams, Map defaultParams) {
    def task = load("${WORKSPACE}/Jenkinsfiles/methods/common/retrieveAvailableBuilds.groovy")
    task(jobParams, defaultParams)
}

def runSonobuoyConformanceTests() {
    def task= load("${WORKSPACE}/Jenkinsfiles/methods/common/runSonobuoyConformanceTests.groovy")
    task()
}

def triggerJenkinsJobs(Map jobParams, Map defaultParams) {
    def task = load("${WORKSPACE}/Jenkinsfiles/methods/common/triggerJenkinsJobs.groovy")
    task(jobParams, defaultParams)
}

def workspaceCleanup() {
    def task = load("${WORKSPACE}/Jenkinsfiles/methods/common/workspaceCleanup.groovy")
    task()
}

return this;
