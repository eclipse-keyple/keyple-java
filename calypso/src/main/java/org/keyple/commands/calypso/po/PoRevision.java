package org.keyple.commands.calypso.po;

/**
 * This enumeration registers the Calypso revisions of PO.
 *
 * @author Ixxi
 */
public enum PoRevision {

    REV2_4("Calypso Revision 2.4"), // cla 0x94

    REV3_1("Calypso Revision 3.1"), // cla 0x00

    REV3_2("Calypso Revision 3.2"); // cla 0x00

    private String name;

    private PoRevision(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
