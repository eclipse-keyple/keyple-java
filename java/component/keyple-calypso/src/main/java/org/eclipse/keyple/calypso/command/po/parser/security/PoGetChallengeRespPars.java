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
package org.eclipse.keyple.calypso.command.po.parser.security;


import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.builder.security.PoGetChallengeCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.*;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * PO Get challenge response parser. See specs: Calypso / page 108 / 9.54 - Get challenge
 */
public final class PoGetChallengeRespPars extends AbstractPoResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6C08, new StatusProperties(false,
                "Le value incorrect (00h in ISO 7816 T=0, or above the data size)", KeyplePoBadExpectedLengthException.class));
        STATUS_TABLE = m;
    }

    /**
     * Instantiates a new PoGetChallengeRespPars.
     *
     * @param response the response from PO Get Challenge APDU Command
     * @param builderReference the reference to the builder that created this parser
     */
    public PoGetChallengeRespPars(ApduResponse response, PoGetChallengeCmdBuild builderReference) {
        super(response, builderReference);
    }

    public byte[] getPoChallenge() {
        return getApduResponse().getDataOut();
    }

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }
}
