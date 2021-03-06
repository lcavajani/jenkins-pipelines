def call() {
    def dir = '/var/jenkins_home/secrets/'

    def credentials = [
        caasp_regcode: readFile(dir + 'caasp_regcode').trim(),
        hyperv_password: readFile(dir + 'hyperv_password').trim(),
        hyperv_username: readFile(dir + 'hyperv_username').trim(),
        jenkins_api_password: readFile(dir + 'jenkins_api_password').trim(),
        jenkins_api_username: readFile(dir + 'jenkins_api_username').trim(),
        jenkins_ssh_privkey_path: dir + 'jenkins_ssh_privkey',
        openstack_rc_path: dir + 'openstack_rc',
        vmware_password: readFile(dir + 'vmware_password').trim(),
        vmware_username: readFile(dir + 'vmware_username').trim()
    ]

    return credentials
}

return this;
