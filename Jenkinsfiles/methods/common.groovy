def nodeInfo() {
    def task = load("./Jenkinsfiles/methods/common/nodeInfo.groovy")
    task()
}

def setUpWorkspace() {
    def task = load("./Jenkinsfiles/methods/common/setUpWorkspace.groovy")
    task()
}

def cloneKubicRepos(Map conf) {
    def task = load("./Jenkinsfiles/methods/common/cloneKubicRepos.groovy")
    task(conf)
}

def configureEnvironment(Map conf) {
    def task = load("./Jenkinsfiles/methods/common/configureEnvironment.groovy")
    task(conf)
}

def readDefaultJobParameters() {
    def task = load("./Jenkinsfiles/methods/common/readDefaultJobParameters.groovy")
    task()
}

def readJobParameters(PLATFORM, params, defaultParams) {
    def task = load("./Jenkinsfiles/methods/common/readJobParameters.groovy")
    task(PLATFORM, params, defaultParams)
}

def retrieveAvailableBuilds(Map conf, defaultParams) {
    def task = load("./Jenkinsfiles/methods/common/retrieveAvailableBuilds.groovy")
    task(conf, defaultParams)
}

def runSonobuoyConformanceTests() {
    def task= load("./Jenkinsfiles/methods/common/runSonobuoyConformanceTests.groovy")
    task()
}

def triggerJenkinsJobs(Map conf, defaultParams) {
    def task = load("./Jenkinsfiles/methods/common/triggerJenkinsJobs.groovy")
    task(conf, defaultParams)
}

def workspaceCleanup() {
    def task = load("./Jenkinsfiles/methods/common/workspaceCleanup.groovy")
    task()
}

return this;
