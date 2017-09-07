package org.keyple.calypso.commandset;

/**
 * This enumeration registers all types of readRecords commands.
 *
 * @author Ixxi
 */
public enum enumCmdReadRecords {

	/** The read one record. */
	READ_ONE_RECORD("readOneRecord"),
	
	/** The read records. */
	READ_RECORDS("readRecords"),
	
	/** The read one record from ef using sfi. */
	READ_ONE_RECORD_FROM_EF_USING_SFI("readOneRecordFromEFUsingSfi"),
	
	/** The read records from ef using sfi. */
	READ_RECORDS_FROM_EF_USING_SFI("readRecordsFromEFUsingSfi");


	/** The name. */
	private String name;

	/**
	 *  The generic constructor of enumCmdReadRecords.
	 *
	 * @param name the name
	 */
	private enumCmdReadRecords(String name) {
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
	
	/**
	 * Gets the cmd by name.
	 *
	 * @param name the name
	 * @return the cmd by name
	 */
	public static enumCmdReadRecords getCmdByName(String name) {
        for (enumCmdReadRecords el : enumCmdReadRecords.values()) {
            if (el.getName().equals(name)) {
                return el;
            }
        }
        return null;
    }
}
