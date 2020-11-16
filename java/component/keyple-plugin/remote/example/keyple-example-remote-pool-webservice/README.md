# Pool Remote Plugin Client with Web Service example

This project aims at demonstrating how straight forward is the implementation of the synchronous mode of the PoolRemotePluginClient` (see the [developer guide](https://calypsonet.github.io/keyple-website/docs/developer-guide/develop-ticketing-app-remote/#poolremotepluginclient))

This example uses the keyple `StubPoolPlugin` that emulates a local pool plugin, allowing to allocate a reader with a simulated card. Instead of a ``StubPoolPlugin``, you can configure any plugin that implements the interface ``ReaderPoolPlugin``.

Running the example, a complete scenario is executed: 
- quarkus server starts up
- client and server components are initialized
- client requests a remote reader allocation
- server allocate a reader from the pool plugin
- client executes a complete transaction with the remote reader
- client requests to release the reader
- server releases the reader from the pool plugin
- quarkus server shutdowns

## Running the example

This example is based on the Quarkus framework. To execute the example, you need to install Quarkus dependencies : 
- an IDE
- JDK 1.8+ installed with JAVA_HOME configured appropriately

You can run the application by the gradle command : 
```
./gradlew runExample
```

This task starts the Quarkus server in dev mode that enables live coding.

For the matter of this example, the client app is included within the quarkus server. Nevertheless, it can be deployed in another device. 

## Packaging and running the application

The application can be packaged using `./gradlew quarkusBuild`.
It produces the `keyple-example-remote-pool-web-service-1.0.0-SNAPSHOT-runner.jar` file in the `build` directory.
Be aware that it is a _über-jar_ as the dependencies are copied inside the jar.

The application is now runnable using `java -jar build/keyple-example-remote-pool-web-service-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./gradlew build -Dquarkus.package.type=native`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./build/keyple-example-remote-pool-web-service-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling#building-a-native-executable.

## About Quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .
