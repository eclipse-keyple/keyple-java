package cna.sdk.calypso.commandset;

/**
 * This enumeration registers all modes for select application command.
 *
 * @author Ixxi
 */
public enum enumCmdSelectApplication {

	/** The select first occurence return fci. */
	SELECT_FIRST_OCCURENCE_RETURN_FCI("Select first occurrence and return the FCI",(byte)0x00),
	
	/** The select next occurence return fci. */
	SELECT_NEXT_OCCURENCE_RETURN_FCI("Select next occurrence and return the FCI",(byte)0x02),
	
	/** The select first occurence no return fci. */
	SELECT_FIRST_OCCURENCE_NO_RETURN_FCI("Select first occurrence and do not return the FCI optional mode",(byte)0x0C),
	
	/** The select next occurence no return fci. */
	SELECT_NEXT_OCCURENCE_NO_RETURN_FCI("Select next occurrence and do not return the FCI optional mode",(byte)0x0E);

	/** The name. */
	private String name;
	
	/** The tag byte P 2. */
	private byte tagbyteP2;
	
	/**
	 * Instantiates a new enumCmdSelectApplication.
	 *
	 * @param name the name
	 * @param tagbyteP2 the tag byte P 2
	 */
	private enumCmdSelectApplication(String name, byte tagbyteP2) {
		this.name = name;
		this.tagbyteP2 = tagbyteP2;
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
	 * Gets the tag byte P 2.
	 *
	 * @return the tag byte P 2
	 */
	public byte getTagbyteP2() {
		return tagbyteP2;
	}
	
	
	
}
