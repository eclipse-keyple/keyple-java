#!groovy
@Library('java-builder') _
def keypleVersion
pipeline {
    agent {
        kubernetes {
            label 'keyple-java'
            yaml javaBuilder('1')
        }
    }
    environment {
        uploadParams = "-PdoSign=true --info"
        forceBuild = false
    }
    stages {
        stage('Import keyring'){
            when {
                expression { env.GIT_URL == 'https://github.com/eclipse/keyple-java.git' && env.CHANGE_ID == null }
            }
            steps{
                container('java-builder') {
                    withCredentials([
                        file(credentialsId: 'secret-subkeys.asc',
                            variable: 'KEYRING')]) {
                        sh 'ln -s /home/jenkins/agent/gradle.properties /home/jenkins/.gradle/gradle.properties'
                        
                        /* Import GPG keyring with --batch and trust the keys non-interactively in a shell build step */
                        sh 'gpg1 --batch --import "${KEYRING}"'
                        sh 'gpg1 --list-secret-keys'
                        sh 'gpg1 --list-keys'
                        sh 'gpg1 --version'
                        sh 'for fpr in $(gpg1 --list-keys --with-colons  | awk -F: \'/fpr:/ {print $10}\' | sort -u); do echo -e "5\ny\n" |  gpg1 --batch --command-fd 0 --expert --edit-key ${fpr} trust; done'
                        sh 'ls -l  /home/jenkins/.gnupg/'
                    }
                    configFileProvider(
                        [configFile(fileId: 'gradle.properties',
                            targetLocation: '/home/jenkins/agent/gradle.properties')]) {
                        /* Read key Id in gradle.properties */
                        sh 'head -1 /home/jenkins/.gradle/gradle.properties'
                    }
                }
            }
        }
        stage('Keyple Java: Build and Test') {
            steps{
                container('java-builder') {
                    sh './gradlew installAll --info'
                    catchError(buildResult: 'UNSTABLE', message: 'There were failing tests.', stageResult: 'UNSTABLE') {
                        sh './gradlew check --info'
                    }
                    junit allowEmptyResults: true, testResults: 'java/component/**/build/test-results/test/*.xml'

                    script {
                        keypleVersion = sh(script: 'grep version java/component/keyple-core/gradle.properties | cut -d= -f2 | tr -d "[:space:]"', returnStdout: true)
                    }
                }
            }
        }
        stage('Keyple Android: Build and Test') {
            steps{
                container('java-builder') {
                    dir('android') {
                        sh './gradlew :keyple-plugin:keyple-plugin-android-nfc:check'
                        sh './gradlew :keyple-plugin:keyple-plugin-android-omapi:check'
                        junit allowEmptyResults: true, testResults: 'keyple-plugin/**/build/test-results/testDebugUnitTest/*.xml'
                    }
                }
            }
        }
        stage('Keyple Examples: Build and Test') {
            steps{
                container('java-builder') {
                    sh "./gradlew -b java/example/calypso/remotese/build.gradle check -P keyple_version=${keypleVersion}"
                }
            }
        }
        stage('Keyple Java: Code Quality') {
            when {
                expression { env.GIT_URL == 'https://github.com/eclipse/keyple-java.git' && (env.GIT_BRANCH == "develop" || env.GIT_BRANCH.startsWith('release-0.9')) && env.CHANGE_ID == null && keypleVersion ==~ /.*-SNAPSHOT$/ }
            }
            steps {
                catchError(buildResult: 'SUCCESS', message: 'Unable to log code quality to Sonar.', stageResult: 'FAILURE') {
                    container('java-builder') {
                        withSonarQubeEnv('Eclipse Sonar') {
                            sh './gradlew codeQuality --info'
                        }
                    }
                }
            }
        }
        stage('Keyple Android: Code Quality') {
            when {
                expression { env.GIT_URL == 'https://github.com/eclipse/keyple-java.git' && (env.GIT_BRANCH == "develop" || env.GIT_BRANCH.startsWith('release-0.9')) && env.CHANGE_ID == null && keypleVersion ==~ /.*-SNAPSHOT$/ }
            }
            steps {
                catchError(buildResult: 'SUCCESS', message: 'Unable to log code quality to Sonar.', stageResult: 'FAILURE') {
                    container('java-builder') {
                        dir('android') {
                            withSonarQubeEnv('Eclipse Sonar') {
                                sh './gradlew codeQuality'
                            }
                        }
                    }
                }
            }
        }
        stage('Keyple Java: Upload artifacts to sonatype') {
            when {
                expression { env.GIT_URL == 'https://github.com/eclipse/keyple-java.git' && (env.GIT_BRANCH == "develop" || env.GIT_BRANCH.startsWith('release-0.9')) && env.CHANGE_ID == null && keypleVersion ==~ /.*-SNAPSHOT$/ }
            }
            steps{
                container('java-builder') {
                    configFileProvider(
                        [configFile(fileId: 'gradle.properties',
                            targetLocation: '/home/jenkins/agent/gradle.properties')]) {
                        sh './gradlew :java:component:keyple-core:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-calypso:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-plugin:keyple-plugin-pcsc:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-plugin:keyple-plugin-remotese:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-plugin:remote-se:keyple-plugin-remotese-core:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-plugin:remote-se:keyple-plugin-remotese-nativese:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-plugin:remote-se:keyple-plugin-remotese-virtualse:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-plugin:keyple-plugin-stub:uploadArchives ${uploadParams}'
                    }
                }
            }
        }
        stage('Keyple Android: Upload artifacts to sonatype') {
            when {
                expression { env.GIT_URL == 'https://github.com/eclipse/keyple-java.git' && (env.GIT_BRANCH == "develop" || env.GIT_BRANCH.startsWith('release-0.9')) && env.CHANGE_ID == null && keypleVersion ==~ /.*-SNAPSHOT$/ }
            }
            steps{
                container('java-builder') {
                    configFileProvider(
                        [configFile(fileId: 'gradle.properties',
                            targetLocation: '/home/jenkins/agent/gradle.properties')]) {
                        dir('android') {
                            sh './gradlew :keyple-plugin:keyple-plugin-android-nfc:uploadArchives ${uploadParams}'
                            sh './gradlew :keyple-plugin:keyple-plugin-android-omapi:uploadArchives ${uploadParams}'
                        }
                    }
                }
            }
        }
        stage('Keyple Java: Generate apks') {
            when {
                expression { env.GIT_URL == 'https://github.com/eclipse/keyple-java.git' && (env.GIT_BRANCH == "develop" || env.GIT_BRANCH.startsWith('release-0.9')) && env.CHANGE_ID == null && keypleVersion ==~ /.*-SNAPSHOT$/ }
            }
            steps{
                container('java-builder') {
                    sh 'mkdir -p "./java/example/calypso/android/nfc/?/.android/"'
                    sh 'mkdir -p "./java/example/calypso/android/omapi/?/.android/"'
                    sh 'keytool -genkey -v -keystore ./java/example/calypso/android/nfc/?/.android/debug.keystore -storepass android -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US"'
                    sh 'keytool -genkey -v -keystore ./java/example/calypso/android/omapi/?/.android/debug.keystore -storepass android -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US"'
                    dir('java/example/calypso/android/nfc/') {
                        sh './gradlew assembleDebug'
                    }
                    dir('java/example/calypso/android/omapi') {
                        sh './gradlew assembleDebug'
                    }
                }
            }
        }
        stage('Keyple Java: Deploy to eclipse') {
            when {
                expression { env.GIT_URL == 'https://github.com/eclipse/keyple-java.git' && (env.GIT_BRANCH == "develop" || env.GIT_BRANCH.startsWith('release-0.9')) && env.CHANGE_ID == null && keypleVersion ==~ /.*-SNAPSHOT$/ }
            }
            steps {
                container('java-builder') {
                    sh 'mkdir ./repository'
                    sh 'mkdir ./repository/java'
                    sh 'mkdir ./repository/android'
                    sh 'cp ./java/component/keyple-calypso/build/libs/keyple-java-calypso*.jar ./repository/java'
                    sh 'cp ./java/component/keyple-core/build/libs/keyple-java-core*.jar ./repository/java'
                    sh 'cp ./java/component/keyple-plugin/pcsc/build/libs/keyple-java-plugin*.jar ./repository/java'
                    sh 'cp ./java/component/keyple-plugin/remotese/build/libs/keyple-java-plugin*.jar ./repository/java'
                    sh 'cp ./java/component/keyple-plugin/stub/build/libs/keyple-java-plugin*.jar ./repository/java'
                    sh 'cp ./java/example/calypso/android/nfc/build/outputs/apk/debug/*.apk ./repository/android'
                    sh 'cp ./java/example/calypso/android/omapi/build/outputs/apk/debug/*.apk ./repository/android'
                    sh 'cp ./android/keyple-plugin/android-nfc/build/outputs/aar/keyple-android-plugin*.aar ./repository/android'
                    sh 'cp ./android/keyple-plugin/android-omapi/build/outputs/aar/keyple-android-plugin*.aar ./repository/android'
                    sh 'ls -R ./repository'
                    sshagent(['projects-storage.eclipse.org-bot-ssh']) {
                        sh "ssh genie.keyple@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/keyple/snapshots"
                        sh "ssh genie.keyple@projects-storage.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/keyple/snapshots"
                        sh "scp -r ./repository/* genie.keyple@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/keyple/snapshots"
                    }
                }
            }
        }
    }
    post {
        always {
            container('java-builder') {
                archiveArtifacts artifacts: 'java/component/**/build/reports/tests/**,android/keyple-plugin/**/build/reports/tests/**', allowEmptyArchive: true
            }
        }
    }
}
