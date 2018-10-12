node {
   def mvnHome

   /*
    stage ('hello') {
        echo "Hello"
    }
    */

   stage('Checkout github') { // for display purposes
      // Get some code from a GitHub repository
      //git 'https://github.com/jglick/simple-maven-project-with-tests.git'
      // Get the Maven tool.
      // ** NOTE: This 'M3' Maven tool must be configured
      // **       in the global configuration.
      //checkout scm
    git credentialsId: 'odelcroi-github',
        url: 'https://github.com/calypsonet/keyple-java.git', branch:'maven-versioning'


   }


   stage('Build keyple core') {
        sh '/opt/bitnami/gradle4.10/gradle-4.10.2/bin/gradle keyple-core:build --info'

   }

   stage('Build keyple calypso') {
        sh '/opt/bitnami/gradle4.10/gradle-4.10.2/bin/gradle keyple-calypso:build --info'
   }

   stage('Build java keyple plugins') {
        sh '/opt/bitnami/gradle4.10/gradle-4.10.2/bin/gradle keyple-plugin:build --info'
   }
   /*
   stage('Build java keyple examples') {
        sh '/opt/bitnami/gradle4.10/gradle-4.10.2/bin/gradle keyple-example:build --info'
   }
   */


   stage('Build android OMAPI') {
        sh '/opt/bitnami/gradle4.10/gradle-4.10.2/bin/gradle -b ./keyple-plugin/android-omapi/build.gradle build test assembleDebug generateDebugJavadoc --info'
        sh '/opt/bitnami/gradle4.10/gradle-4.10.2/bin/gradle -b ./keyple-example/android/omapi/build.gradle assembleDebug --info'
   }

   /*
   stage('Build android NFC') {
       //error='Cannot allocate memory' (errno=12)
        sh '/opt/bitnami/gradle4.10/gradle-4.10.2/bin/gradle -b ./keyple-plugin/android-nfc/build.gradle build test assembleDebug generateDebugJavadoc --info'
        sh '/opt/bitnami/gradle4.10/gradle-4.10.2/bin/gradle -b ./keyple-example/android/nfc/build.gradle assembleDebug --info'
   }
   */

   /*
   work
   stage('Build keyple plugin') {
        sh '/opt/bitnami/gradle4.10/gradle-4.10.2/bin/gradle keyple-plugin:build --info'
   }
   */


   /*
   does not work
   stage('Javadoc') {
        sh '/opt/bitnami/gradle4.10/gradle-4.10.2/bin/gradle javadoc --info'
   }
   */

   stage('Results') {
        echo "Finished"
   }
}