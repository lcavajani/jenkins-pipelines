def nodeInfo() {
    def nodeInfo = load("${WORKSPACE}/methods/common/nodeInfo.groovy")
    nodeInfo()
}

def setUpWorkspace() {
    def setUpWorkspace = load("${WORKSPACE}/methods/common/setUpWorkspace.groovy")
    setUpWorkspace()
}

def cloneKubicRepos(Map conf) {
    def cloneKubicRepos = load("${WORKSPACE}/methods/common/cloneKubicRepos.groovy")
    cloneKubicRepos(conf)
}

def configureEnvironment(Map conf) {
    def configureEnvironment = load("${WORKSPACE}/methods/common/configureEnvironment.groovy")
    configureEnvironment(conf)
}

def runSonobuoyConformanceTests() {
    def runSonobuoyConformanceTests = load("${WORKSPACE}/methods/common/runSonobuoyConformanceTests.groovy")
    runSonobuoyConformanceTests()
}

def workspaceCleanup() {
    def workspaceCleanup = load("${WORKSPACE}/methods/common/workspaceCleanup.groovy")
    workspaceCleanup()
}

return this;
