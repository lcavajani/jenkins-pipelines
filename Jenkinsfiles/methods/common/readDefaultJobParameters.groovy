def call() {
    def paramsFile = './Jenkinsfiles/job_parameters.yaml'
    def defaultJobParametersMap = readYaml file: paramsFile

    println "INFO: default parameters from ${paramsFile}"
    println defaultJobParametersMap

    return defaultJobParametersMap
}

return this;
