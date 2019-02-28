@Library('vl-jenkins')_

import no.nav.jenkins.*

pipeline {
    agent {
        docker {
            image 'maven:3-alpine'
            args '-v $HOME/.m2:/root/.m2'
        }
    }
    stages {
        stage("initialize") {
            steps {
                checkout scm
                script {
                    def GIT_COMMIT_HASH = sh(script: "git log -n 1 --pretty=format:'%h'", returnStdout: true)
                    def changelist = "_" + date.format("YYYYMMDDHHmmss") + "_" + GIT_COMMIT_HASH
                    def mRevision = maven.revision()
                    def tagName = mRevision + changelist
                    //def committer = sh(script: 'git log -1 --pretty=format:"%an (%ae)"', returnStdout: true).trim()
                    //def committerEmail = sh(script: 'git log -1 --pretty=format:"%ae"', returnStdout: true).trim()
                    //def changelog = sh(script: 'git log `git describe --tags --abbrev=0`..HEAD --oneline', returnStdout: true)
                    currentBuild.displayName = tagName
                }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B'
            }
        }

        stage('Upload Artifact') {
            steps {
                sh "mvn clean deploy -Dusername=${env.DEP_USERNAME} -Dpassword=${env.DEP_PASSWORD} -DskipTests -B -e -Dfile.encoding=UTF-8 -DdeployAtEnd=true -Dsha1= -Dchangelist= -Drevision=$tagName"
            }
        }
    }
}
