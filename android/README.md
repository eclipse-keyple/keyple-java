## Building the examples and the Keyple components

### Android components

If you want to build the keyple android components (aar plugins, apk example app), you need : 
- Java JDK 1.8 or newer (OMAPI app requires java 1.8)
- Intellij 2018 community version or Android Studio 3.0
- Android sdk 26 should be installed on your machine [follow those instructions](http://www.androiddocs.com/sdk/installing/index.html)
- Gradle (any version as we use the gradle wrapper) [available here](https://gradle.org/install/)

To acknowledge where is installed you Android SDK, you need to create a file `local.properties` in the ```/android```, ``/android/example/calypso/nfc``, ```/android/example/calypso/omapi`` folders with the following content 
`sdk.dir=absolut/path/to/where/your/android/sdk/is`

For instance ``sdk.dir=/Users/user/Library/Android/sdk``



#### Linux or Macos
To build the plugins, execute the following commands in the **/android folder**, the first command is required to be executed at least once to build the gradle wrapper.  

```
gradle wrapper --gradle-version 4.5.1
./gradlew build
```

To build the example app NFC and OMAPI

```
./gradlew -b ./example/calypso/nfc/build.gradle assembleDebug 
./gradlew -b ./example/calypso/omapi/build.gradle assembleDebug
```

### Windows

To build the plugins, execute the following commands in the **/android folder**, the first command is required to be executed at least once to build the gradle wrapper.  

```
gradle wrapper --gradle-version 4.5.1`
.\gradlew.bat build
```

To build the example app NFC and OMAPI

```
.\gradlew.bat -b ./example/calypso/nfc/build.gradle assembleDebug 
.\gradlew.bat -b ./example/calypso/omapi/build.gradle assembleDebug
