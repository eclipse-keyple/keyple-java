**Using the Keyple Core library**
---

**Resources common to all Keyple Core demonstration examples**

`org.eclipse.keyple.example.common.generic`

* The purpose of this package is to demonstrate the use of the Core library.
  * Reader management:
    * Observability of plugins to manage the insertion / removal of readers
    * Observability of readers to manage the insertion / removal of secure elements
  * SeRequestSet/SeResponseSet building
  * PO technology identification (protocol based filtering)
  * Application selection (AID or ATR based)
  * Two basic scenarios
    * Plugins and readers monitoring
    * PO selection

* Package content

|File|Description|
|---|---|
|`Demo_ObservableReaderNotificationEngine.java`|This class provides all the mechanisms to implement to perform the observability operations.<br>It is independent of the platform.|

**PC platform**

`org.eclipse.keyple.example.pc.generic`

* This package contains two implementations of the Keyple Core demonstration on a PC platform.
  * Real mode with PC/SC readers (Secure Elements required [Calypso and/or others])
  * Simulation mode (virtual Secure Elements included)

* Package content

|File|Description|
|---|---|
|`Demo_ObservableReaderNotification_Pcsc.java`|Contains the main class for the PC/SC demo|
|`Demo_ObservableReaderNotification_Stub.java`|Contains the main class for the same demo without the need of hardware readers|
|`stub.se/StubSe1.java`|Virtual SE (`StubSecureElement`)|
|`stub.se/StubSe2.java`|Virtual SE (`StubSecureElement`)|

**Using the Keyple Calypso library**
---

**Resources common to all Keyple Calypso demonstration examples**

`org.eclipse.keyple.example.common.calypso`
* The purpose of this package is to demonstrate the use of the Calypso library.
  * Dual reader configuration (PO and CSM)
  * PO Secure Session management
  * Basic scenario for ticketing

* Package content

|File|Description|
|---|---|
|`Demo_HoplinkTransactionEngine.java`|This class provides all the mechanisms to implement to perform a ticketing scenario with a full PO Secure Session.<br>It is independent of the platform.
|`HoplinkInfoAndSampleCommands.java`|This class provides Hoplink data elements.|

**PC platform**

`org.eclipse.keyple.example.pc.calypso`
* This package contains two implementations of the Keyple Calypso demonstration on a PC platform.
  * Real mode with PC/SC readers (Calypso Secure Elements required [PO and CSM])
  * Simulation mode (virtual Secure Elements included)

* Package content

|File|Description|
|---|---|
|`Demo_Hoplink_Pcsc.java`|Contains the main class for the PC/SC demo|
|`PcscReadersSettings.java`|Interface defining PC/SC settings to identify readers|
|`Demo_Hoplink_Stub.java`|Contains the main class for the same demo without the need of hardware readers|
|`stub.se/CsmStubSe.java`|Virtual Hoplink PO (`StubSecureElement`)|
|`stub.se/HoplinkStubSe.java`|Virtual CSM (`StubSecureElement`)|
