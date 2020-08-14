# Keyple - Release Notes

Releases:
 - Alpha [Keyple Java 20/next](#keyple-java-20next)
 - [Patch - Keyple Java 20/05](#patch---keyple-java-2005)
 - [Master - Keyple Java 20/01](#master---keyple-java-2001)
 - [Master - Keyple Java 19/07](#master---keyple-java-1907)


## Keyple Java 20/next
 - [keyple-java-core 0.9a](#keyple-java-core-09a)
 - [keyple-java-calypso 0.9a](#keyple-java-calypso-09a)
 - [keyple-java-plugin-pcsc 0.9a](#keyple-java-plugin-pcsc-09a)
 - [keyple-java-plugin-stub 0.9a](#keyple-java-plugin-stub-09a)
 - [keyple-java-plugin-remotese 0.9a](#keyple-java-plugin-remotese-09a)
 - [keyple-android-plugin-android-nfc 0.9a](#keyple-android-plugin-android-nfc-09a)
 - [keyple-android-plugin-android-omapi 0.9a](#keyple-android-plugin-android-omapi-09a)

### keyple-java-core 0.9a
* KEYP-314 : reviewed the channel control management: notifySeProcessed is removed
* KEYP-312 : restricting access to methods of the reader API that are not used by the plugins.
* KEYP-108, 213 : improved plugin lifecycle management (SeProxyService, PluginFactory)
* KEYP-145 : improved command builders and parsers to manage erroneous status word with dedicated exceptions
* KEYP-154,295 : improved exceptions management, all exceptions are now runtime exceptions
* KEYP-243,250,251 : improvement of the classes from the selection package (deep refactoring and simplification)
* KEYP-245 : improved isolation in the plugin API
* KEYP-202, 241, 242 : fixed misused Set fields (changed into List), simplified ProxyReader API (less methods)
* KEYP-225 : SeResource constructor is now public
* fixed a comparison issue in the Tag class
* various other refactorings and improvements
* fixed many issues raised by Sonarqube
 
### keyple-java-calypso 0.9a
* KEYP-314 : simplified the channel control management: a new prepareReleasePoChannel command is added to PoTransaction 
* KEYP-306 : added a default value to SeProtocol in PoSelector and SamSelector (specifying it is now optional)
* KEYP-311 : added the possibility to unlock the SAM when it is selected
* KEYP-62 : added Invalidate and Rehabilitate commands
* KEYP-96 : added Stored Value management commands
* KEYP-23 : added PIN management commands
* KEYP-101 : enhanced CalypsoPo with card data (updated during the transaction)
* KEYP-102 : update the content of CalypsoPo during selection
* KEYP-103,113 : extensive reworking of the PoTransaction class (split into 3 classes). The data received from the PO are now made available in CalypsoPo instead of the previous parser system. 
* KEYP-134,216 : extensive reworking of the selection classes. The data received from the PO are now made available in CalypsoPo.
* KEYP-145 : extensive reworking of command builders and parsers 
* KEYP-154 : improved exceptions management, add many exception cases
* KEYP-290 : fixed NPE when opening a secure session without reading with rev 2.4 PO
* KEYP-292 : make CalypsoPo and CalypsoSam classes extendable
* KEYP-225 : PoResource and SamResource classes are removed, their usage is replaced by SeResource<CalypsoPo/CalypsoSam>
* KEYP-306 : SE protocol is now optional when building PoSelector and SamSelector. Its default value is ISO_14443_4 and ISO7816_3 respectively.
* updated with the new keyple-java-core API
* various other refactorings and improvements
* fixed many issues raised by Sonarqube

### keyple-java-plugin-pcsc 0.9a
### keyple-java-plugin-stub 0.9a
* any parameters can be set in the stub reader
### keyple-java-plugin-remotese 0.9a
### keyple-android-plugin-android-nfc 0.9a
* Conversion of the Java code to Kotlin
* Removal of Supports Library usage
* AndroidNFC plugin now support Android 9.0+ remove SE detection
* Updated versions of Android sdk support and dependencies
* Various improvements of Android NFC example application
### keyple-android-plugin-android-omapi 0.9a
* Conversion of the Java code to Kotlin
* Removal of Supports Library usage
* OMAPI compliance improvement to the SIM Alliance 
* OMAPI implementation and to the Android 9.0 native OMAPI implementation.
* OMAPI Plugin may now support NEXT instruction on SimAlliance OMAPI 3+
* Updated versions of Android sdk support and dependencies
* Various improvements of OMAPI's example application


## Patch - Keyple Java 20/05

Release train components:
 - [keyple-java-core 0.8.1](#keyple-java-core-081)
 - [keyple-java-calypso 0.8.1](#keyple-java-calypso-081)
 - [keyple-java-plugin-pcsc 0.8.1](#keyple-java-plugin-pcsc-081)
 - [keyple-java-plugin-stub 0.8.1](#keyple-java-plugin-stub-081)
 - [keyple-java-plugin-remotese 0.8.1](#keyple-java-plugin-remotese-081)
 - [keyple-android-plugin-nfc 0.8.1](#keyple-android-plugin-nfc-081)
 - [keyple-android-plugin-omapi 0.8.1](#keyple-android-plugin-omapi-081)

### keyple-java-core 0.8.1
* KEYP-187 : ReaderPoolPlugin#allocate throw a KeypleAllocationReaderException if allocation fails for technical issue, ReaderPoolPlugin#allocate throw a KeypleAllocationNoReaderAvailableException if no reader is available
* KEYP-192 : Reorganize plugin and reader abstract classes
* KEYP-195 : Ensure readers list is thread safe 

### keyple-java-calypso 0.8.1
* KEYP-187 : SamResourceManager throw a CalypsoNoSamResourceAvailableException if unable to allocate a Sam Resource
* KEYP-189 : SamResourceManager : retry mechanism is now parametrized - new constructor added

### keyple-java-plugin-pcsc 0.8.1
* none

### keyple-java-plugin-stub 0.8.1
* KEYP-207 : add KeypleAllocationReaderException to stub plugin

### keyple-java-plugin-remotese 0.8.1
* KEYP-190 : fix NullPointerException on RemoteMethodTxEngine when remoteMethodTx is null
* KEYP-203 : fix ConcurrentModificationException while disconnecting/connecting readers
* KEYP-204 : add 'error' field in Keyple Dto, reject dto response if id does not match with request
* KEYP-208 : fix ClassCastException in VirtualReaderImpl
* KEYP-212 : allow users to define their own ExecutorService for async task - new constructor for MasterAPI and SlaveAPI 
* KEYP-212 : add a createdDate in virtual reader session
* KEYP-219 : ensure load balancing works properly : fix sessionId in RmPoolReleaseTx, RmPoolAllocateExecutor use node Id

### keyple-android-plugin-nfc 0.8.1
* none

### keyple-android-plugin-omapi 0.8.1
* KEYP-195 : update init readers method to use ConcurrentSkipListSet


## Master - Keyple Java 20/01

Release train components:
 - [keyple-java-core 0.8](#keyple-java-core-08)
 - [keyple-java-calypso 0.8](#keyple-java-calypso-08)
 - [keyple-java-plugin-pcsc 0.8](#keyple-java-plugin-pcsc-08)
 - [keyple-java-plugin-stub 0.8](#keyple-java-plugin-stub-08)
 - [keyple-java-plugin-remotese 0.8](#keyple-java-plugin-remotese-08)
 - [keyple-android-plugin-nfc 0.8](#keyple-android-plugin-nfc-08)
 - [keyple-android-plugin-omapi 0.8](#keyple-android-plugin-omapi-08)

 
### keyple-java-core 0.8
* Se Proxy API
  * Plugins need to be registered to the SeProxyService with an PluginFactory with a unique plugin name
* Reader monitoring
  * Redesign of the abstract classes inherited by plugin objects
  * Fix for a clean stop of the observation when removing a reader
* Secure Element monitoring
  * Add a new API to start/stop the SE Detection methods in the ObservableReader interface
  * Redesign of the abstract classes inherited by reader objects
  * Abstract Local Reader should now declare a behaviour for each of their state via with AbstractObservableState
  * Added 4 ObservableState : WaitForSeInsertion, WaitForSeProcessing, WaitForSeRemoval, WaitForStartDetect
  * Added MonitorigingJob objects to launch a background task in a AbstractObservableState
  * SE_REMOVAL event renamed SE_REMOVED
* NoStackTraceThrowable exception removed
* Secure Element logical and physical channels management
  * Deletion of the SeRequestSet class replaced by Set<SeRequest>
  * Changes in the management of card channels: the closure of the channel is no longer attached to a SeRequest but operated independently
  * ChannelState renamed ChannelControl
  * Update of the ProxyReader interface: addition of variants of the "transmit" method with control of the multi-request mode and channel closing
  * Update of the DefaultSelectionRequest and DefaultSelectionsResponse classes
* Addition of the MultiSeRequestProcessing enum used in the "transmit" method to indicate the procedure to be followed during the selection phase

### keyple-java-calypso 0.8
* Update of the selection and transaction classes to take the new channel management into account
* Addition of SAM key management commands, & PO Write Record command.
* Fixed a bug in the parser of the response to Select File

### keyple-java-plugin-pcsc 0.8
* Added PcscPluginFactory to register plugin to SeProxy
* Added PcscPlugin interface, make PcscPluginImpl package protected
* Added PcscReader interface, make PcscReaderImpl package protected
* Added a work around the stop of the Smart Card service under Windows has been made specific to this platform by detecting the OS
* Added a specific behaviour for card detection on MacOs

### keyple-java-plugin-stub 0.8
* Added StubPluginFactory and StubPoolPluginFactory to register plugin to SeProxy
* StubPlugin and StubPoolPlugin should be instantiated via the factory with a unique plugin name
* Added StubPlugin/StubPoolPlugin interface, make StubPluginImpl/StubPoolPluginImpl package protected
* Added StubReader interface, make StubReaderImpl package protected

### keyple-java-plugin-remotese 0.8
* Added VirtualObservableReader interface for Remote Se Observable Reader
* Added a DefaultTransportDto with basic behaviour
* RemoteMethod renamed to RemoteMethodName
* RemoteSePlugin should be instantiated via the factory with a unique plugin name
* RemoteSePoolPlugin should be instantiated via the factory with a unique plugin name
* Added AndroidNfcPluginFactory  to register plugin to SeProxy
* Added RemoteSePlugin interface, make RemoteSePluginImpl package protected
* Added VirtualReader interface, make RemoteSeReaderImpl package protected
* Added RemoteSePoolPlugin interface, make RemoteSePoolPluginImpl package protected

### keyple-android-plugin-nfc 0.8
* Added AndroidNfcPluginFactory to register plugin to SeProxy
* Added AndroidNfcPlugin interface, make AndroidNfcPluginImpl package protected
* Added AndroidNfcReader interface, make AndroidNfcReaderImpl package protected
* Added a Se Removal detection mecanism via CardAbsentPingMonitoringJob  in WaitForSeRemoval state

### keyple-android-plugin-omapi 0.8
* Added AndroidOmapiPluginFactory to register plugin to SeProxy
* Added AndroidOmapiPlugin interface, make AndroidOmapiPluginImpl package protected
* Added AndroidOmapiReader interface, make AndroidOmapiReaderImpl package protected

### build tools :
* Added Gradle wrapper in java root project and android root project
* Added Gradle Keyple tasks in root project to install/remove all artefacts at once: installAll, removeAll
* CI : move jenkins and docker files to a new repository : https://github.com/eclipse/keyple-ops

___

## Master - Keyple Java 19/07

Release train components:
 - [keyple-java-core 0.7](#keyple-java-core-07)
 - [keyple-java-calypso 0.7](#keyple-java-calypso-07)
 - [keyple-java-plugin-pcsc 0.7](#keyple-java-plugin-pcsc-07)
 - [keyple-java-plugin-stub 0.7](#keyple-java-plugin-stub-07)
 - [keyple-java-plugin-remotese 0.7](#keyple-java-plugin-remotese-07)
 - [keyple-android-plugin-nfc 0.7](#keyple-android-plugin-nfc-07)
 - [keyple-android-plugin-omapi 0.7](#keyple-android-plugin-omapi-07)

### keyple-java-core 0.7
* SE Proxy API: generic universal interface to interface a smartcard reader; transmission of grouped APDU commands; observable reader notification
* Transaction API: management of an explicit application selection on a reader; setting of a default selection operation on an observed reader
* Known issues/limits 
  * Allows the cast of ReaderPlugin & SeReader native classes: could cause a misusage of the API by ticketing application. A ReaderPlugin factory would be necessary for the setting of plugins to the SE Proxy Service.
  * The support for non-ISO Secure Element (memory cards) is limited.
  * Selecting an already selected SE raises a null pointer exception (#73). The re-selection checking has weaknesses (#51). A time-based mechanism should be added to avoid multiple selections.

### keyple-java-calypso 0.7
* Low level API: support of the main Calypso PO & SAM commands
* Transaction API; management a Calypso secure session
* Known issues/limits 
  * the Calypso PO test in contact mode has yet to be performed
  * PO session has to be improved with automatic setting and security counter measures.  
  * Missing the Calypso CLAp automatic setting, the support of the PIN, Stored Value, Confidential session, & Data Signature features.

### keyple-java-plugin-pcsc 0.7
* Interface the Java SmartCardIO API
* Known issues/limits 
  * PcscPlugin has a public visibility.
  * In order to deal with a problem with the implementation of smartcard.io on Windows, we use a hack using reflective code that can induce new problems: compilation on the next Java platforms, strange behavior on other environments (Linux, embedded Linux) See https://stackoverflow.com/questions/16921785/smartcard-terminal-removal-scard-e-no-service-cardexception

### keyple-java-plugin-stub 0.7
* Reader plugin emulator: allows to define virtual readers to operate the SE Proxy API without native readers.
* Known issues/limits: 
  * StubPlugin has a public visibility.
  * Some tests have a dependency with the Calypso library.

### keyple-java-plugin-remotese 0.7
* Plugin to communicate remotely with a Secure Element; definition of MasterAPI and SlaveAPI to enable Remote Procedure Call (RPC) between Keyple virtual reader terminals and Keyple native reader terminals. MasterAPI manages virtual reader that acts as a proxy to the remote reader.
* Known limits in 0.7.0 
  * RemoteSePlugin has a public visibility.
  * virtualReader#SePresent() API is not implemented
  * Functions to be renamed in accordance with the RPC standard

### keyple-android-plugin-nfc 0.7
* Plugin interfacing an observable reader operated through the Android NFC API.
* Known issues/limits: 
  * AndroidNfcPlugin has a public visibility.
  * Usage of 'fragment' to remove from the plugin code
  * SE remove detection capability added in Android 7.0 not yet supported.

### keyple-android-plugin-omapi 0.7
* Plugin interfacing static readers operated through the SIM Alliance / GlobalPlatform OMAPI.
* Known issues/limits: 
  * OmapiPlugin has a public visibility.
  * Usage of 'fragment' to remove from the plugin code
  * package API renamed from Android 9.0 not yet supported.
