/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po.parser;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ApduResponse;

/**
 * Update records response parser. See specs: Calypso / page 96 / 9.4.1.1 - Update record
 */
public class AppendRecordRespPars extends AbstractApduResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6B00, new StatusProperties(false, "P1 or P2 vaue not supported."));
        m.put(0x6700, new StatusProperties(false, "Lc value not supported."));
        m.put(0x6400, new StatusProperties(false, "Too many modifications in session."));
        m.put(0x6981, new StatusProperties(false, "The current EF is not a Cyclic EF."));
        m.put(0x6982, new StatusProperties(false,
                "Security conditions not fulfilled (no session, wrong key)."));
        m.put(0x6985, new StatusProperties(false,
                "Access forbidden (Never access mode, DF is invalidated, etc..)."));
        m.put(0x6986, new StatusProperties(false, "Command not allowed (no current EF)."));
        m.put(0x6A82, new StatusProperties(false, "File not found."));
        STATUS_TABLE = m;
    }

    /**
     * Instantiates a new AppendRecordRespPars.
     *
     * @param response the response from the Update Records APDU command
     */
    public AppendRecordRespPars(ApduResponse response) {
        super(response);
    }

    /**
     * Get the internal status table
     *
     * @return Status table
     */
    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }
}
