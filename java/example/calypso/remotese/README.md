Getting Started - Remote SE Example
---

This project aims at implementing the concept of executing a Calypso transaction between two distants terminal. 

One terminal is acting as the Master terminal, it will pilot the transaction. The other terminal acts as a Slave, it will proxy a local Reader to allow the Master terminal to communicate with any Secure Element inserted. 

Master and Slave communicates between each other through a transport layer that must be provided. In those examples, two tranport layer have been implemented :
- HTTP Web services with long polling
- Web socket 

Example 
---
All 4 examples execute the same scenario :
- the Slave terminal connects its local reader to the Master terminal
- the local reader is configured to select a given type of SE by default
- insert a matching SE is inserted, in response a Calypso Transaction is executed by the Master
- after a given time the SE is removed.

The ticketing logic is defined in the Master terminal, the slave Terminal acts only as a proxy. All SE inserted in the slave are accessible from the Master.

4 examples of Remote SE plugin :   
- ``Demo_WebserviceWithRetrofit_MasterClient`` : execute the demo with a Webservice protocol, the Master device uses a Retrofit client 
- ``Demo_WebserviceWithRetrofit_MasterServer`` : execute the demo with a webservice protocol, the Master device uses the webservice server
- ``DemoWsKMasterServer`` : execute the demo with a websocket protocol, the master device uses the websocket server
- ``DemoWsKMasterClient`` : execute the demo with a websocket protocol, the master device uses the websocket client

Executing the Example
---

###Requirements

Remotese examples requires a specific JDK in order to have the polling working correctly. 

It has been successfully tested with :
- ``jdk1.8.0_40``

Some issues have been acknowledged with the WebserviceWithRetrofit examples on a newer JDK :
- ``jdk1.8.0_231`` 
- ``adoptopenjdk-11.jdk``

###Run

Use the custom gradle tasks :
- ```./gradlew runWs_MasterClient``` to execute the demo with a Webservice protocol, the Master device uses a Retrofit client  
- ```./gradlew runWS_MasterServer``` to execute the demo with a webservice protocol, the Master device uses the webservice server
- ```./gradlew runWsK_MasterClient``` to execute the demo with a websocket protocol, the master device uses the websocket server
- ```./gradlew runWsK_MasterServer``` to execute the demo with a websocket protocol, the master device uses the websocket client
