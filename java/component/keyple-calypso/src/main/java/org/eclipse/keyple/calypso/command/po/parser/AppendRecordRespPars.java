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
import org.eclipse.keyple.calypso.command.po.builder.AppendRecordCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.*;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * Update records response parser. See specs: Calypso / page 96 / 9.4.1.1 - Update record
 */
public final class AppendRecordRespPars extends AbstractPoResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6B00, new StatusProperties(false, "P1 or P2 value not supported.",
                CalypsoPoIllegalParameterException.class));
        m.put(0x6700, new StatusProperties(false, "Lc value not supported.",
                CalypsoPoDataAccessException.class));
        m.put(0x6400, new StatusProperties(false, "Too many modifications in session.",
                CalypsoPoSessionBufferOverflowException.class));
        m.put(0x6981, new StatusProperties(false, "The current EF is not a Cyclic EF.",
                CalypsoPoDataAccessException.class));
        m.put(0x6982,
                new StatusProperties(false,
                        "Security conditions not fulfilled (no session, wrong key).",
                        CalypsoPoSecurityContextException.class));
        m.put(0x6985,
                new StatusProperties(false,
                        "Access forbidden (Never access mode, DF is invalidated, etc..).",
                        CalypsoPoAccessForbiddenException.class));
        m.put(0x6986, new StatusProperties(false, "Command not allowed (no current EF).",
                CalypsoPoDataAccessException.class));
        m.put(0x6A82,
                new StatusProperties(false, "File not found.", CalypsoPoDataAccessException.class));
        STATUS_TABLE = m;
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

    /**
     * Instantiates a new AppendRecordRespPars.
     * 
     * @param response the response from the PO
     * @param builderReference the reference to the builder that created this parser
     */
    public AppendRecordRespPars(ApduResponse response, AppendRecordCmdBuild builderReference) {
        super(response, builderReference);
    }
}
