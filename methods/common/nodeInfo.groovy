def call() {
    echo "Node: ${NODE_NAME}"
    echo "Workspace: ${WORKSPACE}"
    sh(script: 'env | sort')
    sh(script: 'ip a')
    sh(script: 'ip r')
    sh(script: 'df -h')
    sh(script: 'cat /etc/resolv.conf')
}

return this;
