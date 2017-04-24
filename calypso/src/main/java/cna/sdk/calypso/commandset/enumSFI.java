package cna.sdk.calypso.commandset;

/**
 * This enumeration registers the minimum types of file contained in a Calypso
 * app.
 *
 * @author Ixxi
 */
public enum enumSFI {

    /** The ICC file. */
    FREE_FILE("FreeFile", (byte) 0x01),

    /** The ICC file. */
    ICC_FILE("IccFile", (byte) 0x02),

    /** The ID file. */
    ID_FILE("IdFile", (byte) 0x03),

    /** The unknown file. */
    UNKNOWN_FILE("unknownFile", (byte) 0x05),

    /** The ODMemory file. */
    ODMEMORY_FILE("unknownFile", (byte) 0x06),

    /** The environment file. */
    ENVIRONMENT_FILE("environmentFile", (byte) 0x07),

    /** The event log file. */
    EVENT_LOG_FILE("eventLogFIle", (byte) 0x08),

    /** The contract file. */
    CONTRACT_FILE("contractFile", (byte) 0x09),

    /** The counter file. */
    COUNTER_FILE("counterFile", (byte) 0x19),

    /** The simulated counter file 1 */
    SIMULATED_COUNTER_FILE_1("simulatedCounterFile1", (byte) 0x0A),
    /** The simulated counter file 2 */
    SIMULATED_COUNTER_FILE_2("simulatedCounterFile2", (byte) 0x0B),
    /** The simulated counter file 3 */
    SIMULATED_COUNTER_FILE_3("simulatedCounterFile3", (byte) 0x0C),
    /** The simulated counter file 4 */
    SIMULATED_COUNTER_FILE_4("simulatedCounterFile4", (byte) 0x0D),
    /** The simulated counter file 5 */
    SIMULATED_COUNTER_FILE_5("simulatedCounterFile5", (byte) 0x0E),
    /** The simulated counter file 6 */
    SIMULATED_COUNTER_FILE_6("simulatedCounterFile6", (byte) 0x0F),
    /** The simulated counter file 7 */
    SIMULATED_COUNTER_FILE_7("simulatedCounterFile7", (byte) 0x10),
    /** The simulated counter file 8 */
    SIMULATED_COUNTER_FILE_8("simulatedCounterFile8", (byte) 0x12),
    /** The simulated counter file 9 */
    SIMULATED_COUNTER_FILE_9("simulatedCounterFile9", (byte) 0x13),

    /** The counter file. */
    SUPPLEMENTARY_COUNTER_FILE("supplementaryCounterFile", (byte) 0x10),

    /** The holder file. */
    HOLDER_FILE("holderFile", (byte) 0x1C),

    /** The special event file. */
    SPECIAL_EVENT_FILE("specialEventFile", (byte) 0x1D),

    /** The contract list file. */
    CONTRACT_LIST_FILE("contractListFile", (byte) 0x1E);

    /** The name. */
    private final String name;

    /** The sfi. */
    private final byte sfi;

    /**
     * Instantiates a new enumSFI.
     *
     * @param name
     *            the name
     * @param sfi
     *            the sfi
     */
    private enumSFI(String name, byte sfi) {
        this.name = name;
        this.sfi = sfi;
    }

    /**
     * Gets the name.
     *
     * @return the name of the type
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the sfi.
     *
     * @return the sfi byte of the type
     */
    public byte getSfi() {
        return sfi;
    }

    /**
     *
     *
     * @param code
     *            the code
     * @return the corresponding type of file
     */
    public static enumSFI getSfiByCode(byte code) {
        for (enumSFI el : enumSFI.values()) {
            if (el.getSfi() == code) {
                return el;
            }
        }
        return enumSFI.UNKNOWN_FILE;
    }

}
