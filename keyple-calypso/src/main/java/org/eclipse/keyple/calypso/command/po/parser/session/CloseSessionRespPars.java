/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po.parser.session;


import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.util.ByteBufferUtils;

/**
 * Close Secure Session (008E) response parser. See specs: Calypso / page 104 / 9.5.2 - Close Secure
 * Session
 */
public class CloseSessionRespPars extends AbstractApduResponseParser {

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
    private ByteBuffer signatureLo;

    /** The postponed data. */
    private ByteBuffer postponedData;

    /**
     * Instantiates a new CloseSessionRespPars from the response.
     *
     * @param response from CloseSessionCmdBuild
     */
    public CloseSessionRespPars(ApduResponse response) {
        super(response);
        parse(response.getBytes());
    }

    private void parse(ByteBuffer response) {
        final int size = response.limit() - 2;

        if (size == 8) {
            signatureLo = ByteBufferUtils.subIndex(response, 4, 8);
            postponedData = ByteBufferUtils.subIndex(response, 0, 4);
        } else if (size == 4) {
            signatureLo = ByteBufferUtils.subIndex(response, 0, size);
        }
    }

    public ByteBuffer getSignatureLo() {
        return signatureLo;
    }

    public ByteBuffer getPostponedData() {
        return postponedData;
    }
}
