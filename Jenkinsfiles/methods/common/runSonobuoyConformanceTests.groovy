def call() {
    dir("${WORKSPACE}/automation/k8s-e2e-tests") {
        try {
            sh(script: "./e2e-tests --kubeconfig ${WORKSPACE}/kubeconfig")
        } finally {
            archiveArtifacts(artifacts: "results/**")
            junit("results/plugins/e2e/results/*.xml")
        }
    }
}

return this;
