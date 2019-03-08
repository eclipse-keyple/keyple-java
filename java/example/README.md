**Folder content**
---

* Two main branches to demonstrate the use of keyple
    - generic: examples of keyple-core implementation targeting all kinds of secure elements
    - calypso : examples of keyple-calypso implementation targeting specific functionalities of Calypso secure elements

**Build configuration**
---
## Building the examples and the Keyple components

### Java components

#### Prerequisites
Here are the prerequisites to build the keyple components (jars) and to run the /example projects
- Java JDK 1.6 or newer
- Maven (any version) [available here](https://maven.apache.org/install.html)
- Gradle (any version as we use the gradle wrapper) [available here](https://gradle.org/install/)


####Linux or Macos
Following commands will build all the artifacts at once. The first command is required to be executed at least once to build the gradle wrapper.  
```
gradle wrapper --gradle-version 4.5.1
./gradlew build  --info
```


####Windows
Following commands will build all the artifacts at once. The first command is required to be executed at least once to build the gradle wrapper.  
```
gradle wrapper --gradle-version 4.5.1
.\gradlew.bat build  --info
```



**Log configuration**
---
The application log output format is configurable in the properties files
`resources/simplelogger.properties`.
The user will mainly be interested in setting the log level with the `org.slf4j.simpleLogger.defaultLogLevel` field (see the documentation inside the file).
