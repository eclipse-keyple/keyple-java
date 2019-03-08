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

- Use Case ‘Calypso 1’ – Explicit Selection Aid
    - Scenario:
        - Check if a ISO 14443-4 SE is in the reader, select a Calypso PO, operate a simple Calypso PO transaction (simple plain read, not involving a Calypso SAM).
        - ‘Explicit Selection’ means that it is the terminal application which start the SE processing.
        - PO messages:
            - A first SE message to select the application in the reader
            - A second SE message to operate the simple Calypso transaction
        - Implementations:
            - For PC/SC plugin: UseCase_Calypso1_ExplicitSelectionAid_Pcsc
            - For Stub plugin: UseCase_Calypso1_ExplicitSelectionAid_Stub
            - For OMAPI plugin: UseCase_Calypso1_ExplicitSelectionAid_Omapi
            - Not supported by the Android NFC plugin, because the NFC reader could be managed only on SE detection.

- Use Case ‘Calypso 2’ – Default Selection Notification
    - Scenario:
        - Define a default selection of ISO 14443-4 Calypso PO and set it to an observable reader, on SE detection in case the Calypso selection is successful, notify the terminal application with the PO information, then the terminal follows by operating a simple Calypso PO transaction.
        - ‘Default Selection Notification’ means that the SE processing is automatically started when detected.
        - PO messages:
            - A first SE message to notify about the selected Calypso PO
            - A second SE message to operate the simple Calypso transaction
        - Implementations:
            - For PC/SC plugin: UseCase_Calypso2_DefaultSelectionNotification_Pcsc
            - For Stub plugin: UseCase_Calypso2_DefaultSelectionNotification_Stub
            - For NFC plugin: UseCase_Calypso2_DefaultSelectionNotification_Nfc
            - Not supported by the Android OMAPI plugin, because an OMAPI reader isn’t observable.

* Available packages in details:

  - `org.eclipse.keyple.example.calypso.common.transaction` and `org.eclipse.keyple.example.calypso.common.postructure`

|File|Description|
|:---|---|
|`CalypsoClassicInfo.java`|This class provides Calypso data elements (files definitions).|
|`CalypsoClassicTransactionEngine.java`|This class provides all the mechanisms to implement to perform a ticketing scenario with a basic Calypso Secure Session.<br>It is independent of the platform.|
|`HoplinkInfo.java`|This class provides Hoplink data elements (files definitions).|
|`HoplinkTransactionEngine.java`|This class provides all the mechanisms to implement to perform a ticketing scenario with an Hoplink Secure Session.<br>It is independent of the platform.|

  - `org.eclipse.keyple.example.calypso.common.stub.se`

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
|`UseCase_Calypso1_ExplicitSelectionAid_Stub.java`|Explicit Selection with a Stub reader (virtual SE and reader)|
|`UseCase_Calypso2_DefaultSelectionAid_Pcsc.java`|Default Selection with a PC/SC reader|
|`UseCase_Calypso2_DefaultSelectionAid_Stub.java`|Default Selection with a Stub reader (virtual SE and reader)|
