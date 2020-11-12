# Keyple Remote Plugin (Core library)

The **Keyple Remote Plugin** allows a terminal to communicate with a "local" reader plugged into another terminal.

In a Calypso context, it is useful when your SAM reader and/or your PO reader aren't connected to the same terminal. With the **Keyple Remote Plugin**, you can open Calypso transaction within a distributed architecture.

## Table of Contents

* [Overview](#overview)
* [About Core library](#about-core-library)
* [How to use it ?](#how-to-use-it-)
* [Use cases](#use-cases)
    * [Remote Server Plugin](#remote-server-plugin)
    * [Remote Client Plugin](#remote-client-plugin)
    * [Remote Pool Client Plugin](#remote-pool-client-plugin)
* [Public Global API](#public-global-api)
* [Network configuration](#network-configuration)
    * [Full-Duplex Asynchronous communication](#full-duplex-asynchronous-communication)
    * [Client-Server Synchronous communication](#client-server-synchronous-communication)
* [Exchanged data](#exchanged-data)
* [Annexes](#annexes)
    * [Remote Server Plugin API (class diagram)](#remote-server-plugin-api-class-diagram)
    * [Remote Client Plugin API (class diagram)](#remote-client-plugin-api-class-diagram)
    * [Remote Pool Client Plugin API (class diagram)](#remote-pool-client-plugin-api-class-diagram)
    * [Remote Server Plugin (sequence diagram)](#remote-server-plugin-sequence-diagram)
    * [Internal Class Diagram](#internal-class-diagram)

## Overview

The **Keyple Remote Plugin** is divided into two main libraries, and one common library :
* The **[Local](../local/README.md)** library (`keyple-plugin-remote-local`) : This library must be used by the application installed on the terminal having local access to the reader.
* The **[Remote](../remote/README.md)** library (`keyple-plugin-remote-remote`) : This library must be used by the application installed on the terminal not having local access to the reader and that wishes to control the reader remotely.
* The **Core** library (`keyple-plugin-remote-core`) : The common library.

## About Core library

The **Core** library contains all the common components used by **Local** and **Remote** libraries such as nodes used for communication management.

> Therefore, you do not have to import it explicitly because it's imported by transitivity.

## How to use it ?

1. Read first [Overview](#overview) & [About Core library](#about-core-library) chapters.
2. Find your use case with the help of chapter [Use cases](#use-cases). This will help you determine exactly which interfaces to use.
3. Import [Local](../local/README.md) and/or [Remote](../remote/README.md) libraries depending on your use case.
4. Using chapter [Network configuration](#network-configuration), you must implement the transport layer using the sequence diagram adapted to your network configuration.
5. Implement your ticketing services using the interfaces detailed in chapter [Public API](#annexes).

## Use cases

### Remote Server Plugin

![Remote_Component_RemoteServerPlugin_SeReader_API](../../../../../docs/img/remote/component/Remote_Component_RemoteServerPlugin_SeReader_API.svg)

![Remote_Component_RemoteServerPlugin_ObservableReader_API](../../../../../docs/img/remote/component/Remote_Component_RemoteServerPlugin_ObservableReader_API.svg)

### Remote Client Plugin

![Remote_Component_RemoteClientPlugin_SeReader_API](../../../../../docs/img/remote/component/Remote_Component_RemoteClientPlugin_SeReader_API.svg)

![Remote_Component_RemoteClientPlugin_ObservableReader_API](../../../../../docs/img/remote/component/Remote_Component_RemoteClientPlugin_ObservableReader_API.svg)

![Remote_Component_RemoteClientObservablePlugin_SeReader_API](../../../../../docs/img/remote/component/Remote_Component_RemoteClientObservablePlugin_SeReader_API.svg)

![Remote_Component_RemoteClientObservablePlugin_ObservableReader_API](../../../../../docs/img/remote/component/Remote_Component_RemoteClientObservablePlugin_ObservableReader_API.svg)

### Remote Pool Client Plugin

![Remote_Component_RemotePoolClientPlugin_SeReader_API](../../../../../docs/img/remote/component/Remote_Component_RemotePoolClientPlugin_SeReader_API.svg)

## Public Global API

![Remote_Class_API](../../../../../docs/img/remote/class/Remote_Class_API.svg)

## Network configuration

Usually distributed architecture will rely on a TCP/IP network to communicate. It is up to the users to choose which protocol to use on top of it. The Remote Plugin does not provide the network implementation, but it provides a set of interfaces to be implemented.

### Full-Duplex Asynchronous communication

If you want to implement a Full-Duplex communication protocol, such as Web Sockets for example, then you should use:
* on **client**, the `KeypleClientAsyncNode` node and provide an implementation of the `KeypleClientAsync` endpoint interface;
* on **server**, the `KeypleServerAsyncNode` node and provide an implementation of the `KeypleServerAsync` endpoint interface;

![Remote_Sequence_AsyncNode_API](../../../../../docs/img/remote/sequence/Remote_Sequence_AsyncNode_API.svg)

### Client-Server Synchronous communication

If you want to implement a Client-Server communication protocol, such as standard HTTP for example, then you should use:
* on **client**, the `KeypleClientSyncNode` node and provide an implementation of the `KeypleClientSync` endpoint interface;
* on **server**, the `KeypleServerSyncNode` node;

![Remote_Sequence_SyncNode_API](../../../../../docs/img/remote/sequence/Remote_Sequence_SyncNode_API.svg)

## Exchanged data

The POJO object `KeypleMessageDto` contains data exchanged between **Local** and **Remote** components. It is built and processed by the plugin, and you don't need to modified it.

## Annexes

### Remote Server Plugin API (class diagram)

![Remote_Class_RemoteServerPlugin_API](../../../../../docs/img/remote/class/Remote_Class_RemoteServerPlugin_API.svg)

### Remote Client Plugin API (class diagram)

![Remote_Class_RemoteClientPlugin_API](../../../../../docs/img/remote/class/Remote_Class_RemoteClientPlugin_API.svg)

### Remote Pool Client Plugin API (class diagram)

![Remote_Class_RemotePoolClientPlugin_API](../../../../../docs/img/remote/class/Remote_Class_RemotePoolClientPlugin_API.svg)

### Remote Server Plugin (sequence diagram)

![Remote_Sequence_RemoteServerPlugin_API](../../../../../docs/img/remote/sequence/Remote_Sequence_RemoteServerPlugin_API.svg)

### Internal Class Diagram

![Remote_Class](../../../../../docs/img/remote/class/impl/Remote_Class.svg)
