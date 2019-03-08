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
package org.eclipse.keyple.calypso.command.po.parser.session;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.message.ApduResponse;

/**
 * Close Secure Session (008E) response parser. See specs: Calypso / page 104 / 9.5.2 - Close Secure
 * Session
 */
public final class CloseSessionRespPars extends AbstractApduResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6700, new StatusProperties(false,
                "Lc signatureLo not supported (e.g. Lc=4 with a Revision 3.2 mode for Open Secure Session)."));
        m.put(0x6B00, new StatusProperties(false, "P1 or P2 signatureLo not supported."));
        m.put(0x6988, new StatusProperties(false, "incorrect signatureLo."));
        m.put(0x6985, new StatusProperties(false, "No session was opened."));

        STATUS_TABLE = m;
    }

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    /** The signatureLo. */
    private byte[] signatureLo;

    /** The postponed data. */
    private byte[] postponedData;

    /**
     * Instantiates a new CloseSessionRespPars from the response.
     *
     * @param response from CloseSessionCmdBuild
     */
    public CloseSessionRespPars(ApduResponse response) {
        super(response);
        parse(response.getDataOut());
    }

    private void parse(byte[] response) {
        if (response.length == 8) {
            signatureLo = Arrays.copyOfRange(response, 4, 8);
            postponedData = Arrays.copyOfRange(response, 0, 4);
        } else if (response.length == 4) {
            signatureLo = Arrays.copyOfRange(response, 0, 4);
        } else {
            if (response.length != 0) {
                throw new IllegalArgumentException(
                        "Unexpected length in response to CloseSecureSession command: "
                                + response.length);
            }
        }
    }

    public byte[] getSignatureLo() {
        return signatureLo;
    }

    public byte[] getPostponedData() {
        return postponedData;
    }
}
