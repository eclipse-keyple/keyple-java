package org.keyple.calypso.commandset;

/**
 *  This enumeration registers all types of write record commands.
 *
 * @author Ixxi
 */
public enum enumCmdWriteRecords {


	/** The write record. */
	WRITE_RECORD("writeRecord"),
	
	/** The write record using sfi. */
	WRITE_RECORD_USING_SFI("readRecordFromEFUsingSfi"),

	/** The append record. */
	APPEND_RECORD("appendRecord");
	
	/** The name. */
	private String name;

	/**
	 *  The generic constructor of enumCmdWriteRecords.
	 *
	 * @param name the name
	 */
	private enumCmdWriteRecords(String name) {
		this.name = name;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name of the type of readRecords command
	 */
	public String getName() {
		return name;
	}
}
