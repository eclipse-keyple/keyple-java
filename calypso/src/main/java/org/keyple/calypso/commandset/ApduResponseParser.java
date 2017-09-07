/*
 *
 */
package org.keyple.calypso.commandset;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.keyple.calypso.utils.LogUtils;
import org.keyple.seproxy.APDUResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ApduResponseParser. This abstract class has to be extended by all
 * PO and CSM response parser classes, it provides the generic getters to manage
 * response’s status.
 *
 * @author Ixxi
 *
 */
public abstract class ApduResponseParser {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

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
     * 
     * @param responseToParse
     * @param mapStatusProperties
     */
    public ApduResponseParser(APDUResponse responseToParse, Map<byte[], StatusProperties> mapStatusProperties){
    	this.response = responseToParse;
    	this.statusTable = mapStatusProperties;
    }

    /**
     * the generic abstract constructor to build a parser of the APDU response.
     *
     * @param response
     *            response to parse
     */
    public ApduResponseParser(APDUResponse response) {
        logger.info("status : " + LogUtils.hexaToString(response.getStatusCode()));
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
        for (Entry<byte[], StatusProperties> it : this.statusTable.entrySet()) {
            if (Arrays.equals(it.getKey(), response.getStatusCode())) {
                return it.getValue().getSuccessful();
            }
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
        for (Entry<byte[], StatusProperties> it : this.statusTable.entrySet()) {
            if (Arrays.equals(it.getKey(), response.getStatusCode())) {
                return it.getValue().getInformation();
            }
        }
        return null;
    }

    /**
     * The Class StatusProperties. inner Class
     *
     * @author Ixxi
     *
     */
    protected class StatusProperties {

        /** The successful. */
        private boolean successful;

        /** The information. */
        private String information;

        /**
         * A map with the double byte of a status as key, and the successful
         * property and ASCII text information as data.
         *
         * @param successful
         *            set successful status
         * @param information
         *            additional information
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
