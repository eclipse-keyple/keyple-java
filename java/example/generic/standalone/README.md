Getting Started - Generic Local Example
---

Those examples make use of the Keyple Core library. They demonstrate how to observe insertion/removal of smart card or Readers.

**These examples involve several packages**

- Resources common to all Keyple Core demonstration examples

      `org.eclipse.keyple.example.generic.standalone.common`
      
- Local platform launchers

      `org.eclipse.keyple.example.generic.standalone.*`
      
**The purpose of these examples is to demonstrate the use of the Core library**

  * Reader management:
    * Observability of plugins to manage the insertion / removal of readers
    * Observability of readers to manage the insertion / removal of secure elements
  * SeRequest/SeResponse building
  * PO technology identification (protocol based filtering)
  * Application selection (AID or ATR based)
  * Two basic scenarios
    * Plugins and readers monitoring
    * PO selection

* Five launchers working out of the box on a java platform

  * Plugin and reader observability [`Demo_ObservableReaderNotification`]
    * Real mode with PC/SC readers (Secure Elements required [Calypso and/or others]) [`Main_ObservableReaderNotification_Pcsc.java`]
    * Simulation mode (virtual Secure Elements included) [`Main_ObservableReaderNotification_Stub.java`]
  * PO type detection through the use of the protocol flag mechanism [`Demo_CardProtocolDetection`] 
    * Real mode with PC/SC readers (Secure Elements required [Calypso and/or others]) [`Main_CardProtocolDetection_Pcsc.java`]
    * Simulation mode (virtual Secure Elements included) [`Main_CardProtocolDetection_Stub.java`]
  * Use case multiple select: illustrates the possibility of selecting multiple SE applications using the same AID prefix and P2 standard values to select the first or next occurrence. [`UseCase4_SequentialMultiSelection`]
  
Available packages in details:
--
  - `org.eclipse.keyple.example.generic.standalone.common`

|File|Description|
|:---|---|
|`StubMifareClassic.java`|Mifare Classic stub SE|
|`StubMifareDesfire.java`|Mifare Desfire stub SE|
|`StubMifareUL.java`|Mifare Ultralight stub SE|
|`StubSe1.java`|Generic stub SE 1|
|`StubSe2.java`|Generic stub SE 2|
|`GenericCardSelectionRequest.java`|This class provides a generic card selection request.|
|`PcscReaderUtils.java`|This class provides utilities for Pcsc configuration processing|

  - `org.eclipse.keyple.example.generic.standalone.*`

|File|Description|
|:---|---|
|`Demo_CardProtocolDetection.Main_CardProtocolDetection_Pcsc.java`|Main class for the protocol detection example (PC/SC)|
|`Demo_CardProtocolDetection.Main_CardrotocolDetection_Stub.java`|Main class for the protocol detection example (stub)|
|`Demo_ObservableReaderNotification.Main_ObservableReaderNotification_Pcsc.java`|Main class for the plugin/reader observability example (PC/SC)|
|`Demo_ObservableReaderNotification.Main_ObservableReaderNotification_Stub.java`|Main class for the plugin/reader observability example (stub)|
|`UseCase1_ExplicitSelectionAid.Main_ExplicitSelectionAid.java`|Operate an explicit Selection Aid (PC/SC)|
|`UseCase2_DefaultSelectionNotification.Main_DefaultSelectionNotification_Pcsc.java`|Configure a default Selection Notification (PC/SC)|
|`UseCase3_SequentialMultiSelection.Main_SequentialMultiSelection_Pcsc.java`|Illustrates the use of the select next mechanism|
|`UseCase4_GroupedMultiSelection.Main_GroupedMultiSelection_Pcsc.java`|Illustrates the use of the select next mechanism|
