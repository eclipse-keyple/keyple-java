# Use Case #7 : Pool Reader Server Side with Web Sync Network Protocol (Web Service) example

This project aims at demonstrating how simple is the implementation of the synchronous mode of the **Keyple Distributed** solution with a Web Service.
This configuration allows a **client** application to control a **pool** of smart cards readers available on a **server** (e.g. HSM readers).
See the [developer guide](https://keyple.org/docs/developer-guide/distributed-application/#pool-reader-server-side) for more details.

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
It produces the `UseCase7_PoolReaderServerSide_Webservice-1.0.0-runner.jar` file in the `build` directory.
Be aware that it is a uber-jar as the dependencies are copied inside the jar.

The application is now runnable using `java -jar build/UseCase7_PoolReaderServerSide_Webservice-1.0.0-runner.jar`.

## About Quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .
