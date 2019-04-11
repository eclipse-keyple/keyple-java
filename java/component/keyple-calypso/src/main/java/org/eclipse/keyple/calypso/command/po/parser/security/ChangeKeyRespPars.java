/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.po.parser.security;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.message.ApduResponse;

public class ChangeKeyRespPars extends AbstractPoResponseParser {
    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6700, new StatusProperties(false,
                "Lc value not supported (not 04h, 10h, 18h, 20h or 18h not " + "supported)."));
        m.put(0x6900, new StatusProperties(false, "Transaction Counter is 0."));
        m.put(0x6982, new StatusProperties(false,
                "Security conditions not fulfilled (Get Challenge not done: challenge unavailable)."));
        m.put(0x6985, new StatusProperties(false,
                "Access forbidden (a session is open or DF is invalidated)."));
        m.put(0x6988, new StatusProperties(false, "Incorrect Cryptogram."));
        m.put(0x6A80, new StatusProperties(false,
                "Decrypted message incorrect (key algorithm not supported, incorrect padding, etc.)."));
        m.put(0x6A87, new StatusProperties(false, "Lc not compatible with P2."));
        m.put(0x6B00, new StatusProperties(false, "Incorrect P1, P2."));
        m.put(0x9000, new StatusProperties(false, "Successful execution."));
        STATUS_TABLE = m;
    }

    /**
     * Instantiates a new ChangeKeyRespPars
     */
    public ChangeKeyRespPars(ApduResponse response) {
        super(response);
    }

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }
}
