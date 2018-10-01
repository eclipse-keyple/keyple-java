**Using the Keyple Core library**
---

**These examples involve two packages**

- Resources common to all Keyple Core demonstration examples

      `org.eclipse.keyple.example.common.generic`
- PC platform launchers

      `org.eclipse.keyple.example.pc.generic`
      
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

* Four launchers working out of the box on a PC platform

  * Plugin and reader observability
    * Real mode with PC/SC readers (Secure Elements required [Calypso and/or others])
    * Simulation mode (virtual Secure Elements included)
  * PO type detection through the use of the protocol flag mechanism
    * Real mode with PC/SC readers (Secure Elements required [Calypso and/or others])
    * Simulation mode (virtual Secure Elements included)
