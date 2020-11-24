Getting Started - Calypso Example
---
Those projects showcases the calypso protocol among multiple readers in local and remote architecture.  

## Local Example
The [local examples](/java/example/calypso/local) project make use of the Keyple Calypso library with local plugins. They demonstrate how to select a Calypso application and execute Calypso secure Transaction. We use a PCSC plugin for real smartcard a Stub Plugin to simulates Calypso Smart Card. They show case the [PCSC plugin](/java/keyple-plugin/pcsc) and the [Stub plugin](/java/keyple-plugin/stub). 
    
**The purpose of these packages is to demonstrate the use of the Calypso library:**

  * Dual reader configuration (PO and SAM)
  * PO Secure Session management
  * Default application selection
  * Explicit application selection

## Remote Example
The [remote example](/java/example/calypso/remote) project demonstrates how easy is the implementation of the Remote plugin. It is used to communicate with a smartcard inserted on a remote reader hosted on another device.

## Android Example
[Android Application](/java/example/calypso/android) : NFC and OMAPI application examples. They show case the [Android NFC plugin](/android/keyple-plugin/android-nfc) and the [Android OMAPI plugin](/android/keyple-plugin/android-omapi)
