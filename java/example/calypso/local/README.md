Getting Started - Calypso Local Example 
---

Those examples make use of the Keyple Calypso library. They demonstrate how to select a Calypso application and execute Calypso secure Transaction. We use a PCSC plugin for real smartcard a Stub Plugin to simulates Calypso Smart Card. 

   **The purpose of these packages is to demonstrate the use of the Calypso library:**

  * Dual reader configuration (PO and SAM)
  * PO Secure Session management
  * Default application selection
  * Explicit application selection
  

Ten launchers can be run independently

  * Classic Calypso Transaction (use of PoSecure session) : [Demo_CalypsoClassic](/java/example/calypso/pc/src/main/java/org/eclipse/keyple/example/calypso/local/Demo_CalypsoClassic)
    * Real mode with PC/SC readers (Calypso Secure Elements required [_PO and SAM_])
    * Simulation mode (Stub Secure Elements included)
  * Use case Calypso Authentication: open/close Secure Session only:  [UseCase4_PoAuthentication](/java/example/calypso/pc/src/main/java/org/eclipse/keyple/example/calypso/local/UseCase4_PoAuthentication)
    * Real mode with PC/SC readers [`UseCase4_PoAuthentication_Pcsc.java`]
    * Simulation mode  (Stub Secure Elements included) [`UseCase4_PoAuthentication_Stub.java`]
  * Use case Multiple Session: illustrates the multiple session generation mechanism for managing the sending of modifying commands that exceed the capacity of the session buffer. [UseCase5_MultipleSession](/java/example/calypso/pc/src/main/java/org/eclipse/keyple/example/calypso/local/UseCase5_MultipleSession)
    * Real mode with PC/SC readers [`UseCase5_MultipleSession_Pcsc.java`]
  * Use Case ‘Calypso 1’ – Explicit Selection Aid : [UseCase1_ExplicitSelectionAid](/java/example/calypso/pc/src/main/java/org/eclipse/keyple/example/calypso/local/UseCase4_PoAuthentication)
    * Check if a ISO 14443-4 SE is in the reader, select a Calypso PO, operate a simple Calypso PO transaction (simple plain read, not involving a Calypso SAM).
    * _Explicit Selection_ means that it is the terminal application which start the SE processing.
    * PO messages:
        * A first SE message to select the application in the reader
        * A second SE message to operate the simple Calypso transaction
    * Implementations:
        * For PC/SC plugin: [`UseCase1_ExplicitSelectionAid_Pcsc.java`]
        * For Stub plugin: [`UseCase1_ExplicitSelectionAid_Stub`]         
  * Use Case ‘Calypso 2’ – Default Selection Notification [UseCase2_DefaultSelectionNotification](/java/example/calypso/pc/src/main/java/org/eclipse/keyple/example/calypso/local/UseCase2_DefaultSelectionNotification)
    * Define a default selection of ISO 14443-4 Calypso PO and set it to an observable reader, on SE detection in case the Calypso selection is successful, notify the terminal application with the PO information, then the terminal follows by operating a simple Calypso PO transaction.
    * _Default Selection Notification_ means that the SE processing is automatically started when detected.
    * PO messages:
         * A first SE message to notify about the selected Calypso PO
         * A second SE message to operate the simple Calypso transaction
    * Implementations:
         * For PC/SC plugin: [`UseCase2_DefaultSelectionNotification_Pcsc.java`]
         * For Stub plugin: [`UseCase2_DefaultSelectionNotification_Stub.java`]

Available packages in details:
--

  - `org.eclipse.keyple.example.calypso.local.common`

|File|Description|
|:---|---|
|`CalypsoClassicInfo.java`|This class provides Calypso data elements (files definitions).|
|`CalypsoUtils.java`|This class provides utilities for Calypso processing|
|`PcscReaderUtils.java`|This class provides utilities for Pcsc configuration processing|
|`StubCalypsoClassic.java`|Calypso PO stub  (`StubSmartCard`)|
|`StubSamCalypsoClassic.java`| Calypso SAM stub SE (`StubSmartCard`)|

  - `org.eclipse.keyple.example.calypso.local.*`

|File|Description|
|:---|---|
|`Demo_CalypsoClassic_Pcsc.java`|Contains the main class for the Calypso PC/SC demo|
|`Demo_CalypsoClassic_Stub.java`|Contains the main class for the Calypso basic without the need of hardware readers|
|`UseCase_CalypsoAuthenticationLevel3_Pcsc.java`|Contains the main class of an example of code focusing on session opening/closing|
|`UseCase_MultipleSession_Pcsc.java`|Contains the main class of an example of code focusing on the multiple session mode (for handling large amount of data transferred to a PO)|
|`UseCase1_ExplicitSelectionAid.ExplicitSelectionAid_Pcsc.java`|Explicit Selection with a PC/SC reader|
|`UseCase1_ExplicitSelectionAid.ExplicitSelectionAid_Stub.java`|Explicit Selection with a Stub reader (stub SE and reader)|
|`UseCase2_DefaultSelectionAid.DefaultSelectionAid_Pcsc.java`|Default Selection with a PC/SC reader|
|`UseCase2_DefaultSelectionAid.DefaultSelectionAid_Stub.java`|Default Selection with a Stub reader (stub SE and reader)|
|`UseCase3_Rev1Selection.Rev1Selection_Pcsc.java`|B' Selection with a PC/SC reader|
|`UseCase4_PoAuthentication.PoAuthentication_Pcsc.java`|Execute a Calypso Transaction with a PC/SC reader|
|`UseCase4_PoAuthentication.PoAuthentication_Stub.java`|Execute a Calypso Transaction with a Stub reader|
|`UseCase5_MultipleSession.MultipleSession_Pcsc.java`|Execute a Calypso Transaction containing multiple modifications with a PC/SC reader|
|`UseCase6_VerifyPin.VerifyPin_Pcsc.java`|Execute multiple successive presentations of PIN codes|
|`UseCase7_StoredValue_SimpleReload.StoredValue_SimpleReload_Pcsc.java`|Execute an out of secure session SV reload |
|`UseCase8_StoredValue_DebitInSession.StoredValue_DebitInSession_Pcsc.java`|Execute a SV debit within a secure session|
