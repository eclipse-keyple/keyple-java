/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.command;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.core.seproxy.exception.KeypleSeCommandException;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * Base class for parsing APDU
 */
public abstract class AbstractApduResponseParser {

    /** the byte array APDU response. */
    protected ApduResponse response;

    protected static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        HashMap<Integer, StatusProperties> m = new HashMap<Integer, StatusProperties>();
        m.put(0x9000, new StatusProperties("Success", null));
        STATUS_TABLE = m;
    }

    /**
     * Get the internal status table
     * 
     * @return Status table
     */
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    /**
     * the generic abstract constructor to build a parser of the APDU response.
     *
     * @param response response to parse
     */
    public AbstractApduResponseParser(ApduResponse response) {
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
     * @return the properties associated to the response status code
     */
    private StatusProperties getStatusCodeProperties() {
        return getStatusTable().get(response.getStatusCode());
    }

    /**
     * Checks if is successful.
     *
     * @return if the status is successful from the statusTable according to the current status
     *         code.
     */
    public boolean isSuccessful() {
        StatusProperties props = getStatusCodeProperties();
        return props != null && props.isSuccessful();
    }

    /**
     * Gets the status information.
     *
     * @return the ASCII message from the statusTable for the current status code.
     */
    public final String getStatusInformation() {
        StatusProperties props = getStatusCodeProperties();
        return props != null ? props.getInformation() : null;
    }

    /**
     * Status code properties
     */
    protected static class StatusProperties {

        /** The status information */
        private final String information;

        /** The successful indicator */
        private final boolean successful;

        /** The associated exception class in case of error status */
        private final Class<? extends KeypleSeCommandException> exceptionClass;

        /**
         * Create a successful status.
         *
         * @param information the status information
         */
        public StatusProperties(String information) {
            this.information = information;
            this.successful = true;
            this.exceptionClass = null;
        }

        /**
         * Create an error status.<br>
         * If {@code exceptionClass} is null, then a successful status is created.
         *
         * @param information the status information
         * @param exceptionClass the associated exception class
         */
        public StatusProperties(String information,
                Class<? extends KeypleSeCommandException> exceptionClass) {
            this.information = information;
            this.successful = exceptionClass == null;
            this.exceptionClass = exceptionClass;
        }

        /**
         * @return the status information
         */
        public String getInformation() {
            return information;
        }

        /**
         * @return the successful indicator
         */
        public boolean isSuccessful() {
            return successful;
        }

        /**
         * @return the nullable exception class
         */
        public Class<? extends KeypleSeCommandException> getExceptionClass() {
            return exceptionClass;
        }
    }
}
