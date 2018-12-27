def call() {
    sh(script: "mkdir -p ${WORKSPACE}/logs")
    sh(script: "mkdir -p ${WORKSPACE}/{automation,salt,velum}")
    sh(script: "mkdir -p ${WORKSPACE}/{caasp-container-manifests,caasp-services}")
}

return this;
