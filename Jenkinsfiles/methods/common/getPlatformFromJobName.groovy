def call(currentBuild) {
    platforms = ['hyperv', 'kvm', 'openstack', 'vmware', 'xen']
    job = currentBuild.projectName.split('-')
 
    def platform = null
    for (p in platforms) {
        for (m in job) {
            if (m == p) {
              platform = p
              println "INFO: running pipeline on ${platform} platform"
              return platform
            }
        }
    }
}

return this;
