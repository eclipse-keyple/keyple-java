package org.keyple.calypso.commandset.dto;


/**
 * The Class KVC.
 * KVC: Key Version and Category. Arbitrary value identifying a key among several of the same
type.
 */
public class KVC {
	
	/** The value. */
	private byte value;

	/**
	 * Instantiates a new KVC.
	 *
	 * @param value the value
	 */
	public KVC(byte value) {
		super();
		this.value = value;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public byte getValue() {
		return value;
	}

}
