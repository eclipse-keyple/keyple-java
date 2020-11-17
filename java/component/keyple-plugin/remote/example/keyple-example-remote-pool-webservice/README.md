# Pool Remote Plugin Client with Web Service example

This project aims at demonstrating how simple is the implementation of the synchronous mode of the PoolRemotePluginClient`. This plugin allows a client application to control a pool of smart card readers available on a server (e.g. HSM readers); see the [developer guide](https://calypsonet.github.io/keyple-website/docs/developer-guide/develop-ticketing-app-remote/#poolremotepluginclient) for more details.

This example uses the keyple `StubPoolPlugin` that emulates a pool of smart card readers, allowing to allocate, release readers. Instead of a ``StubPoolPlugin``, you can configure any plugin that implements the interface ``ReaderPoolPlugin``.

Running the example, a complete scenario is executed: 
- http server starts up
- client and server components are initialized
- client requests a remote reader allocation which results on server allocating a reader from the pool plugin
- client executes a complete transaction with the remote reader
- client requests to release the reader, then server releases the reader from the pool plugin
- http  server shutdowns

## Running the example

This example is based on the Quarkus framework for the http server. To execute the example, you need to install Quarkus dependencies : 
- JDK 1.8+ installed with JAVA_HOME configured appropriately
- Gradle installed

You can run the application with the gradle command : 
```
./gradlew runExample
```

This gradle command starts the Quarkus server in dev mode that enables live coding.

For the matter of this example, the client app is included within the quarkus server. Nevertheless, it can be deployed in another device. 

## Packaging and running the application

The application can be packaged using `./gradlew quarkusBuild`.
It produces the `keyple-example-remote-pool-web-service-1.0.0-SNAPSHOT-runner.jar` file in the `build` directory.
Be aware that it is a _über-jar_ as the dependencies are copied inside the jar.

The application is now runnable using `java -jar build/keyple-example-remote-pool-web-service-1.0.0-SNAPSHOT-runner.jar`.

## About Quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .
