/*
 *
 */
package cna.sdk.calypso.commandset;

import java.util.HashMap;
import java.util.Map;

import cna.sdk.seproxy.APDUResponse;

/**
 * The Class ApduResponseParser.
 * This abstract class has to be extended by all PO and CSM response parser classes, it provides the generic getters to manage response’s status.
 * @author Ixxi
 *
 */
public abstract class ApduResponseParser {

    /** the byte array APDU response. */
    protected APDUResponse response;

    /** The status table. */
    protected Map<byte[], StatusProperties> statusTable = new HashMap<>();

    /**
     * Instantiates a new ApduResponseParser.
     */
    public ApduResponseParser() {

    }

    /**
     * the generic abstract constructor to build a parser of the APDU response.
     *
     * @param response
     *            response to parse
     */
    public ApduResponseParser(APDUResponse response) {
        this.response = response;
    }

    /**
     * Gets the apdu response.
     *
     * @return the ApduResponse instance.
     */
    public final APDUResponse getApduResponse() {
        return response;
    }

    /**
     * Checks if is successful.
     *
     * @return if the status is successful from the statusTable according to the
     *         current status code.
     */
    public boolean isSuccessful() {
        StatusProperties status = this.statusTable.get(response.getStatusCode());
        if (status != null) {
            return status.getSuccessful();
        }
        return false;
    }

    /**
     * Gets the status information.
     *
     * @return the ASCII message from the statusTable for the current status
     *         code.
     */
    public final String getStatusInformation() {
        StatusProperties status = this.statusTable.get(response.getStatusCode());
        if (status != null) {
            return status.getInformation();
        }
        return null;
    }

    /**
     * The Class StatusProperties.
     * inner Class
     * @author Ixxi
     *
     */
    protected class StatusProperties {

        /** The successful. */
        private boolean successful;

        /** The information. */
        private String information;

        /** A map with the double byte of a status as key, and the successful property and ASCII text information as data.
         * @param successful set successful status
         * @param information additional information
         */
        public StatusProperties(boolean successful, String information) {
            this.successful = successful;
            this.information = information;
        }

        /**
         * Gets the successful.
         *
         * @return the successful
         */
        public boolean getSuccessful() {
            return successful;
        }

        /**
         * Gets the information.
         *
         * @return the information
         */
        public String getInformation() {
            return information;
        }

    }
}
