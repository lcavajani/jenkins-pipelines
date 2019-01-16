def call(Map jobParams) {
    timeout(10) {
        dir('automation/misc-tools') {
            sh(script: "python3 ./wait-for-velum https://\$(jq '.minions[0].addresses.publicIpv4' -r ${WORKSPACE}/environment.json) --timeout 5")
        }
    }

    timeout(90) {
        dir('automation/velum-bootstrap') {
            sh(script: 'sed -i \'/^BUNDLE_PATH/d\' ./.bundle/config')
        }
    }

    timeout(220) {
        dir('automation/velum-bootstrap') {
            try {
                if (jobParams.chooseCrio) {
                    echo "Choosing cri-o"
                    sh(script: "./velum-interactions --configure --enable-tiller --environment ${WORKSPACE}/environment.json --choose-crio")
                } else {
                    echo "Choosing Docker"
                    sh(script: "./velum-interactions --configure --enable-tiller --environment ${WORKSPACE}/environment.json")
                }

                parallel 'monitor-logs': {
                    sh(script: "${WORKSPACE}/automation/misc-tools/parallel-ssh -e ${WORKSPACE}/environment.json -i ${WORKSPACE}/automation/misc-files/id_shared all -- journalctl -f")
                },
                'bootstrap': {
                    try {
                        sh(script: "./velum-interactions --bootstrap --download-kubeconfig --environment ${WORKSPACE}/environment.json")
                        sh(script: "cp kubeconfig ${WORKSPACE}/kubeconfig")
                    } finally {
                        sh(script: "${WORKSPACE}/automation/misc-tools/parallel-ssh --stop -e ${WORKSPACE}/environment.json -i ${WORKSPACE}/automation/misc-files/id_shared all -- journalctl -f")
                    }
                }
            } finally {
                junit "velum-bootstrap.xml"
                try {
                    archiveArtifacts(artifacts: "screenshots/**")
                    archiveArtifacts(artifacts: "kubeconfig")
                } catch (Exception exc) {
                    echo "Failed to Archive Artifacts"
                }
            }
        }
    }
}

return this;
