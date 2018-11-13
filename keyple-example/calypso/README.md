**Using the Keyple Calypso library**
---

**These examples involve two set of packages and two complete Android projects**

- Resources common to all Keyple Calypso demonstration examples

      `org.eclipse.keyple.example.calypso.common.transaction`
      `org.eclipse.keyple.example.calypso.common.postructure`
- PC platform launchers

      `org.eclipse.keyple.example.calypso.pc`
- Android projects

  - NFC based project (https://developer.android.com/reference/android/nfc/package-summary)
  - OMAPI based project (https://developer.android.com/reference/android/se/omapi/package-summary)       
* The purpose of these packages is to demonstrate the use of the Calypso library.

  * Dual reader configuration (PO and SAM)
  * PO Secure Session management
  * Basic scenario for ticketing

* Six launchers working out of the box on a PC platform

  * Classic Calypso Transaction (use of PoSecure session) 
    * Real mode with PC/SC readers (Calypso Secure Elements required [PO and SAM]) [`Demo_CalypsoClassic_Pcsc.java`]
    * Simulation mode (virtual Secure Elements included) [`Demo_CalypsoClassic_Stub.java`]
  * Basic Hoplink Transaction (use of PoSecure session) 
    * Real mode with PC/SC readers (Hoplink Secure Elements required [PO and SAM]) [`Demo_Hoplink_Pcsc.java`]
    * Simulation mode (virtual Secure Elements included) [`Demo_Hoplink_Stub.java`]
  * Use case Calypso Authentication: open/close Secure Session only [`UseCase_CalypsoAuthenticationLevel3_Pcsc.java`]
    * Real mode with PC/SC readers
  * Use case Multiple Session: illustrates the multiple session generation mechanism for managing the sending of modifying commands that exceed the capacity of the session buffer.  [`UseCase_MultipleSession_Pcsc.java`]
    * Real mode with PC/SC readers

* Available packages in details:

  - `org.eclipse.keyple.example.calypso.common.transaction` and `org.eclipse.keyple.example.calypso.common.postructure`

|File|Description|
|:---|---|
|`CalypsoClassicInfo.java`|This class provides Calypso data elements (files definitions).|
|`CalypsoClassicTransactionEngine.java`|This class provides all the mechanisms to implement to perform a ticketing scenario with a basic Calypso Secure Session.<br>It is independent of the platform.|
|`HoplinkInfo.java`|This class provides Hoplink data elements (files definitions).|
|`HoplinkTransactionEngine.java`|This class provides all the mechanisms to implement to perform a ticketing scenario with an Hoplink Secure Session.<br>It is independent of the platform.|

  - `org.eclipse.keyple.example.calypso.pc.stub.se`

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
