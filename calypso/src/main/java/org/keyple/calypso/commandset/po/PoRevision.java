package org.keyple.calypso.commandset.po;

/**
 * This enumeration registers the Calypso revisions of PO.
 *
 * @author Ixxi
 */
public enum PoRevision {

	
    
    /** The cmd session open classe */
    CLASS_0x80((byte) 0x80),
	
	
	/** The cmd aid current DF class */
	CLASS_0x94((byte) 0x94),
	
	/** The Close Secure Session class */
	CLASS_0x00((byte) 0x00),

	REV2_4((byte) 0x94),
	
	REV3_1((byte) 0x00),
	
	REV3_2((byte) 0x00);
	
    private byte cla;

    private PoRevision(byte cla) {
        this.cla = cla;
    }

    public byte getCla() {
        return cla;
    }
}
