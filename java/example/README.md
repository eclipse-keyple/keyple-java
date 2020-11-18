Getting started - Example
--
Here are several ready-to-execute examples to demonstrate Keyple capabilities with different types of Keyple plugins and Secure Elements.

Keyple is compatible with any card reader as long as a Keyple plugin is provided. In theses examples, we use:
- PCSC readers, usually USB readers, they can be with-contact or contactless. More info on the [PCSC plugin](/java/component/keyple-plugin/pcsc) 
- Stub readers : allow to emulate programmatically a smart card of any kind. More info on the More info on the [Stub plugin](/java/component/keyple-plugin/stub)
- Remote readers : they allow to communicate with a smart card inserted on a remote reader hosted on another device. More info on the [Remote plugin](/java/component/keyple-plugin/stub)

As Keyple is not tight to Calypso protocol, both generic and Calypso examples are provided.
- Calypso examples are available in this subproject : [Calypso examples](/java/example/calypso/)
- Generic examples are available in this subproject : [Generic examples](/java/example/generic/)

Also, some Android examples are provided: [Android examples](/java/example/calypso/android).
 
Each folder provides a dedicated `readme.md` file with specific information.

Build
---
These examples can run on any machine: Linux, Windows and MacOS. If not installed on your machine, you will need to download :
- Java 1.6 or newer
- Gradle (any version) [download](https://gradle.org/install/)

For ease of use, the examples have been broken down into several main classes: each scenario is provided as an independent runnable class. Each one of this runnable class demonstrates one specific feature of Keyple.

We recommend you to use a Java IDE like [Eclipse](https://www.eclipse.org/downloads/packages/) or Intellij to import the Example project and to run easily the provided java classes.

Once you have cloned this repository, import the folder corresponding to the scenario you want to test into your favorite IDE as a new project. As this project is Gradle based, all the configuration should be recognized by your IDE and the dependencies, including Keyple components, will be downloaded (which might take some time the first time it is done). 

Keyple components are hosted on Maven repositories therefore it is easy to include them in your projects. A gradle import configuration examples can be found in the `build.gradle` of each subproject.

Run 
---
Each subproject contains runnable classes (with a Main method) to demonstrate one feature of Keyple. Running it from your IDE should be straightforward.

Log
---
The application log output format is configurable in the properties files
`common/src/main/resources/simplelogger.properties`.
The user will mainly be interested in setting the log level with the `org.slf4j.simpleLogger.defaultLogLevel` field (see the documentation inside the file).
