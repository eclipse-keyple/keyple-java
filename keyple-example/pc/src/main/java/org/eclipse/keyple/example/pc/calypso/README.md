**Using the Keyple Calypso library**
---

**These examples involve two packages**

- Resources common to all Keyple Calypso demonstration examples

      `org.eclipse.keyple.example.common.calypso.transaction`
      `org.eclipse.keyple.example.common.calypso.postructure`
- PC platform launchers

      `org.eclipse.keyple.example.pc.calypso`
* The purpose of this package is to demonstrate the use of the Calypso library.

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
