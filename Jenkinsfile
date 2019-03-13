pipeline {
    agent {
        kubernetes {
            label 'my-agent-pod'
            yaml """
apiVersion: v1
kind: Pod
spec:
    containers:
    - name: android-sdk
      image: eclipsekeyple/build:latest
      volumeMounts:
      - name: volume-known-hosts
        mountPath: /home/jenkins/.ssh
      command:
      - cat
      tty: true
    volumes:
    - name: volume-known-hosts
      configMap:
        name: known-hosts

"""
        }
    }

    environment {
        JVM_OPTS= '-Xmx3200m'
        ANDROID_HOME=  '/home/user/android-sdk-linux'
        GRADLE_CACHE = '/tmp/gradle-user-home'

    }

    stages {

        stage('cache'){
            steps{
                configFileProvider([configFile(fileId: '97db9a59-1223-4d6a-a789-598c4eb9fdd8', variable: 'MVN_SETTINGS')]) {
                    // some block
                }

            }
        }


        stage('Prepare'){
            steps{
                container('android-sdk') {
                    git branch: 'master', url: 'https://github.com/eclipse/keyple-java.git'
                    //checkout csm
                }
            }
        }

        stage('Execute tests') {
            steps{
                container('android-sdk') {
                    sh 'gradle wrapper --gradle-version 4.5.1'
                    sh './gradlew test --info'
                    sh './gradlew -b ./android/build.gradle :keyple-plugin:keyple-plugin-android-nfc:test'
                    sh './gradlew -b ./android/build.gradle :keyple-plugin:keyple-plugin-android-omapi:test'
                }
            }
        }

        stage('Assemble artifacts') {
            steps{
                container('android-sdk') {
                    sh './gradlew assemble --info'
                    sh './gradlew -b ./android/build.gradle :keyple-plugin:keyple-plugin-android-nfc:assembleDebug --info'
                    sh './gradlew -b ./android/build.gradle :keyple-plugin:keyple-plugin-android-omapi:assembleDebug --info'
                }
            }
        }

        stage('Generate javadocs') {
            steps{
                container('android-sdk') {
                    sh './gradlew -b ./android/build.gradle  :keyple-plugin:keyple-plugin-android-omapi:generateDebugJavadoc'
                    sh './gradlew -b ./android/build.gradle  :keyple-plugin:keyple-plugin-android-nfc:generateDebugJavadoc'
                }
            }
        }

        stage('Generate apks') {
            steps{
                container('android-sdk') {
                    sh 'mkdir -p "/home/jenkins/workspace/test_keyple_java/android/example/calypso/nfc/?/.android/"'
                    sh 'mkdir -p "/home/jenkins/workspace/test_keyple_java/android/example/calypso/omapi/?/.android/"'
                    sh 'keytool -genkey -v -keystore /home/jenkins/workspace/test_keyple_java/android/example/calypso/nfc/?/.android/debug.keystore -storepass android -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US"'
                    sh 'keytool -genkey -v -keystore /home/jenkins/workspace/test_keyple_java/android/example/calypso/omapi/?/.android/debug.keystore -storepass android -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US"'
                    sh './gradlew -b ./android/example/calypso/nfc/build.gradle assembleDebug'
                    sh './gradlew -b ./android/example/calypso/omapi/build.gradle assembleDebug'
                }
            }
        }
    }
}