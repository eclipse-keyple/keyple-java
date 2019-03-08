**Using the Keyple Core library**
---

**These examples involve two packages**

- Resources common to all Keyple Core demonstration examples

      `org.eclipse.keyple.example.generic.common`
- PC platform launchers

      `org.eclipse.keyple.example.generic.pc`
      
* The purpose of these examples is to demonstrate the use of the Core library.

  * Reader management:
    * Observability of plugins to manage the insertion / removal of readers
    * Observability of readers to manage the insertion / removal of secure elements
  * SeRequestSet/SeResponseSet building
  * PO technology identification (protocol based filtering)
  * Application selection (AID or ATR based)
  * Two basic scenarios
    * Plugins and readers monitoring
    * PO selection

* Five launchers working out of the box on a PC platform

  * Plugin and reader observability
    * Real mode with PC/SC readers (Secure Elements required [Calypso and/or others]) [`Demo_ObservableReaderNotification_Pcsc.java`]
    * Simulation mode (virtual Secure Elements included) [`Demo_ObservableReaderNotification_Stub.java`]
  * PO type detection through the use of the protocol flag mechanism
    * Real mode with PC/SC readers (Secure Elements required [Calypso and/or others]) [`Demo_SeProtocolDetection_Pcsc.java`]
    * Simulation mode (virtual Secure Elements included) [`Demo_SeProtocolDetection_Stub.java`]
  * Use case select next: illustrates the possibility of selecting multiple SE applications using the same AID prefix and P2 standard values to select the first or next occurrence. [`UseCase_SelectNext_Pcsc.java`]
  
* Available packages in details:
  - `org.eclipse.keyple.example.generic.common`

|File|Description|
|:---|---|
|`CustomProtocools.java`|Custom protocol definition|
|`CustomProtocolSetting.java`|Custom protocols list|
|`ObservableReaderNotificationEngine.java`|This class provides all the mechanisms to implement to perform the plugin/reader events handling.|
|`SeProtocolDetectionEngine.java`|This class provides all the mechanisms to implement to perform the protocol detection.|
|`DemoHelpers.java`|Helper class (reader management method)|

  - `org.eclipse.keyple.example.generic.pc`

|File|Description|
|:---|---|
|`Demo_ObservableReaderNotification_Pcsc.java`|Main class for the plugin/reader observability example (PC/SC)|
|`Demo_ObservableReaderNotification_Stub.java`|Main class for the plugin/reader observability example (stub)|
|`Demo_SeProtocolDetection_Pcsc.java`|Main class for the protocol detection example (PC/SC)|
|`Demo_SeProtocolDetection_Stub.java`|Main class for the protocol detection example (stub)|
|`PcscReadersSettings.java`|Interface defining PC/SC readers identifiers (for PO / SAM reader role assignment)|

  - `org.eclipse.keyple.example.generic.pc.stub.se`

|File|Description|
|:---|---|
|`StubISO14443_4.java`|ISO 14443-4 stub SE|
|`StubCalypsoBPrime.java`|Calypso B Prime stub SE|
|`StubMemoryST25.java`|ST25 Memory stub SE|
|`StubMifareClassic.java`|Mifare Classic stub SE|
|`StubMifareDesfire.java`|Mifare Desfire stub SE|
|`StubMifareUL.java`|Mifare Ultralight stub SE|
|`StubSe1.java`|Generic stub SE 1|
|`StubSe2.java`|Generic stub SE 2|
