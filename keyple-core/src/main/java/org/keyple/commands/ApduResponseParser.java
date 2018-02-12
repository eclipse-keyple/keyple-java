/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.keyple.seproxy.ApduResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class ApduResponseParser. This abstract class has to be extended by all PO and CSM response
 * parser classes, it provides the generic getters to manage response’s status.
 *
 * @author Ixxi
 *
 */
public abstract class ApduResponseParser {

    /** the byte array APDU response. */
    private ApduResponse response;

    /** The status table. */
    protected Map<byte[], StatusProperties> statusTable = new HashMap<byte[], StatusProperties>();


    /**
     * Instantiates a new apdu response parser.
     *
     * @param responseToParse APDUResponse to parse
     * @param mapStatusProperties informations for each status
     */
    protected ApduResponseParser(ApduResponse responseToParse,
            Map<byte[], StatusProperties> mapStatusProperties) {
        this.response = responseToParse;
        this.statusTable = mapStatusProperties;
    }

    /**
     * the generic abstract constructor to build a parser of the APDU response.
     *
     * @param response response to parse
     */
    public ApduResponseParser(ApduResponse response) {
        this.response = response;
    }

    /**
     * Gets the apdu response.
     *
     * @return the ApduResponse instance.
     */
    public final ApduResponse getApduResponse() {
        return response;
    }


    /**
     * Gets the status code.
     *
     * @return the status code
     */
    public final byte[] getStatusCode() {
        return response.getStatusCode();
    }

    /**
     * Checks if is successful.
     *
     * @return if the status is successful from the statusTable according to the current status
     *         code.
     */
    public final boolean isSuccessful() {
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
     * @return the ASCII message from the statusTable for the current status code.
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
         * A map with the double byte of a status as key, and the successful property and ASCII text
         * information as data.
         *
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
