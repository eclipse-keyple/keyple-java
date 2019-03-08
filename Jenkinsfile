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
        sh './gradlew keyple-core:build --info'
   }

   stage('Build keyple calypso') {
        sh './gradlew keyple-calypso:build --info'
   }

   stage('Build java keyple plugins') {
        sh './gradlew keyple-plugin:build --info'
   }

   stage('Build java keyple examples') {
        sh './gradlew example:build --info'
   }

   stage('Build android OMAPI Plugin') {
        sh './gradlew :keyple-plugin:keyple-plugin-android-omapi:build :keyple-plugin:keyple-plugin-android-omapi:generateDebugJavadoc --info'
   }

   stage('Build android NFC Plugin') {
        sh './gradlew :keyple-plugin:keyple-plugin-android-nfc:build :keyple-plugin:keyple-plugin-android-nfc:generateDebugJavadoc --info'
   }
   stage('Build android NFC Example APP') {
        sh './gradlew -b ./example/calypso/android/nfc/build.gradle assembleDebug --info'
   }
   stage('Finished') {
        echo "Finished"
   }
}