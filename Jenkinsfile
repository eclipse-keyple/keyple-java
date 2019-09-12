pipeline {
    agent {
        kubernetes {
            label 'onPR'
            yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: android-sdk
    image: eclipsekeyple/build:android2
    command: ["/usr/local/bin/uid_entrypoint"]
    args: ["cat"]
    tty: true
    volumeMounts:
    - name: volume-known-hosts
      mountPath: /home/jenkins/.ssh
  volumes:
  - name: volume-known-hosts
    configMap:
      name: known-hosts
'''
        }
    }

    environment {
        JVM_OPTS= '-Xmx3200m'
        ANDROID_HOME=  '/opt/android-sdk-linux'
    }

    stages {

        stage('Prepare'){
            steps{
                container('android-sdk') {
                    echo "branch to be merge : ${ghprbSourceBranch}"
                    //clone branch
                    git branch: '${ghprbSourceBranch}', url: 'https://github.com/eclipse/keyple-java.git'
                    sh 'gradle wrapper --gradle-version 4.5.1'
                }
            }
        }

        stage('Execute tests') {
            steps{
                container('android-sdk') {
                    sh './gradlew check --info'
                    sh './gradlew -b ./java/example/build.gradle check'
                    sh './gradlew -b ./java/integration/build.gradle check'
                    sh './gradlew -b ./android/build.gradle :keyple-plugin:keyple-plugin-android-nfc:check'
                    sh './gradlew -b ./android/build.gradle :keyple-plugin:keyple-plugin-android-omapi:check'
                }
            }
        }

    }
}