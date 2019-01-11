def call() {
    def paramsFile = './jobs_parameter_files/common.yaml'
    def defaultJobParametersMap = readYaml file: paramsFile

    println "INFO: default parameters from ${paramsFile}"
    println defaultJobParametersMap

    return defaultJobParametersMap
}

return this;
