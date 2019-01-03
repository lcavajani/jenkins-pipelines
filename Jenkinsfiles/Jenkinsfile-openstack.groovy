// Configure the build properties
properties([
    buildDiscarder(logRotator(numToKeepStr: '15', daysToKeepStr: '31')),
    //disableConcurrentBuilds(),
    parameters([
        string(name: 'IMAGE', defaultValue: 'SUSE-CaaS-Platform-3.0-for-OpenStack-Cloud.x86_64-3.0.0-GM.qcow2', description: 'CaaSP OpenStack Image To Use'),
        string(name: 'IMAGE_URL', defaultValue: '', description: 'CaaSP OpenStack Image URL'),

        string(name: 'ADMIN_FLAVOR', defaultValue: 'm1.large', description: 'Flavor of Admin Node'),

        string(name: 'MASTER_COUNT', defaultValue: '1', description: 'Number of Master Nodes'),
        string(name: 'MASTER_FLAVOR', defaultValue: 'm1.large', description: 'Flavor of Master Nodes'),

        string(name: 'WORKER_COUNT', defaultValue: '2', description: 'Number of Worker Nodes'),
        string(name: 'WORKER_FLAVOR', defaultValue: 'm1.medium', description: 'Flavor of Worker Nodes'),

        booleanParam(name: 'CHOOSE_CRIO', defaultValue: false, description: 'Use crio as container engine ?'),
        booleanParam(name: 'ENVIRONMENT_DESTROY', defaultValue: false, description: 'Destroy env once done ? if false, manual action is required'),
        booleanParam(name: 'WORKSPACE_CLEANUP', defaultValue: true, description: 'Cleanup workspace once done ?')


    ])
])

//TODO remove all zypper steps, pssh, velum-interactions

def kubicRepo = 'https://github.com/kubic-project/'
def stackName = "${JOB_NAME}-${BUILD_NUMBER}".replace("/", "-")
def BRANCH_NAME = 'master'


def image = params.get('IMAGE')
def imageSourceUrl = params.get('IMAGE_URL')
def adminFlavor = params.get('ADMIN_FLAVOR')
def masterFlavor = params.get('MASTER_FLAVOR')
def masterCount = params.get('MASTER_COUNT').toInteger()
def workerFlavor = params.get('WORKER_FLAVOR')
def workerCount = params.get('WORKER_COUNT').toInteger()

def chooseCrio = params.get('CHOOSE_CRIO')
def environmentDestroy = params.get('ENVIRONMENT_DESTROY')
def workspaceCleanup = params.get('WORKSPACE_CLEANUP')


node {
    stage('Node Info') {
        echo "Node: ${NODE_NAME}"
        echo "Workspace: ${WORKSPACE}"
        sh(script: 'env | sort')
        sh(script: 'ip a')
        sh(script: 'ip r')
        sh(script: 'df -h')
        sh(script: 'cat /etc/resolv.conf')
    }

    stage('Preparation') {
        stage('Set up workspace') {
            sh(script: "mkdir -p ${WORKSPACE}/logs")
            sh(script: "mkdir -p ${WORKSPACE}/{automation,salt,velum}")
            sh(script: "mkdir -p ${WORKSPACE}/{caasp-container-manifests,caasp-services}")
        }

        stage('Clone git repositories') {
            timeout(5) {
                dir("automation") {
                    checkout([$class: 'GitSCM', branches: [[name: "*/${BRANCH_NAME}"]],
                    userRemoteConfigs: [[url: (kubicRepo + 'automation.git')]], extensions: [[$class: 'CleanCheckout']]])
                }
                dir("salt") {
                    checkout([$class: 'GitSCM', branches: [[name: "*/${BRANCH_NAME}"]],
                    userRemoteConfigs: [[url: (kubicRepo + 'salt.git')]], extensions: [[$class: 'CleanCheckout']]])
                }
                dir("velum") {
                    checkout([$class: 'GitSCM', branches: [[name: "*/${BRANCH_NAME}"]],
                    userRemoteConfigs: [[url: (kubicRepo + 'velum.git')]], extensions: [[$class: 'CleanCheckout']]])
                }
                dir("caasp-container-manifests") {
                    checkout([$class: 'GitSCM', branches: [[name: "*/${BRANCH_NAME}"]],
                    userRemoteConfigs: [[url: (kubicRepo + 'caasp-container-manifests.git')]], extensions: [[$class: 'CleanCheckout']]])
                }
                dir("caasp-services") {
                    checkout([$class: 'GitSCM', branches: [[name: "*/${BRANCH_NAME}"]],
                    userRemoteConfigs: [[url: (kubicRepo + 'caasp-services.git')]], extensions: [[$class: 'CleanCheckout']]])
                }
            }
        }
    }

    stage('Push images') {
        if (!imageSourceUrl) {
            echo 'No image source URL provided, skipping task...'
        } else {
            dir("automation/caasp-openstack-terraform") {
                timeout(120) {
                    withCredentials([usernamePassword(credentialsId: 'vcenter-api', usernameVariable: 'VC_USERNAME', passwordVariable: 'VC_PASSWORD')]) {
                        //TODO: configure
                        sh(script: "set -o pipefail; export VC_HOST=${vcenterHost}; glance --help 2>&1 | tee ${WORKSPACE}/logs/caasp-openstack.log")
                    }
                }
            }
        }
    }

    stage('Create environment') {
        dir("automation/caasp-openstack-terraform") {
            timeout(120) {
                withCredentials([file(credentialsId: 'ecp-openrc', variable: 'OPENRC')]) {
                    // TODO: manage networks in Terraform
                    // sh(script: "set -o pipefail; source $OPENRC; openstack --insecure network create --internal --no-share lcavajani-net")
                    // sh(script: "set -o pipefail; source $OPENRC; openstack --insecure subnet create --network lcavajani-net --subnet-range 10.0.0.0/22 --dhcp  lcavajani-subnet")
                    // sh(script: "set -o pipefail; source $OPENRC; openstack --insecure router create --enable  lcavajani-router")
                    // sh(script: "set -o pipefail; source $OPENRC; openstack --insecure router set lcavajani-router --external-gateway floating")
                    // sh(script: "set -o pipefail; source $OPENRC; openstack --insecure router add subnet lcavajani-router lcavajani-subnet")

                    //TODO: manage terraform init locally
                    //TODO: ssh-key creation
                    sh(script: "set -o pipefail; source $OPENRC; [ -d ssh ] || mkdir ssh; if ! [ -f ssh/id_caasp ]; then ssh-keygen -b 2048 -t rsa -f ssh/id_caasp -N \"\";fi")

                    sh(script: "source $OPENRC; set -o pipefail;  terraform init; terraform apply -var auth_url=\$OS_AUTH_URL -var domain_name=\$OS_USER_DOMAIN_NAME -var-file=openstack.tfvars -var region_name=\$OS_REGION_NAME -var project_name=\$OS_PROJECT_NAME -var user_name=\$OS_USERNAME -var password=\$OS_PASSWORD -var identifier=${stackName} -var image_name=${image} -var internal_net=lcavajani-net -var external_net=floating -var admin_size=${adminFlavor} -var masters=${masterCount} -var master_size=${masterFlavor} -var workers=${workerCount} -var worker_size=${workerFlavor}  -auto-approve 2>&1 | tee ${WORKSPACE}/logs/caasp-openstack.log")
                }
            }
        
            // Extract state from log file and generate environment.json
            // sh(script: "cp ./caasp-vmware.state ../../logs")
            // sh(script: "./tools/generate-environment")
            // sh(script: "../misc-tools/generate-ssh-config ./environment.json")
            // sh(script: "cp environment.json ${WORKSPACE}/environment.json")
            // sh(script: "cat ${WORKSPACE}/environment.json")
        }
        // archiveArtifacts(artifacts: 'environment.json', fingerprint: true)
    }

    // stage('Configure environment') {
    //     timeout(300) {
    //         dir('automation/misc-tools') {
    //             sh(script: "python3 ./wait-for-velum https://\$(jq '.minions[0].addresses.publicIpv4' -r ${WORKSPACE}/environment.json) --timeout 5")
    //         }
    //     }

    //     timeout(90) {
    //         dir('automation/velum-bootstrap') {
    //             sh(script: './velum-interactions --setup')
    //         }
    //     }

    //     timeout(220) {
    //         try {
    //             dir('automation/velum-bootstrap') {
    //                 if (chooseCrio) {
    //                     echo "Choosing cri-o"
    //                     sh(script: "./velum-interactions --configure --enable-tiller --environment ${WORKSPACE}/environment.json --choose-crio")
    //                 } else {
    //                     echo "Choosing Docker"
    //                     sh(script: "./velum-interactions --configure --enable-tiller --environment ${WORKSPACE}/environment.json")
    //                 }
    //             }

    //             parallel 'monitor-logs': {
    //                 sh(script: "${WORKSPACE}/automation/misc-tools/parallel-ssh -e ${WORKSPACE}/environment.json -i ${WORKSPACE}/automation/misc-files/id_shared all -- journalctl -f")
    //             },
    //             'bootstrap': {
    //                 try {
    //                     dir('automation/velum-bootstrap') {
    //                         sh(script: "./velum-interactions --bootstrap --download-kubeconfig --environment ${WORKSPACE}/environment.json")
    //                         sh(script: "cp kubeconfig ${WORKSPACE}/kubeconfig")
    //                     }
    //                 } finally {
    //                     sh(script: "${WORKSPACE}/automation/misc-tools/parallel-ssh --stop -e ${WORKSPACE}/environment.json -i ${WORKSPACE}/automation/misc-files/id_shared all -- journalctl -f")
    //                 }
    //             }
    //         } finally {
    //             dir('automation/velum-bootstrap') {
    //                 junit "velum-bootstrap.xml"
    //                 try {
    //                     archiveArtifacts(artifacts: "screenshots/**")
    //                     archiveArtifacts(artifacts: "kubeconfig")
    //                 } catch (Exception exc) {
    //                     echo "Failed to Archive Artifacts"
    //                 }
    //             }
    //         }
    //     }
    // }

    // stage('Run sonobuoy k8s conformance tests') {
    //     dir("${WORKSPACE}/automation/k8s-e2e-tests") {
    //         try {
    //             sh(script: "./e2e-tests --kubeconfig ${WORKSPACE}/kubeconfig")
    //         } finally {
    //             archiveArtifacts(artifacts: "results/**")
    //             junit("results/plugins/e2e/results/*.xml")
    //         }
    //     }
    // }

    stage('Destroy environment') {
        if (!environmentDestroy) {
            input(message: "Proceed to environment destroy ?")
        }
        timeout(30) {
                withCredentials([file(credentialsId: 'ecp-openrc', variable: 'OPENRC')]) {
                    sh(script: "source $OPENRC; set -o pipefail;  terraform init; terraform apply -var auth_url=\$OS_AUTH_URL -var domain_name=\$OS_USER_DOMAIN_NAME -var-file=openstack.tfvars -var region_name=\$OS_REGION_NAME -var project_name=\$OS_PROJECT_NAME -var user_name=\$OS_USERNAME -var password=\$OS_PASSWORD -var identifier=${stackName} -var image_name=${image} -var internal_net=lcavajani-net -var external_net=floating -var admin_size=${adminFlavor} -var masters=${masterCount} -var master_size=${masterFlavor} -var workers=${workerCount} -var worker_size=${workerFlavor}  -auto-approve 2>&1 | tee ${WORKSPACE}/logs/caasp-openstack.log")
            }
        }
    }

    stage('Cleanup') {
        if (workspaceCleanup) {
            try {
                cleanWs()
            } catch (Exception exc) {
                echo "Failed to clean workspace"
            }
        } else {
            echo "Skipping Cleanup as request was made to NOT cleanup the workspace"
        }
    }
}