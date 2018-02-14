/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po.parser;

import org.keyple.calypso.commands.utils.ResponseUtils;
import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * Close Secure Session (008E) response parser. See specs: Calypso / page 104 / 9.5.2 Close Secure
 * Session
 */
public class CloseSessionRespPars extends ApduResponseParser {
    /**
     * Instantiates a new CloseSessionRespPars from the response.
     *
     * @param response from CloseSessionCmdBuild
     */
    public CloseSessionRespPars(ApduResponse response) {
        super(response);
        parse(response.getbytes());
        initStatusTable();
    }

    /** The signatureLo. */
    private byte[] signatureLo;

    /** The postponed data. */
    private byte[] postponedData;

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] {(byte) 0x67, (byte) 0x00}, new StatusProperties(false,
                "Lc signatureLo not supported (e.g. Lc=4 with a Revision 3.2 mode for Open Secure Session)."));
        statusTable.put(new byte[] {(byte) 0x6B, (byte) 0x00},
                new StatusProperties(false, "P1 or P2 signatureLo not supported."));
        statusTable.put(new byte[] {(byte) 0x69, (byte) 0x88},
                new StatusProperties(false, "incorrect signatureLo."));
        statusTable.put(new byte[] {(byte) 0x69, (byte) 0x85},
                new StatusProperties(false, "No session was opened."));
        statusTable.put(new byte[] {(byte) 0x90, (byte) 0x00},
                new StatusProperties(true, "Successful execution."));
    }

    private void parse(byte[] response) {
        // fclairamb(2018-02-14): Removed 2 bytes to the global response length;
        final int size = response.length - 2;

        if (size == 8) {
            signatureLo = ResponseUtils.subArray(response, 4, size);
            postponedData = ResponseUtils.subArray(response, 0, 4);
        } else if (size == 4) {
            signatureLo = ResponseUtils.subArray(response, 0, size);
        }
        // TODO: I can't add this, it breaks compatibility with existing tests
        /*
         * else if ( size != 0 ){ throw new RuntimeException("Size "+size+" is impossible"); }
         */
    }

    public byte[] getSignatureLo() {
        return signatureLo != null ? signatureLo : new byte[] {};
    }

    public byte[] getPostponedData() {
        return postponedData != null ? postponedData : new byte[] {};
    }

    public boolean hasPostponedData() {
        return postponedData != null;
    }
}
