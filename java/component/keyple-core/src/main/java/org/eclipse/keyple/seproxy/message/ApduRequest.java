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
package org.eclipse.keyple.seproxy.message;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.keyple.util.ByteArrayUtils;


/**
 * Single APDU request wrapper
 */
public final class ApduRequest implements Serializable {

    static final long serialVersionUID = 877369841119873812L;


    /**
     * Buffer of the APDU Request
     */
    private byte[] bytes;

    /**
     * a ‘case 4’ flag in order to explicitly specify, if it’s expected that the APDU command
     * returns data → this flag is required to manage revision 2.4 Calypso Portable Objects and
     * ‘S1Dx’ SAMs that presents a behaviour not compliant with ISO 7816-3 in contacts mode (not
     * returning the 61XYh status).
     */
    private final boolean case4;

    /**
     * List of status codes that should be considered successful although they are different from
     * 9000
     */
    private final Set<Integer> successfulStatusCodes;

    /**
     * Name of the request being sent
     */
    private String name;



    /**
     * the constructor called by a ticketing application in order to build the APDU command requests
     * to push to the ProxyReader.
     *
     * @param buffer Buffer of the APDU request
     * @param case4 the case 4
     * @param successfulStatusCodes the list of status codes to be considered as successful although
     *        different from 9000
     */
    public ApduRequest(byte[] buffer, boolean case4, Set<Integer> successfulStatusCodes) {
        this.bytes = buffer;
        this.case4 = case4;
        this.successfulStatusCodes = successfulStatusCodes;
    }

    /**
     * Alternate constructor with name
     * 
     * @param name name to be printed (e.g. in logs)
     * @param buffer data buffer
     * @param case4 case 4 flag (true if case 4)
     * @param successfulStatusCodes the list of status codes to be considered as successful although
     *        different from 9000
     */
    public ApduRequest(String name, byte[] buffer, boolean case4,
            Set<Integer> successfulStatusCodes) {
        this(buffer, case4, successfulStatusCodes);
        this.name = name;
    }

    /**
     * Alternate constructor without status codes list
     * 
     * @param buffer data buffer
     * @param case4 case 4 flag (true if case 4)
     */
    public ApduRequest(byte[] buffer, boolean case4) {
        this(buffer, case4, null);
    }

    /**
     * Alternate constructor with name, without status codes list
     *
     * @param name name to be printed (e.g. in logs)
     * @param buffer data buffer
     * @param case4 case 4 flag (true if case 4)
     */
    public ApduRequest(String name, byte[] buffer, boolean case4) {
        this(buffer, case4, null);
        this.name = name;
    }

    /**
     * Checks if is case 4.
     *
     * @return the case4 flag.
     */
    public boolean isCase4() {
        return case4;
    }


    /**
     * Name this APDU request
     * 
     * @param name Name of the APDU request
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the list of successful status codes for the request
     * 
     * @return the list of status codes
     */
    public Set<Integer> getSuccessfulStatusCodes() {
        return successfulStatusCodes;
    }

    /**
     * Get the name of this APDU request
     * 
     * @return Name of the APDU request
     */
    public String getName() {
        return name;
    }

    /**
     * Get the buffer of this APDU
     *
     * @return Name of the APDU request
     */
    public byte[] getBytes() {
        return this.bytes;
    }

    @Override
    public String toString() {
        StringBuilder string;
        string = new StringBuilder("ApduRequest: NAME = \"" + this.getName() + "\", RAWDATA = "
                + ByteArrayUtils.toHex(bytes));
        if (isCase4()) {
            string.append(", case4");
        }
        if (successfulStatusCodes != null) {
            string.append(", additional successful status codes = ");
            Iterator<Integer> iterator = successfulStatusCodes.iterator();
            while (iterator.hasNext()) {
                string.append(String.format("%04X", iterator.next()));
                if (iterator.hasNext()) {
                    string.append(", ");
                }
            }
        }
        return string.toString();
    }
}
