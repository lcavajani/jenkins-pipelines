def call() {
    try {
        cleanWs()
    } catch (Exception exc) {
        echo "Failed to clean workspace"
    }
}

return this;
