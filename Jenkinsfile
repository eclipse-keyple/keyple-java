#!groovy
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
        PROJECT_NAME = "keyple-java"
        PROJECT_BOT_NAME = "Eclipse Keyple Bot"
    }
    stages {
        stage('Import keyring'){
            when {
                expression { env.GIT_URL == 'https://github.com/eclipse/keyple-java.git' && env.CHANGE_ID == null }
            }
            steps{
                container('java-builder') {
                    configFileProvider(
                        [configFile(fileId: 'gradle.properties',
                            targetLocation: '/home/jenkins/agent/gradle.properties')]) {
                        sh 'ln -s /home/jenkins/agent/gradle.properties /home/jenkins/.gradle/gradle.properties'
                        /* Read key Id in gradle.properties */
                        sh 'head -1 /home/jenkins/.gradle/gradle.properties'
                    }
                    withCredentials([
                        file(credentialsId: 'secret-subkeys.asc',
                            variable: 'KEYRING')]) {
                        /* Import GPG keyring with --batch and trust the keys non-interactively in a shell build step */
                        sh 'gpg1 --batch --import "${KEYRING}"'
                        sh 'gpg1 --list-secret-keys'
                        sh 'gpg1 --list-keys'
                        sh 'gpg1 --version'
                        sh 'for fpr in $(gpg1 --list-keys --with-colons  | awk -F: \'/fpr:/ {print $10}\' | sort -u); do echo -e "5\ny\n" |  gpg1 --batch --command-fd 0 --expert --edit-key ${fpr} trust; done'
                        sh 'ls -l  /home/jenkins/.gnupg/'
                    }
                }
            }
        }
        stage('Prepare settings') {
            steps{
                container('java-builder') {
                    script {
                        keypleVersion = sh(script: 'grep version java/component/keyple-core/gradle.properties | cut -d= -f2 | tr -d "[:space:]"', returnStdout: true).trim()
                        echo "Building version ${keypleVersion}"
                        deploySnapshot = env.GIT_URL == 'https://github.com/eclipse/keyple-java.git' && env.GIT_BRANCH == "develop" && env.CHANGE_ID == null && keypleVersion ==~ /.*-SNAPSHOT$/
                        deployRelease = env.GIT_URL == 'https://github.com/eclipse/keyple-java.git' && (env.GIT_BRANCH == "master" || env.GIT_BRANCH.startsWith('release-')) && env.CHANGE_ID == null && keypleVersion ==~ /\d+\.\d+.\d+$/
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
                        keypleVersion = sh(script: 'grep version java/component/keyple-core/gradle.properties | cut -d= -f2 | tr -d "[:space:]"', returnStdout: true).trim()
                        echo "Building version ${keypleVersion}"
                    }
                }
            }
        }
        stage('Keyple Android: Build and Test') {
            steps{
                container('java-builder') {
                    dir('android') {
                        sh './gradlew :keyple-plugin:keyple-plugin-android-nfc:installPlugin :keyple-plugin:keyple-plugin-android-nfc:check'
                        sh './gradlew :keyple-plugin:keyple-plugin-android-omapi:installPlugin :keyple-plugin:keyple-plugin-android-omapi:check'
                        junit allowEmptyResults: true, testResults: 'keyple-plugin/**/build/test-results/testDebugUnitTest/*.xml'
                    }
                }
            }
        }
        stage('Keyple Examples: Build and Test') {
            steps{
                container('java-builder') {
                    sh 'keytool -genkey -v -keystore ~/.android/debug.keystore -storepass android -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US" -keyalg RSA -keysize 2048 -validity 90'

                    dir('java/example/generic/android/nfc/') {
                        sh "./gradlew assembleDebug -P keyple_version=${keypleVersion}"
                    }
                    dir('java/example/generic/android/omapi') {
                        sh "./gradlew assembleDebug -P keyple_version=${keypleVersion}"
                    }
                    dir('java/example/generic/remote/keyple-example-remote-poolclient-webservice') {
                        sh "./gradlew assemble -P keyple_version=${keypleVersion}"
                    }
                    dir('java/example/generic/remote/keyple-example-remote-server-webservice') {
                        sh "./gradlew assemble -P keyple_version=${keypleVersion}"
                    }
                    dir('java/example/generic/remote/keyple-example-remote-server-websocket') {
                        sh "./gradlew assemble -P keyple_version=${keypleVersion}"
                    }
                    dir('java/example/calypso') {
                        sh "./gradlew assemble -P keyple_version=${keypleVersion}"
                    }
                    dir('java/example/generic/local') {
                        sh "./gradlew assemble -P keyple_version=${keypleVersion}"
                    }
                }
            }
        }
        stage('Keyple Java: Tag/Push') {
            when {
                expression { deployRelease }
            }
            steps{
                container('java-builder') {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                        sh """
                            git config --global user.email "${PROJECT_NAME}-bot@eclipse.org"
                            git config --global user.name "${PROJECT_BOT_NAME}"
                            git tag '${keypleVersion}'
                            git push 'https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/eclipse/keyple-java.git' refs/tags/${keypleVersion}
                        """
                    }
                }
            }
        }
        stage('Keyple Java: Code Quality') {
            when {
                expression { deploySnapshot || deployRelease }
            }
            steps {
                catchError(buildResult: 'SUCCESS', message: 'Unable to log code quality to Sonar.', stageResult: 'FAILURE') {
                    container('java-builder') {
                        withCredentials([string(credentialsId: 'sonarcloud-token', variable: 'SONAR_LOGIN')]) {
                            sh './gradlew codeQuality --info'
                        }
                    }
                }
            }
        }
        stage('Keyple Android: Code Quality') {
            when {
                expression { deploySnapshot || deployRelease }
            }
            steps {
                catchError(buildResult: 'SUCCESS', message: 'Unable to log code quality to Sonar.', stageResult: 'FAILURE') {
                    container('java-builder') {
                        dir('android') {
                            withCredentials([string(credentialsId: 'sonarcloud-token', variable: 'SONAR_LOGIN')]) {
                                sh './gradlew codeQuality'
                            }
                        }
                    }
                }
            }
        }
        stage('Keyple Java: Upload artifacts to sonatype') {
            when {
                expression { deploySnapshot || deployRelease }
            }
            steps{
                container('java-builder') {
                    configFileProvider(
                        [configFile(fileId: 'gradle.properties',
                            targetLocation: '/home/jenkins/agent/gradle.properties')]) {
                        sh './gradlew :java:component:keyple-core:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-calypso:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-plugin:keyple-plugin-pcsc:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-plugin:remote:keyple-plugin-remote-common:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-plugin:remote:keyple-plugin-remote-local:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-plugin:remote:keyple-plugin-remote-remote:uploadArchives ${uploadParams}'
                        sh './gradlew :java:component:keyple-plugin:keyple-plugin-stub:uploadArchives ${uploadParams}'
                        sh './gradlew --stop'
                    }
                }
            }
        }
        stage('Keyple Android: Upload artifacts to sonatype') {
            when {
                expression { deploySnapshot || deployRelease }
            }
            steps{
                container('java-builder') {
                    configFileProvider(
                        [configFile(fileId: 'gradle.properties',
                            targetLocation: '/home/jenkins/agent/gradle.properties')]) {
                        dir('android') {
                            sh './gradlew :keyple-plugin:keyple-plugin-android-nfc:uploadArchives ${uploadParams} -P keyple_version=${keypleVersion}'
                            sh './gradlew :keyple-plugin:keyple-plugin-android-omapi:uploadArchives ${uploadParams} -P keyple_version=${keypleVersion}'
                            sh './gradlew --stop'
                        }
                    }
                }
            }
        }
        stage('Keyple Java: Prepare packaging') {
            when {
                expression { deploySnapshot || deployRelease }
            }
            steps {
                container('java-builder') {
                    sh 'mkdir ./repository'
                    sh 'mkdir ./repository/java'
                    sh 'mkdir ./repository/android'
                    sh 'cp ./java/component/keyple-calypso/build/libs/keyple-java-calypso*.jar ./repository/java'
                    sh 'cp ./java/component/keyple-core/build/libs/keyple-java-core*.jar ./repository/java'
                    sh 'cp ./java/component/keyple-plugin/pcsc/build/libs/keyple-java-plugin*.jar ./repository/java'
                    sh 'cp ./java/component/keyple-plugin/remote/common/build/libs/keyple-java-plugin*.jar ./repository/java'
                    sh 'cp ./java/component/keyple-plugin/remote/local/build/libs/keyple-java-plugin*.jar ./repository/java'
                    sh 'cp ./java/component/keyple-plugin/remote/remote/build/libs/keyple-java-plugin*.jar ./repository/java'
                    sh 'cp ./java/component/keyple-plugin/stub/build/libs/keyple-java-plugin*.jar ./repository/java'
                    sh 'cp ./java/example/generic/android/nfc/build/outputs/apk/debug/*.apk ./repository/android'
                    sh 'cp ./java/example/generic/android/omapi/build/outputs/apk/debug/*.apk ./repository/android'
                    sh 'cp ./android/keyple-plugin/android-nfc/build/outputs/aar/keyple-android-plugin*.aar ./repository/android'
                    sh 'cp ./android/keyple-plugin/android-omapi/build/outputs/aar/keyple-android-plugin*.aar ./repository/android'
                    sh 'ls -R ./repository'
                }
            }
        }
        stage('Keyple Java: Deploy packaging to eclipse snapshots') {
            when {
                expression { deploySnapshot }
            }
            steps {
                container('java-builder') {
                    sshagent(['projects-storage.eclipse.org-bot-ssh']) {
                        sh "ssh genie.keyple@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/keyple/snapshots"
                        sh "ssh genie.keyple@projects-storage.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/keyple/snapshots"
                        sh "scp -r ./repository/* genie.keyple@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/keyple/snapshots"
                    }
                }
            }
        }
        stage('Keyple Java: Deploy packaging to eclipse releases') {
            when {
                expression { deployRelease }
            }
            steps {
                container('java-builder') {
                    sshagent(['projects-storage.eclipse.org-bot-ssh']) {
                        sh "ssh genie.keyple@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/keyple/releases"
                        sh "ssh genie.keyple@projects-storage.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/keyple/releases"
                        sh "scp -r ./repository/* genie.keyple@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/keyple/releases"
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
