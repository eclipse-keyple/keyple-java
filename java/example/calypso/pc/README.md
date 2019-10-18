Getting Started - Calypso Example
---

Those examples make use of the Keyple Calypso library. They demonstrate how to select a Calypso application and execute Calypso secure Transaction. We use a PCSC plugin for real smartcard a Stub Plugin to simulates Calypso Secure Element. 

**These examples involve several sets of packages**

- Resources common to all Keyple Calypso demonstration examples

      `org.eclipse.keyple.example.common.calypso.pc.transaction`
      `org.eclipse.keyple.example.common.calypso.postructure`
- PC platform launchers

      `org.eclipse.keyple.example.calypso.pc.*`
   
**The purpose of these packages is to demonstrate the use of the Calypso library:**

  * Dual reader configuration (PO and SAM)
  * PO Secure Session management
  * Default application selection
  * Explicit application selection
  

Ten launchers can be run independently

  * Classic Calypso Transaction (use of PoSecure session) [`Demo_CalypsoClassic`]
    * Real mode with PC/SC readers (Calypso Secure Elements required [_PO and SA_]) [`Demo_CalypsoClassic_Pcsc.java`]
    * Simulation mode (Stub Secure Elements included) [`Demo_CalypsoClassic_Stub.java`]
  * Use case Calypso Authentication: open/close Secure Session only [`UseCase_Calypso4_PoAuthentication`]
    * Real mode with PC/SC readers [`UseCase_Calypso4_PoAuthentication_Pcsc.java`]
    * Simulation mode  (Stub Secure Elements included) [`UseCase_Calypso4_PoAuthentication_Stub.java`]
  * Use case Multiple Session: illustrates the multiple session generation mechanism for managing the sending of modifying commands that exceed the capacity of the session buffer.  [`UseCase_Calypso5_MultipleSession`]
    * Real mode with PC/SC readers [`UseCase_Calypso5_MultipleSession_Pcsc.java`]
  
  * Use Case ‘Calypso 1’ – Explicit Selection Aid [`UseCase_Calypso1_ExplicitSelectionAid`]
    * Check if a ISO 14443-4 SE is in the reader, select a Calypso PO, operate a simple Calypso PO transaction (simple plain read, not involving a Calypso SAM).
    * _Explicit Selection_ means that it is the terminal application which start the SE processing.
    * PO messages:
        * A first SE message to select the application in the reader
        * A second SE message to operate the simple Calypso transaction
    * Implementations:
        * For PC/SC plugin: [`UseCase_Calypso1_ExplicitSelectionAid_Pcsc.java`]
        * For Stub plugin: [`UseCase_Calypso1_ExplicitSelectionAid_Stub`]
            
  * Use Case ‘Calypso 2’ – Default Selection Notification [`UseCase_Calypso2_DefaultSelectionNotification`]
    * Define a default selection of ISO 14443-4 Calypso PO and set it to an observable reader, on SE detection in case the Calypso selection is successful, notify the terminal application with the PO information, then the terminal follows by operating a simple Calypso PO transaction.
    * _Default Selection Notification_ means that the SE processing is automatically started when detected.
    * PO messages:
         * A first SE message to notify about the selected Calypso PO
         * A second SE message to operate the simple Calypso transaction
    * Implementations:
         * For PC/SC plugin: [`UseCase_Calypso2_DefaultSelectionNotification_Pcsc.java`]
         * For Stub plugin: [`UseCase_Calypso2_DefaultSelectionNotification_Stub.java`]

Available packages in details:
--

  - `org.eclipse.keyple.example.common.calypso.pc.transaction` and `org.eclipse.keyple.example.common.calypso.postructure`

|File|Description|
|:---|---|
|`CalypsoClassicInfo.java`|This class provides Calypso data elements (files definitions).|
|`CalypsoClassicTransactionEngine.java`|This class provides all the mechanisms to implement to perform a ticketing scenario with a basic Calypso Secure Session.<br>It is independent of the platform.|
|`HoplinkInfo.java`|This class provides Hoplink data elements (files definitions).|
|`HoplinkTransactionEngine.java`|This class provides all the mechanisms to implement to perform a ticketing scenario with an Hoplink Secure Session.<br>It is independent of the platform.|

  - `org.eclipse.keyple.example.common.generic.stub`

|File|Description|
|:---|---|
|`StubCalypsoClassic.java`|Calypso PO stub SE (`StubSecureElement`)|
|`StubSam.java`|Calypso SAM stub SE (`StubSecureElement`)|
|`StubHoplink.java`|Hoplink PO stub SE (`StubSecureElement`)|

  - `org.eclipse.keyple.example.calypso.pc`

|File|Description|
|:---|---|
|`Demo_CalypsoClassic_Pcsc.java`|Contains the main class for the Calypso PC/SC demo|
|`Demo_CalypsoClassic_Stub.java`|Contains the main class for the Calypso basic without the need of hardware readers|
|`Demo_Hoplink_Pcsc.java`|Contains the main class for the Hoplink PC/SC demo|
|`UseCase_CalypsoAuthenticationLevel3_Pcsc.java`|Contains the main class of an example of code focusing on session opening/closing|
|`UseCase_MultipleSession_Pcsc.java`|Contains the main class of an example of code focusing on the multiple session mode (for handling large amount of data transferred to a PO)|
|`UseCase_Calypso1_ExplicitSelectionAid_Pcsc.java`|Explicit Selection with a PC/SC reader|
|`UseCase_Calypso1_ExplicitSelectionAid_Stub.java`|Explicit Selection with a Stub reader (stub SE and reader)|
|`UseCase_Calypso2_DefaultSelectionAid_Pcsc.java`|Default Selection with a PC/SC reader|
|`UseCase_Calypso2_DefaultSelectionAid_Stub.java`|Default Selection with a Stub reader (stub SE and reader)|
|`UseCase_Calypso3_Rev1Selection_Pcsc.java`|B' Selection with a PC/SC reader|
|`UseCase_Calypso4_PoAuthentication_Pcsc.java`|Execute a Calypso Transaction with a PC/SC reader|
|`UseCase_Calypso4_PoAuthentication_Stub.java`|Execute a Calypso Transaction with a Stub reader|
|`UseCase_Calypso5_MultipleSession_Pcsc.java`|Execute a Calypso Transaction containing multiple modifications with a PC/SC reader|
