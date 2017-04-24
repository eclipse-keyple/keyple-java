package cna.sdk.calypso.commandset.dto;

/**
 * The Class POHalfSessionSignature.
 * Half session signature return by a close secure session APDU command
 */
public class POHalfSessionSignature {
	
	/** The value. */
	private byte [] value;

	/**
	 * Instantiates a new POHalfSessionSignature.
	 *
	 * @param value the value
	 */
	public POHalfSessionSignature(byte[] value) {
		super();
		this.value = value;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public byte[] getValue() {
		return value;
	}
	
	

}
