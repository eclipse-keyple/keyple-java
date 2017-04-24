package cna.sdk.calypso.commandset.po;

/**
 * This enumeration registers the Calypso revisions of PO.
 *
 * @author Ixxi
 */
public enum PoRevision {

    /** The r2 4. */
    REV2_4((byte) 0x94),

    /** The r3 1. */
    REV3_1((byte) 0x00),

    /** The r3 2. */
    REV3_2((byte) 0x00);

    private byte cla;

    private PoRevision(byte cla) {
        this.cla = cla;
    }

    public byte getCla() {
        return cla;
    }
}
