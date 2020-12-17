# 'Eclipse Keyple' Java implementation

This is the repository for the Java implementation of the 'Eclipse Keyple' API. 

Please find all the information you need on the [official documentation](https://keyple.org/docs) and on the [API Reference guide](https://keyple.org/docs/api-reference/)

The Keyple C++ implementation is hosted on https://github.com/eclipse/keyple-cpp.

### Running Keyple examples
This repository includes Java and Android examples of Eclipse Keyple use cases : [Keyple Examples](/java/example)

### Import Keyple components in your project
Keyple components are deployed to Maven Central thus you can import them in your project as dependencies.

Gradle project: 
```
repositories {
        //to import releases
        maven { url 'https://oss.sonatype.org/content/repositories/releases' }
        
        //to import snapshots
        maven {url 'https://oss.sonatype.org/content/repositories/snapshots' }
}

dependencies {
    // Keyple Core is a mandatory library for using Keyple, in this case import the last version of keyple-java-core
    implementation group: 'org.eclipse.keyple', name: 'keyple-java-core', version: '1.0.0'
    
    // Import Calypso library to support Calypso Portable Object, in this case import the last version of keyple-java-calypso
    implementation group: 'org.eclipse.keyple', name: 'keyple-java-calypso', version: '1.0.0'
    
    // Import PCSC library to use a Pcsc reader, in this case import the last version of keyple-java-plugin-pcsc
    implementation group: 'org.eclipse.keyple', name: 'keyple-java-plugin-pcsc', version: '1.0.0'
}
```

### Supported platforms
- Java SE 1.6 compact2
- Android 4.4 KitKat API level 19

### Repository projects

Modules that are provided as artifacts
  - [keyple-java-core](/java/component/keyple-core): contains the core components and interfaces for Eclipse Keyple. 
  - [keyple-java-calypso](/java/component/keyple-calypso): extension for Eclipse Keyple to manage Calypso Portable Object securely using Calypso SAM.
  - [keyple-java-plugin-pcsc](/java/component/keyple-plugin/pcsc): enables the use for PCSC smart card readers into Eclipse Keyple.
  - [keyple-java-plugin-stub](/java/component/keyple-plugin/stub): makes it easy to test your Keyple application by simulating APDU exchanges.    
  - [keyple-android-plugin-nfc](/android/keyple-plugin/nfc): enables the use of the standard NFC reader embedded in Android devices.
  - [keyple-android-plugin-omapi](/android/keyple-plugin/omapi): enables the access of embedded Secure Elements in Android devices.
  - [keyple-distributed](/java/component/keyple-distributed): enables the communication of Keyple components on remote devices.

Quick-start, example projects
  - [Android example](/java/example/generic/android): Android apps with use cases of NFC plugin and OMAPI plugin. 
  - [Calypso example](/java/example/calypso): Runnable use cases of the Calypso extension.  
  - [Standalone example](/java/example/generic/standalone): Runnable use cases of the Keyple Core components with PCSC plugin and Stub plugin. 
  - [Distributed example](/java/example/generic/distributed): Runnable use cases of the Distributed architecture components with web service and web socket implementation.

## Contribute to Eclipse Keyple
We welcome contributions! Every contribution will be reviewed by the developpers team and scan by our CI and quality code tools before being merged to the base code.

### CI and Docker 
Eclipse CI tools to build and test the components are Open Source too. They can be found in this repository : [Eclipse Keyple Ops](https://www.github.com/eclipse/keyple-ops)

## Trademarks

* Eclipse Keyple and the Eclipse Keyple project are Trademarks of the Eclipse Foundation, Inc.
* Eclipse® is a Trademark of the Eclipse Foundation, Inc.
* Eclipse Foundation is a Trademark of the Eclipse Foundation, Inc.

## Copyright and license

Copyright 2020 the [Eclipse Foundation, Inc.](https://www.eclipse.org) and 
the [Keyple Java authors](https://github.com/eclipse/keyple-java/graphs/contributors). 
Code released under the [Eclipse Public License Version 2.0 (EPL-2.0)](https://github.com/eclipse/keyple-java/blob/src/LICENSE).
