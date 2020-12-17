### Android components
If you want to build the keyple android components (aar plugins), you need : 
- Java JDK 1.6, 1.7 or 1.8 (Java 11 is not supported yet)
- Intellij 2018+ community version or Android Studio 3.0
- Android sdk 26 should be installed on your machine [follow those instructions](http://www.androiddocs.com/sdk/installing/index.html)
- Gradle (any version as we use the gradle wrapper) [available here](https://gradle.org/install/)

To setup where is installed you Android SDK, you need to create a file `local.properties` in the ```/android```, ``/android/example/calypso/nfc``, ```/android/example/calypso/omapi``` folders with the following content 
`sdk.dir=absolut/path/to/where/your/android/sdk/is`

For instance ``sdk.dir=/Users/user/Library/Android/sdk``

#### Windows, Linux or macOS

First, you need to build and install locally the java component keyple-core (see above)
To build the plugins, execute the following commands :  

On Windows : 
```
cd android
.\gradlew.bat installPlugin
```
On Linux, macOS : 
```
cd android
./gradlew installPlugin
```

To build the example app NFC and OMAPI, first, you need to build and install locally the java component keyple-core, keyple-calypso and keyple-android-plugin (see above)

On Windows : 
```
.\gradlew.bat -b ./example/generic/android/nfc/build.gradle assembleDebug 
.\gradlew.bat -b ./example/generic/android/omapi/build.gradle assembleDebug
```
On Linux, macOS : 
```
./gradlew -b ./example/generic/android/nfc/build.gradle assembleDebug 
./gradlew -b ./example/generic/android/omapi/build.gradle assembleDebug
```