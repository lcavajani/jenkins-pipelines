def call() {
    def paramsFile = './Jenkinsfiles/job_parameters.yaml'
    def defaultParameters = readYaml file: paramsFile

    println "INFO: default parameters from ${paramsFile}"
    println defaultParameters

    return defaultParameters
}

return this;
