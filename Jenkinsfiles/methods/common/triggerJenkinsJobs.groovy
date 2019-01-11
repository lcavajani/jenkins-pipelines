def call(Map jobParams, Map defaultParams) {
    dir('scripts/trigger_jenkins_jobs') {
        withCredentials([usernamePassword(credentialsId: defaultParams.jenkins.credentials_id, usernameVariable: 'JENKINS_USER', passwordVariable: 'JENKINS_PASSWORD')]) {
            jenkinsCrumb = sh(returnStdout: true, script: "curl -u \"${JENKINS_USER}:${JENKINS_PASSWORD}\" '${JENKINS_URL}/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)'")

            def dryRunModeParam = (jobParams.triggerJobDryRun) ? '--dry-run' : ''
            def imageUrlParam = (jobParams.imageSourceUrl) ? "--image-url ${jobParams.imageSourceUrl}" : ''
            jobParams.jobsCiFile = "${WORKSPACE}/${defaultParams.default.jobs_parameter_files_dir}/${jobParams.jobsCiFile}"

            if (jobParams.triggerJobMode == 'auto') {
                def resultsDir = "${WORKSPACE}/${defaultParams.available_builds.results_dir}"
                def resultsFile = "${resultsDir}/${defaultParams.available_builds.results_file}"

                sh(script: "export JENKINS_CRUMB=${jenkinsCrumb};./trigger_jenkins_jobs.py --git-directory ${resultsDir} --images-file ${resultsFile} --ci-jobfile ${jobParams.jobsCiFile} --auto ${dryRunModeParam}")
            } else {
                sh(script: "export JENKINS_CRUMB=${jenkinsCrumb};./trigger_jenkins_jobs.py --ci-jobfile ${jobParams.jobsCiFile} --platform ${jobParams.platform} --image ${jobParams.image} ${imageUrlParam} ${dryRunModeParam}") 
            }
        }
    }
}

return this;
