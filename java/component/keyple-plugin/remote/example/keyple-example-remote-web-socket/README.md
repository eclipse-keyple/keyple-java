# Remote Plugin Server with Web Socket example

This projects aims at demonstrating how straight forward is the implementation of the asynchronous mode of the Remote Plugin Server (see the [developer guide](https://calypsonet.github.io/keyple-website/docs/developer-guide/develop-ticketing-app-remote/#remotepluginserver)).

The native reader is plugged on the client device which communicates with the server through a web socket.

Running the example, a complete scenario is executed :
- quarkus server starts up
- client and server components are initialized
- a Stub calypso Card is inserted in the native reader (client side)
- a remote transaction is executed by the server
- quarkus server shutdowns

By default, this example uses the keyple `StubPlugin` that emulates a Native Reader and a Secure Element. You can activate the PCSC configuration by invoking ```initPcscReader()``` instead of  ```initStubReader()``` in the `clientApp.init()` method.      


## Running the example

This example is based on the Quarkus framework. To execute the example in dev mode, you need to install Quarkus dependencies : 
- an IDE
- JDK 1.8+ installed with JAVA_HOME configured appropriately

You can run your application in dev mode that enables live coding using:
```
./gradlew runExample
```

This module depends on an external module located in `../common`. If you need a standalone module, you can copy manually all classes in the `common` module inside this project. Don't forget to delete references to the `common` module in the `gradle.properties` and `build.gradle` configuration files. 

If you are interested in an example presenting asynchronous mode, checkout the websocket example.

## Packaging and running the application

The application can be packaged using `./gradlew quarkusBuild`.
It produces the `keyple-example-remote-web-socket-1.0.0-SNAPSHOT-runner.jar` file in the `build` directory.
Be aware that it is a _über-jar_ as the dependencies are copied inside the jar.

The application is now runnable using `java -jar build/keyple-example-remote-web-socket-1.0.0-SNAPSHOT-runner.jar`.


## Creating a native executable

You can create a native executable using: `./gradlew build -Dquarkus.package.type=native`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./build/keyple-example-remote-web-socket-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling#building-a-native-executable.

## About Quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .
