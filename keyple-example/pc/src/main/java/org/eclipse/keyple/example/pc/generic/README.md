**Using the Keyple Calypso library**
---

**These examples involve two packages**

- Resources common to all Keyple Calypso demonstration examples

      `org.eclipse.keyple.example.common.calypso`
- PC platform launchers

      `org.eclipse.keyple.example.pc.calypso`
* The purpose of this package is to demonstrate the use of the Calypso library.

  * Dual reader configuration (PO and CSM)
  * PO Secure Session management
  * Basic scenario for ticketing

* Four launchers working out of the box on a PC platform

  * Basic Calypso Transaction (use of PoSecure session) 
    * Real mode with PC/SC readers (Calypso Secure Elements required [PO and CSM]) [`Demo_CalypsoBasic_Pcsc.java`]
    * Simulation mode (virtual Secure Elements included) [`Demo_CalypsoBasic_Stub.java`]
  * Basic Hoplink Transaction (use of PoSecure session) 
    * Real mode with PC/SC readers (Hoplink Secure Elements required [PO and CSM]) [`Demo_Hoplink_Pcsc.java`]
    * Simulation mode (virtual Secure Elements included) [`Demo_Hoplink_Stub.java`]
