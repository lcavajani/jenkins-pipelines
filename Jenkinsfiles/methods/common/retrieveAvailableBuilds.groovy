def call(Map conf, defaultParams) {
    dir('scripts') {
        def resultsDir = "${WORKSPACE}/${defaultParams.available_builds.results_dir}"
        def resultsFile = "${resultsDir}/${defaultParams.available_builds.results_file}"

        sh(script: "./list_image_repo.py --url ${conf.imagesRepo} --results-file ${resultsFile} --download-checksum --checksum-dir ${resultsDir}")
    }
}

return this;
