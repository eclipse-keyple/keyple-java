

pipeline {
    agent {
        kubernetes {
      label 'my-pod'
      yaml '''
apiVersion: v1
kind: Pod
spec:
    containers:
    - name: android-sdk
      image: eclipsekeyple/build:pgp2
      command: ["/usr/local/bin/uid_entrypoint"]
      args: ["cat"]
      tty: true
      volumeMounts:
      - name: "jenkins-home"
        mountPath: "/home/jenkins"
        readOnly: false
      - name: "gradle-home"
        mountPath: "/home/jenkins/.gradle"
        readOnly: false
      - name: volume-known-hosts
        mountPath: /home/jenkins/.ssh
      - name: settings-xml
        mountPath: /home/jenkins/.m2/settings.xml
        subPath: settings.xml
        readOnly: true
      - name: m2-repo
        mountPath: /home/jenkins/.m2/repository

    volumes:
    - name: "jenkins-home"
      emptyDir:
        medium: ""
    - name: "gradle-home"
      emptyDir:
        medium: ""
    - name: volume-known-hosts
      configMap:
        name: known-hosts
    - name: settings-xml
      configMap:
        name: m2-dir
        items:
        - key: settings.xml
          path: settings.xml
    - name: m2-repo
      emptyDir: {}
'''
        }
    }

    environment {
        JVM_OPTS= '-Xmx3200m'
        ANDROID_HOME=  '/opt/android-sdk-linux'
        GRADLE_USER_HOME = "/home/jenkins/agent"
        uploadParams = "-PdoSign=true --info"
        forceBuild = false
    }

    stages {

        stage('Check volume folder'){
            steps{
                container('android-sdk') {
                    sh 'ls -l /home/jenkins'
                    //sh 'mkdir /home/jenkins/.gradle'
                    sh 'ls -l /home/jenkins/.gradle'
                    sh 'echo $GRADLE_USER_HOME'
                    sh 'echo ANDROID_HOME'

                }
            }
        }

        stage('Checkout repo') {
            steps{
                container('android-sdk') {
                    script {

                        /* Checkout project */
                        def scmVars =  checkout([$class: 'GitSCM', branches: [[name: '*/develop']], userRemoteConfigs: [[url: 'https://github.com/eclipse/keyple-java.git']]])

                        echo "scmVars.GIT_COMMIT ${scmVars.GIT_COMMIT}"
                        echo "scmVars.GIT_BRANCH ${scmVars.GIT_BRANCH}"

                        env.GIT_COMMIT = scmVars.GIT_COMMIT
                        env.GIT_PREVIOUS_SUCCESSFUL_COMMIT = scmVars.GIT_PREVIOUS_SUCCESSFUL_COMMIT
                        env.UPDATE = (env.GIT_COMMIT != env.GIT_PREVIOUS_SUCCESSFUL_COMMIT)
                        echo "env.GIT_COMMIT : ${env.GIT_COMMIT}"
                        echo "env.GIT_PREVIOUS_SUCCESSFUL_COMMIT : ${env.GIT_PREVIOUS_SUCCESSFUL_COMMIT}"
                        echo "env.UPDATE : ${env.UPDATE}"

                        if(forceBuild ==true){
                            env.UPDATE = true;
                        }

                    }
                }
            }
        }

        stage('Import keyring'){
            when {
                //execute stage only if update on the source code
                expression { env.UPDATE == "true" }
            }
            steps{
                container('android-sdk') {
                    withCredentials([
                        file(credentialsId: 'a85c50bf-acdb-4bfe-9036-21c76e893104',
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

        stage('Build project and upload artifacts to sonatype') {
            when {
                //execute stage only if update on the source code
                expression { env.UPDATE == "true" }
            }
            steps{
                container('android-sdk') {
                    configFileProvider(
                        [configFile(
                            fileId: 'cc143349-1c10-4453-95f6-38e61cdffe3f',
                            targetLocation: GRADLE_USER_HOME)]) {

                        /* Compile project */
                        //build and install keyple-core locally
                        sh 'gradle :java:component:keyple-core:uploadArchives --info'
                        sh 'gradle :java:component:keyple-calypso:uploadArchives --info'


                        /* Read key Id in gradle.properties */
                        sh 'head -1  ${GRADLE_USER_HOME}/gradle.properties'


                        /* Sign and Upload Jars */
                        sh 'gradle :java:component:keyple-core:uploadArchives ${uploadParams}'
                        sh 'gradle :java:component:keyple-calypso:uploadArchives ${uploadParams}'
                        sh 'gradle :java:component:keyple-plugin:keyple-plugin-pcsc:uploadArchives ${uploadParams}'
                        sh 'gradle :java:component:keyple-plugin:keyple-plugin-remotese:uploadArchives ${uploadParams}'
                        sh 'gradle :java:component:keyple-plugin:keyple-plugin-stub:uploadArchives ${uploadParams}'

                         /* Sign and Upload Aars */
                        sh 'gradle -b ./android/build.gradle :keyple-plugin:keyple-plugin-android-nfc:uploadArchives ${uploadParams}'
                        sh 'gradle -b ./android/build.gradle :keyple-plugin:keyple-plugin-android-omapi:uploadArchives ${uploadParams}'
                    }
                }
            }
        }

        stage('Generate apks ') {
             when {
                //execute stage only if update on the source code
                expression { env.UPDATE == "true" }
            }
            steps{
                container('android-sdk') {
                    sh 'mkdir -p "./android/example/calypso/nfc/?/.android/"'
                    sh 'mkdir -p "./android/example/calypso/omapi/?/.android/"'
                    sh 'keytool -genkey -v -keystore ./android/example/calypso/nfc/?/.android/debug.keystore -storepass android -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US"'
                    sh 'keytool -genkey -v -keystore ./android/example/calypso/omapi/?/.android/debug.keystore -storepass android -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US"'
                    sh 'gradle -b ./android/example/calypso/nfc/build.gradle assembleDebug'
                    sh 'gradle -b ./android/example/calypso/omapi/build.gradle assembleDebug'
                }
            }
        }

        stage('Prepare package for eclipse') {
             when {
                //execute stage only if update on the source code
                expression { env.UPDATE == "true" }
            }
          steps {
           container('android-sdk') {
                sh 'mkdir ./repository'
                sh 'mkdir ./repository/java'
                sh 'mkdir ./repository/android'
                sh 'cp ./java/component/keyple-calypso/build/libs/keyple-java-calypso*.jar ./repository/java'
                sh 'cp ./java/component/keyple-core/build/libs/keyple-java-core*.jar ./repository/java'
                sh 'cp ./java/component/keyple-plugin/pcsc/build/libs/keyple-java-plugin*.jar ./repository/java'
                sh 'cp ./java/component/keyple-plugin/remotese/build/libs/keyple-java-plugin*.jar ./repository/java'
                sh 'cp ./java/component/keyple-plugin/stub/build/libs/keyple-java-plugin*.jar ./repository/java'
                sh 'cp ./android/example/calypso/nfc/build/outputs/apk/debug/*.apk ./repository/android'
                sh 'cp ./android/example/calypso/omapi/build/outputs/apk/debug/*.apk ./repository/android'
                sh 'cp ./android/keyple-plugin/android-nfc/build/outputs/aar/keyple-android-plugin*.aar ./repository/android'
                sh 'cp ./android/keyple-plugin/android-omapi/build/outputs/aar/keyple-android-plugin*.aar ./repository/android'
                sh 'ls -R ./repository'
           }
          }
        }
        /*
        */
        stage('Deploy to eclipse') {
             when {
                //execute stage only if update on the source code
                expression { env.UPDATE == "true" }
            }
          steps {
           container('android-sdk') {
            sshagent(['828b4b17-45cd-467a-ad4c-2b362e8582e5']) {
                //sh "head -n 50 /etc/passwd"
                sh "ssh genie.keyple@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/keyple/snapshots"
                sh "ssh genie.keyple@projects-storage.eclipse.org mkdir -p /home/data/httpd/download.eclipse.org/keyple/snapshots"
                sh "scp -r ./repository/* genie.keyple@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/keyple/snapshots"
              }
            }
          }
        }

        stage('No update on source code'){
            when {
                //execute stage only if update on the source code
                expression { env.UPDATE == "false" }
            }
            steps{
               echo "No update on the source code"
            }
        }
    }
}