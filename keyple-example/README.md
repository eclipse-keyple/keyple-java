**Folder content**
---

* Two main branches to demonstrate the use of keyple
    - generic: examples of keyple-core implementation targeting all kinds of secure elements
    - calypso : examples of keyple-calypso implementation targeting specific functionalities of Calypso secure elements

**Build configuration**
---
### Gradle
    TBD
### Maven
    TBD

**Log configuration**
---
The application log output format is configurable in the properties files
`resources/simplelogger.properties`.
The user will mainly be interested in setting the log level with the `org.slf4j.simpleLogger.defaultLogLevel` field (see the documentation inside the file).
