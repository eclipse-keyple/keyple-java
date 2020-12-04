# Keyple Remote Plugins - Remote Lib

## Overview

The **Remote Lib** Java library is one of the two main libraries provided by the **Keyple Remote Plugins** solution (the other one is [Local Lib](../local/README.md)).

It must be imported and used by the application installed on the terminal not having local access to the smart card reader and that wishes to control the reader remotely.

It is important to note that it uses and exposes the APIs exposed by the library [Network Lib](../network/README.md).

## User Guide & Download Information

The full documentation, including download information is available in the [Developer Guides / Develop a Remote Ticketing Application](http://keyple.org/docs/developer-guide/develop-ticketing-app-remote/) section of the official Keyple website [keyple.org](http://keyple.org)

## Build the Code

The code is built with **Gradle** and is compliant with **Java 1.6** in order to be able to be used by a very large number of applications.

## Code Contributions

We welcome code contributions through merge requests. Please help us enhance the plugin !

In addition to the APIs class diagram available on the official website, you can find the internal API class diagram on the [Network Lib](../network/README.md) repository.
