# Use Case #1 : Reader Server Side with Sync Network Protocol (Web Service) example

This projects aims at demonstrating how simple is the implementation of the synchronous mode of the **Keyple Distributed** solution with a Web Service protocol.
This configuration allows a **server** application to control a smart card reader available on a **client** (e.g. PO reader).
See the [developer guide](https://keyple.org/docs/developer-guide/distributed-application/#reader-client-side).

The local reader is plugged on the client device which communicates with the server through web services.

Running the example, a complete scenario is executed: 
- quarkus server starts up
- client and server components are initialized
- a Stub calypso card is inserted in the local reader (client side)
- a remote transaction is executed by the server
- quarkus server shutdowns

By default, this example uses the keyple `StubPlugin` that emulates a **local reader** and a **smart card**. You can activate the PCSC configuration by invoking ```initPcscReader()``` instead of  ```initStubReader()``` in the `clientApp.init()` method.      

If you are interested in an asynchronous protocol example, checkout the websocket example.

## Running the example

This example is based on the Quarkus framework. To execute the example, you need to install Quarkus dependencies : 
- JDK 1.8+ installed with JAVA_HOME configured appropriately
- gradle

You can run the example in dev mode:

```
./gradlew runExample
```

This project depends on an external module located in `../common`. If you need a standalone module, you can copy manually all classes in the `common` module inside this project. Don't forget to delete references to the `common` module in the `gradle.properties` and `build.gradle` configuration files. 

## Packaging and running the application

The application can be packaged using `./gradlew quarkusBuild`.
It produces the `UseCase1_ReaderClientSide_Webservice-1.0.0-SNAPSHOT-runner.jar` file in the `build` directory.
Be aware that it is a _über-jar_ as the dependencies are copied inside the jar.

The application is now runnable using `java -jar build/UseCase1_ReaderClientSide_Webservice-1.0.0-SNAPSHOT-runner.jar`.

## About Quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .
