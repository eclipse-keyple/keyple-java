## Building Keyple Android components

If you want to build the keyple android components (aar plugins, apk example app), you need : 
- Java JDK 1.8 or newer (OMAPI app requires java 1.8)
- Intellij 2018 community version or Android Studio 3.0
- Android sdk 26 should be installed on your machine [follow those instructions](http://www.androiddocs.com/sdk/installing/index.html)
- Gradle (any version as we use the gradle wrapper) [available here](https://gradle.org/install/)

Depending on your environment and to way you build, you may need to acknowledge where is installed you Android SDK: in this case create a file `local.properties` at the root of the _android_ folder with the following content `sdk.dir=absolut/path/to/where/your/android/sdk/is`



#### Building the plugins via the command line

From the _android_ folder: `gradle build`

Using the gradle wrapper can be convenient, if is not provided you first need to generate it. To do so, execute the following commands

`gradle wrapper --gradle-version <gradle_version>`
With `<gradle_version>` at 4.5.1 minimum

And then: `./gradlew build`
Or `./gradlew.bat build` for Windows users.
