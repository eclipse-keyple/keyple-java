node {

    stage ('hello') {
        echo "Hello"

    }


    stage ('Clone') {
        echo "stage Clone"
        checkout scm
        //git  branch: 'origin/maven-versioning', credentialsId: '93ff2866-a5e1-4e3e-a167-ca33402feb7b', url: 'git@github.com:calypsonet/keyple-java.git'
    }

    stage ('gradle') {
        echo "Gradle build"
        sh 'gradle build'

    }

}