package org.keyple.calypso.commandset.csm;

/**
 * This enumeration registers all revisions of CSM.
 *
 * @author Ixxi
 */
public enum CsmRevision {

    /** The revision of C1 and S1E SAM. */
    C1((byte) 0x80),

    /** The revision of S1D SAM SAM. */
    S1D((byte) 0x94);

    private byte cla;

    private CsmRevision(byte cla) {
        this.cla = cla;
    }

    public byte getCla() {
        return cla;
    }

}
