## Building the Android examples


If you want to build the keyple android example apps, you need : 
- Java JDK 1.8 or newer (OMAPI app requires java 1.8)
- Intellij 2018 community version or Android Studio 3.0
- Android sdk 26 should be installed on your machine [follow those instructions](http://www.androiddocs.com/sdk/installing/index.html)
- Gradle (any version as we use the gradle wrapper) [available here](https://gradle.org/install/)

Depending on your environment and to way you build, you may need to acknowledge where is installed you Android SDK: in this case create a file `local.properties` at the root of each android project with the following content `sdk.dir=absolut/path/to/where/your/android/sdk/is`


To ease the use, NFC and OMAPI examples have been split into two independent projects. Simply import the one you want to test into you IDE and, as it is gradle based, building it from the IDE should be straightforward.


#### Alternatively you can build the projects via the command line:

From the proper folder: `gradle assembleDebug`

Using the gradle wrapper can be convenient, if is not provided you first need to generate it. To do so, execute the following commands in the proper folder

`gradle wrapper --gradle-version <gradle_version>`
With `<gradle_version>` at 4.5.1 minimum

And then: `./gradlew assembleDebug`
Or `./gradlew.bat assembleDebug` for Windows users.
