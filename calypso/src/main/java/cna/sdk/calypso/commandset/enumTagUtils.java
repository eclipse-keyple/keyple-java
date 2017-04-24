package cna.sdk.calypso.commandset;

/**
 * This enumeration registers all tags for Get Data commands.
 *
 * @author Ixxi
 */
public enum enumTagUtils {

	/** The fci template. */
	FCI_TEMPLATE("fciTemplate",(byte)0x00, (byte) 0x6F),

	/** The df name. */
	DF_NAME("dfName",(byte)0x00, (byte) 0x84),

	/** The fci proprietary template. */
	FCI_PROPRIETARY_TEMPLATE("fciProprietaryTemplate",(byte)0x00, (byte) 0xA5),

	/** The fci issuer discretionary data. */
	FCI_ISSUER_DISCRETIONARY_DATA("fciIssuerDiscretionaryData", (byte) 0xBF, (byte) 0x0C),

	/** The application serial number. */
	APPLICATION_SERIAL_NUMBER("applicationSerialNumber", (byte)0x00,(byte) 0xC7),

	/** The discreationary data. */
	DISCREATIONARY_DATA("DiscreationaryData",(byte)0x00, (byte) 0x53),

	/** The aid of current df. */
	AID_OF_CURRENT_DF("AidOfCurrentDF", (byte)0x00,(byte) 0x4F),

	/** The list of ef. */
	LIST_OF_EF("listOfEF",(byte)0x00, (byte) 0xC0),

	/** The list of aid. */
	LIST_OF_AID("listOfAID", (byte)0x00,(byte) 0xD0),

	/** The ef descriptor. */
	EF_DESCRIPTOR("efDescriptor", (byte)0x00,(byte) 0xC1),

	/** The historical bytes. */
	HISTORICAL_BYTES ("historicalbytes", (byte) 0x5F, (byte) 0x52),

	/** The traceability information. */
	TRACEABILITY_INFORMATION ("traceabilityInformation", (byte) 0x01, (byte) 0x85),

	/** The fcp for the current file. */
	FCP_FOR_THE_CURRENT_FILE("fcpForTheCurrentFile",(byte)0x00,(byte)0x62);

	/** The name. */
	private String name;

	/** The tag byte 1. */
	private byte tagbyte1;

	/** The tag byte 2. */
	private byte tagbyte2;

	/**
	 * Instantiates a new enumTagUtils.
	 *
	 * @param name the name
	 * @param tagbyte1 the tag byte 1
	 * @param tagbytedeux the tag byte 2
	 */
	enumTagUtils(String name, byte tagbyte1, byte tagbyte2){
	this.name = name;
	this.tagbyte1 = tagbyte1;
	this.tagbyte2 = tagbyte2;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the tag byte un.
	 *
	 * @return the tag byte un
	 */
	public byte getTagbyte1() {
		return tagbyte1;
	}

	/**
	 * Gets the tag byte deux.
	 *
	 * @return the tag byte deux
	 */
	public byte getTagbyte2() {
		return tagbyte2;
	}

	/**
	 * Gets the tag by name.
	 *
	 * @param name the name
	 * @return the tag by name
	 */
	public static enumTagUtils getTagByName(String name) {
        for (enumTagUtils el : enumTagUtils.values()) {
            if (el.getName().equals(name)) {
                return el;
            }
        }
        return null;
    }

}

