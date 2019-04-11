# Remote SE Plugin

This project aims at implementing the concept of an interaction between a Terminal and a remote Secure Element. 

The Secure Element is inserted into a Reader connected to a Slave terminal, which exposes the SE to a Master Terminal that will pilot the interaction. The Slave terminal connects one or multiple Native Reader to the Master Terminal. The Master Terminal interacts with those readers as if they were local, in a sense they are virtual.  

The Remote SE plugin is deployed on the Master side to pilot interactions with Slave Terminals. Independent from the transport layer, the remote SE plugin is being prototyped with various transports layers (WebSocket, web service REST). 


### Slave terminal configuration

Like any Keyple projects, Native Readers are exposed to the SeProxyService via the ```ReaderPlugin``` API. In the case of Remote Se configuration, the Slave Terminal give control of one or multiple readers to the Master Terminal using the ```SlaveAPI``` API. While doing so, the connected readers are no longer piloted at the local level, all events are propagated to the Master Terminal. Only the Master Terminal is able to pilot the connected readers and their interactions with inserted Secure Element.  

To connect a native reader on the slave terminal use the API ```slaveAPI.connectReader(nodeId, reader, options)``` where ```nodeId``` is the terminal Id,```reader``` is the reader to connect and ```options``` is a map of options (not required for all readers)
```java
    //Get a reference to the ProxyReader (in this example, a stubReader)
    ProxyReader localReader = SeProxyService.getInstance().getReader("stubReaderTest");
    
    //Instanciate the SlaveAPI  with a DtoSender (needed to send message to Master, see below)
    SlaveAPI slaveAPI = new SlaveAPI(SeProxyService.getInstance(), node);
    
    //Connect the reader with a NodeId (terminalId) and a Map of options
    slaveAPI.connectReader(localReader, "slaveTerminal1");

```

### Master terminal configuration

From the Master terminal, a similar service is used: MasterAPI. Instantiate this Service with a NodeConfiguration, and wait for Slaves to connect to your Remote Plugin. PLUGIN EVENTS and READER EVENTS are received as if the remote readers were local.

This module contains the core of the Remote Se Plugin logic in both Master side and Client side.

Master side : 

```java
    //Instanciate the MasterAPI with a DtoSender (needed to send message to Slave, see below)
    MasterAPI masterAPI = new MasterAPI(SeProxyService.getInstance(), node);
    
    //Get the instanciate RemoteSe PLugin
    ReaderPlugin remoteSePlugin = masterAPI.getPlugin();
    
    //Observe the plugin for events like READER_CONNECTED or SE_INSERTED 
    ((Observable) remoteSePlugin).addObserver(this);

``` 


### Processing events from Master


Once the configuration of the MasterAPI done, events are sent to observers of the Remote Se Plugin as if connecting readers were local. 

```java

    
    /**
     * Receives Event from RSE Plugin
     * @param o : can be a ReaderEvent or PluginEvent
     */
    @Override
    public void update(final Object o) throws Exception {
        logger.debug("UPDATE {}", o);
        final DemoMaster masterThread = this;

        // Receive a PluginEvent
        if (o instanceof PluginEvent) {
            PluginEvent event = (PluginEvent) o;
            switch (event.getEventType()) {
                case READER_CONNECTED:
                    //a new virtual reader is connected, let's observe it
                        logger.info("Add ServerTicketingApp as a Observer of RSE reader");
                        remoteSEPlugin.getReader(event.getReaderName()).addObserver(masterThread);
                    break;
                //.. more events
            }
        }
        // ReaderEvent
        else if (o instanceof ReaderEvent) {
            ReaderEvent event = (ReaderEvent) o;
            switch (event.getEventType()) {
                case SE_INSERTED:
                     
                     //you code here
                    break;
            }
            //.. more events
        }
    }
    
    
```

### Configuring MasterAPI and SlaveAPI

While configuring both services in Master and Slaves sides, you need to provide two Interfaces so what they can discuss together. Each service sends and receives messages from the other side. Those messages are encoded into a defined format:```KeypleDto```Objects. To send and receive each Terminal will use a ```Dtosender``` Object to send KeypleDto messages and a ```DtoHandler``` to receive KeypleDto messages. 
When using your own implementation of the transport layer you need to implement both Interfaces on each side and bind them to the remote services. 

In the provided example, we use a unique interface to send and receive KeypleDtos object, the ````DtoNode```` interface that extends ```DtoSender``` and a embeds a ```DtoHandler``` . 

Examples : 
- The project org.eclipse.keyple.example.remote.websocket implements a transport layer based on web socket protocol where ```WskClient``` is a web socket client that implements ```TransportNode``` and ```WskServer``` is a web socket server that implements```TransportNode```. They are meant to exchange messages with each other, as they implement```TransportNode``` they can be used to configure the Remote Services of the Remote Plugin. 
- A similar example can be found in the org.eclipse.keyple.example.remote.wspolling package.


# Running the example

After building the project (follow the procedure in the top README.md), you can execute 6 examples of Remote SE plugin :   
- ``Demo_Webservice_MasterClient`` : Execute the demo with a webservice protocol, the master device uses the webservice client
- ``Demo_Webservice_MasterServer`` : Execute the demo with a webservice protocol, the master device uses the webservice server 
- ``Demo_WebserviceWithRetrofit_MasterClient`` : Execute the demo with a webservice protocol, the master device uses the webservice Retrofit client 
- ``Demo_WebserviceWithRetrofit_MasterServer`` : Execute the demo with a webservice protocol, the master device uses the webservice server
- ``DemoWsKMasterServer`` : Execute the demo with a websocket protocol, the master device uses the websocket server
- ``DemoWsKMasterClient`` : Execute the demo with a websocket protocol, the master device uses the websocket client
