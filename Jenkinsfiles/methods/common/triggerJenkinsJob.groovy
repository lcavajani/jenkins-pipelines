def call(Map conf, defaultParams) {
    dir('scripts/trigger_jenkins_job') {
        withCredentials([usernamePassword(credentialsId: defaultParameters.jenkins.credentials_id, usernameVariable: 'JENKINS_USER', passwordVariable: 'JENKINS_PASSWORD')]) {
            jenkinsCrumb = sh(returnStdout: true, script: "curl -u \"${JENKINS_USER}:${JENKINS_PASSWORD}\" '${JENKINS_URL}/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)'")

            if (conf.triggerJobMode == 'auto') {
                def resultsDir = "$WORKSPACE/${defaultParams.available_builds.results_dir}"
                def resultsFile = "${resultsDir}/${defaultParams.available_builds.results_file}"

                sh(script: "export JENKINS_CRUMB=${jenkinsCrumb};./trigger_jenkins_job.py --git-directory ${resultsDir} --images-file ${resultsFile} --ci-jobfile ${conf.jobsCiFile} --auto")
            } else {
                sh(script: "export JENKINS_CRUMB=${jenkinsCrumb};./trigger_jenkins_job.py --ci-jobfile ${conf.jobsCiFile} --platform ${conf.platform} -image ${conf.image} --image-url ${conf.imageUrl}  --dry-run")
            }
        }
    }
}

return this;
