/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po.parser;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ApduResponse;

/**
 * Increase (0032) response parser. See specs: Calypso / page 85 / 9.4.4 Increase
 */
public class IncreaseRespPars extends AbstractApduResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6400, new StatusProperties(false, "Too many modifications in session."));
        m.put(0x6700, new StatusProperties(false, "Lc value not supported."));
        m.put(0x6981, new StatusProperties(false,
                "The current EF is not a Counters or Simulated Counter EF."));
        m.put(0x6982, new StatusProperties(false,
                "Security conditions not fulfilled (no session, wrong key, encryption required)."));
        m.put(0x6985, new StatusProperties(false,
                "Access forbidden (Never access mode, DF is invalidated, etc.)"));
        m.put(0x6986, new StatusProperties(false, "Command not allowed (no current EF)."));
        m.put(0x6A80, new StatusProperties(false, "Overflow error."));
        m.put(0x6A82, new StatusProperties(false, "File not found."));
        m.put(0x6B00, new StatusProperties(false, "P1 or P2 value not supported."));
        m.put(0x6103, new StatusProperties(true, "Successful execution."));
        m.put(0x9000, new StatusProperties(true, "Successful execution."));
        STATUS_TABLE = m;
    }


    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    /**
     * Instantiates a new IncreaseRespPars.
     *
     * @param response the response from the Increase APDU command
     */
    public IncreaseRespPars(ApduResponse response) {
        super(response);
    }

    /**
     * Returns the new counter value as an int between 0
     *
     * @return the new value
     * @throws java.lang.IllegalStateException - if the counter value is not available from the
     *         command response.
     */
    public int getNewValue() throws IllegalStateException {
        ByteBuffer newValueBuffer = getApduResponse().getDataOut();
        if (newValueBuffer.limit() == 3) {
            return (newValueBuffer.get(0) << 16) + (newValueBuffer.get(1) << 8)
                    + newValueBuffer.get(2);
        } else {
            throw new IllegalStateException(
                    "No counter value available in response to the Increase command.");
        }
    }

    @Override
    public String toString() {
        return String.format("New counter value: %d", getNewValue());
    }
}
