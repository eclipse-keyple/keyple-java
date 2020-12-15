# Keyple PCSC Plugin

## Overview

The **PCSC** plugin enables the use for PCSC smart card readers into Eclipse Keyple. It uses internally the [javax.smartcardio](https://docs.oracle.com/javase/7/docs/jre/api/security/smartcardio/spec/javax/smartcardio/package-summary.html) library and works on Windows, Linux and Mac OS. 

This library **should be import explicitly** in the project configuration.

## User Guide & Download Information

This plugin is used in a variety of [examples](/java/example/generic/local/) and also in the [quick start](https://keyple.org/docs/build-your-first-app/java-app/) section of the official Keyple website [keyple.org](http://keyple.org). You can find all the information you need in the Eclipse Keyple [documentation](http://keyple.org/docs) and in the [API reference](https://keyple.org/docs/api-reference/).

## Build the Code

The code is built with **Gradle** and is compliant with **Java 1.6** in order to be able to be used by a very large number of applications.

## Known issues on macOS

Because of the changes in macOS Big Sur, Java PC/SC implementation no longer works correctly: https://bugs.openjdk.java.net/browse/JDK-8255877
The workaround is to set the system property: ```sun.security.smartcardio.library=/System/Library/Frameworks/PCSC.framework/Versions/Current/PCSC``

## Code Contributions

We welcome code contributions through merge requests. Please help us enhance the plugin !
