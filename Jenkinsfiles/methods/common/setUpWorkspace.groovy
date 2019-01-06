def call() {
    sh(script: "mkdir -p ${WORKSPACE}/logs")
}

return this;
