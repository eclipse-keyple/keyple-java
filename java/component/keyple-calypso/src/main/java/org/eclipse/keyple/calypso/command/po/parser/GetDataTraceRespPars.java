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
import org.eclipse.keyple.calypso.command.po.builder.GetDataTraceCmdBuild;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;

/**
 * Returns the traceability data obtained from the Get Data command response.
 * <p>
 * Provides getter methods for all relevant information.
 */
public final class GetDataTraceRespPars extends AbstractPoResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6A88, new StatusProperties(false,
                "Data object not found (optional mode not available)."));
        m.put(0x6B00, new StatusProperties(false,
                "P1 or P2 value not supported (<>004fh, 0062h, 006Fh, 00C0h, 00D0h, 0185h and 5F52h, according to "
                        + "available optional modes)."));
        m.put(0x6283, new StatusProperties(true,
                "Successful execution, FCI request and DF is invalidated."));
        STATUS_TABLE = m;
    }

    /**
     * Instantiates a new GetDataTraceRespPars from the ApduResponse to a selection application
     * command.
     *
     * @param response the Traceability Data response from Get Data APDU command
     * @param builderReference the reference to the builder that created this parser
     */
    public GetDataTraceRespPars(ApduResponse response, GetDataTraceCmdBuild builderReference) {
        super(response, builderReference);
    }

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    @Override
    public String toString() {
        return String.format("Traceability data: %s", ByteArrayUtil.toHex(response.getBytes()));
    }
}
