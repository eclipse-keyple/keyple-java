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
package org.eclipse.keyple.calypso.command.po.parser;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.builder.IncreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.*;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;

/**
 * Increase (0032) response parser. See specs: Calypso / page 85 / 9.4.4 Increase
 */
public final class IncreaseRespPars extends AbstractPoResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6400, new StatusProperties("Too many modifications in session.",
                CalypsoPoSessionBufferOverflowException.class));
        m.put(0x6700, new StatusProperties("Lc value not supported.",
                CalypsoPoIllegalParameterException.class));
        m.put(0x6981,
                new StatusProperties("The current EF is not a Counters or Simulated Counter EF.",
                        CalypsoPoDataAccessException.class));
        m.put(0x6982, new StatusProperties(
                "Security conditions not fulfilled (no session, wrong key, encryption required).",
                CalypsoPoSecurityContextException.class));
        m.put(0x6985,
                new StatusProperties(
                        "Access forbidden (Never access mode, DF is invalidated, etc.)",
                        CalypsoPoAccessForbiddenException.class));
        m.put(0x6986, new StatusProperties("Command not allowed (no current EF).",
                CalypsoPoDataAccessException.class));
        m.put(0x6A80,
                new StatusProperties("Overflow error.", CalypsoPoDataOutOfBoundsException.class));
        m.put(0x6A82, new StatusProperties("File not found.", CalypsoPoDataAccessException.class));
        m.put(0x6B00, new StatusProperties("P1 or P2 value not supported.",
                CalypsoPoDataAccessException.class));
        m.put(0x6103,
                new StatusProperties("Successful execution (possible only in ISO7816 T=0).", null));
        STATUS_TABLE = m;
    }

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    /**
     * Constructor to build a parser of the Increase command response.
     *
     * @param response response to parse
     * @param builder the reference to the builder that created this parser
     */
    public IncreaseRespPars(ApduResponse response, IncreaseCmdBuild builder) {
        super(response, builder);
    }

    /**
     * Returns the new counter value as an int between 0
     *
     * @return the new value
     */
    public int getNewValue() {
        byte[] newValueBuffer = getApduResponse().getDataOut();
        if (newValueBuffer.length == 3) {
            return ByteArrayUtil.threeBytesToInt(newValueBuffer, 0);
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
