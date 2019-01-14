def call(PLATFORM, Map params, Map defaultParams) {
    def p = params
    def dp = defaultParams

    //def get_val = { String param_val, String default_val -> 
    //        if (param_val == "") {
    //            return param_val
    //        } else {
    //            return default_val
    //        }
    //    }

    def parametersMap = [
        platform: PLATFORM,
        platformEndpoint: (p.get('PLATFORM_ENDPOINT') == '') ? dp.get(PLATFORM).platform_endpoint : p.get('PLATFORM_ENDPOINT'),
        credentialsId: (p.get('CREDENTIALS_ID') == '') ? dp.get(PLATFORM).credentials_id : p.get('CREDENTIALS_ID'),
        stackName: "${JOB_NAME}-${BUILD_NUMBER}".replace('/', '-'),
        // TODO: change
        branchName: 'master',

        image: p.get('IMAGE'),
        imageSourceUrl: p.get('IMAGE_URL'),

        adminRam: (p.get('ADMIN_RAM') == '') ? dp.default.admin_ram : p.get('ADMIN_RAM'),
        adminCpu: (p.get('ADMIN_CPU') == '') ? dp.default.admin_cpu : p.get('ADMIN_CPU'),

        masterRam: (p.get('MASTER_RAM') == '') ? dp.default.master_ram : p.get('MASTER_RAM'),
        masterCpu: (p.get('MASTER_CPU') == '') ? dp.default.master_cpu : p.get('MASTER_CPU'),
        masterCount: (p.get('MASTER_COUNT') == '') ? dp.default.master_count : p.get('MASTER_COUNT'),

        workerRam: (p.get('WORKER_RAM') == '') ? dp.default.worker_ram : p.get('WORKER_RAM'),
        workerCpu: (p.get('WORKER_CPU') == '') ? dp.default.worker_cpu : p.get('WORKER_CPU'),
        workerCount: (p.get('WORKER_COUNT') == '') ? dp.default.worker_count : p.get('WORKER_COUNT'),

        adminFlavor: (p.get('ADMIN_FLAVOR') == '') ? dp.default.admin_flavor : p.get('ADMIN_FLAVOR'),
        masterFlavor: (p.get('MASTER_FLAVOR') == '') ? dp.default.master_flavor : p.get('MASTER_FLAVOR'),
        workerFlavor: (p.get('WORKER_FLAVOR') == '') ? dp.default.worker_flavor : p.get('WORKER_FLAVOR'),
    
        chooseCrio: p.get('CHOOSE_CRIO'),
        environmentDestroy: p.get('ENVIRONMENT_DESTROY'),
        workspaceCleanup: p.get('WORKSPACE_CLEANUP')
    ]

    println "INFO: build parameters"
    println parametersMap

    return parametersMap
}

return this;
