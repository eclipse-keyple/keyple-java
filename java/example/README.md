Getting started - Example
--
Here are several ready-to-execute examples to demonstrate Keyple capabilities with different types of Keyple plugins and Secure Elements.

Keyple is compatible with any card reader as long as a Keyple plugin is provided. In theses examples, we use:
- PCSC smartcard readers : usually USB readers, they can be with-contact or contactless. They use the Keyple PCSC plugin.
- Stub readers : the Stub plugin allows to emulate programmatically a smartcard of any kind.
- Remote SE readers : the Remote Se plugin allows to communicate with a smartcard inserted on a remote reader hosted on another device.

As Keyple is not tight to Calypso protocol, both generic and Calypso examples are provided.
- Calypso examples are available in this subproject : [Calypso examples](/java/example/calypso/)
- Generic examples are available in this subproject : [Generic examples](/java/example/generic/)

Also, some Android examples are provided: [Android examples](/java/example/calypso/android). A dedicated `readme.md` file with specific information can be found in the android folder.

Build
---
These examples can run on any machine: Linux, Windows and MacOS. If not installed on your machine, you will need to download :
- Java 1.6 or newer
- Gradle (any version) [download](https://gradle.org/install/)

For ease of use, the examples have been broken down into several projects: each scenario is provided as an independant gradle project. Each one of this projetcs demonstrates one specific feature of Keyple. And to avoid code duplication, dependencies to common files are handled via symbolic links.

Although symlink support should be provided out of the box for Unix users, **Windows users** should be aware that the git option `core.symlinks` needs to be enabled before [cloning](https://help.github.com/en/articles/cloning-a-repository) this repo. Several solutions can be considered:
- When installing git for Windows, an option `Enable symbolic links` can be choosen. If it has not been enabled and you want to set it via the installer, a reinstallation is needed
- If you do not want to reinstall git, this option can be enabled afterward via the command line `git config core.symlinks true`
- Also, the option can be enabled once only for this specific cloning operation with `git clone -c core.symlinks=true REPO_URL`

It is important to note that for this option to be actually working, the Windows user needs to have the **_SeCreateSymbolicLink_ permission**: a user with admin rights is typically granted with this permission.


We recommend you to use a Java IDE like [Eclipse](https://www.eclipse.org/downloads/packages/) or Intellij to import the Example project and to run easily the provided java classes.

Once you have cloned this project, import the folder corresponding to the scenario you want to test into your favorite IDE as a new project. As this project is Gradle based, all the configuration should be recognized by your IDE and the dependencies, including Keyple components, will be downloaded (which might take some time the first time it is done). 

Keyple components are hosted on Maven repositories therefore it is easy to include them in your projects. A gradle import configuration examples can be found in the `build.gradle` of each subproject.

Run 
---
Each subproject contains runnable classes (with a Main method) to demonstrate one feature of Keyple. Running it from your IDE should be straightforward.

Log
---
The application log output format is configurable in the properties files
`common/src/main/resources/simplelogger.properties`.
The user will mainly be interested in setting the log level with the `org.slf4j.simpleLogger.defaultLogLevel` field (see the documentation inside the file).
