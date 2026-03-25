pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh 'chmod +x build_and_run.sh'
                sh './build_and_run.sh'
            }
        }
    }
}
