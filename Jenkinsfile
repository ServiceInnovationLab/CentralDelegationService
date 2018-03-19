pipeline {
	agent any
	tools {
	    maven 'Maven 3.3.9'
	}

	stages {
		stage('Compile') {
			steps {
                sh 'mvn compile'
			}
		}
		stage('Integration Test') {
			environment {
				PGPASSWORD='docker'
			}
			steps {
				sh 'docker-compose up -d'

                // skipping resources and main compile, we just did them
                sh 'mvn test -Dmaven.main.skip=true -Dmaven.resources.skip=true -P it'
			}
		}
		stage('Package') {
			steps {
				// skipping resources and main compile, we just did them
				sh 'mvn package -Dmaven.main.skip=true -Dmaven.resources.skip=true'
				archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
			}
		}
		stage('Deploy') {
			steps {
				dir('ansible-deploy') {
					ansiblePlaybook(
						playbook: 'playbook.yml',
						inventory: 'pocdev-hosts'
					)
				}
			}
		}
		stage('Site') {
			steps {
				sh 'mvn site'
				sh 'cp -r "${WORKSPACE}/target/site"/* /usr/share/nginx/html/docs/'
			}
		}
	}

	post {
		success {
			updateGitlabCommitStatus(name: 'build', state: 'success')
		}
		failure {
			updateGitlabCommitStatus(name: 'build', state: 'failed')
		}
	    always {
	        step([$class: 'Mailer', recipients: 'delegation_dev@datacom.co.nz', notifyEveryUnstableBuild: true])
	    }
	}
}
