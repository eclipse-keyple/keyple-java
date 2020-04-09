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
package org.eclipse.keyple.calypso.command.sam.parser.security;


import org.eclipse.keyple.calypso.command.po.exception.*;
import org.eclipse.keyple.calypso.command.sam.AbstractSamResponseParser;
import org.eclipse.keyple.calypso.command.sam.exception.KeypleSamAccessForbiddenException;
import org.eclipse.keyple.calypso.command.sam.exception.KeypleSamIllegalParameterException;
import org.eclipse.keyple.calypso.command.sam.exception.KeypleSamSecurityDataException;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Digest authentication response parser. See specs: Calypso / page 54 / 7.4.2 - Session MAC
 * computation
 */
public class DigestAuthenticateRespPars extends AbstractSamResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6700, new StatusProperties(false, "Incorrect Lc.", KeypleSamIllegalParameterException.class));
        m.put(0x6985, new StatusProperties(false, "Preconditions not satisfied.", KeypleSamAccessForbiddenException.class));
        m.put(0x6988, new StatusProperties(false, "Incorrect signature.", KeypleSamSecurityDataException.class));
        m.put(0x6D00, new StatusProperties(false,
                "Instruction unknown.", KeypleSamIllegalParameterException.class));
        m.put(0x6E00, new StatusProperties(false,
                "Class not supported.", KeypleSamIllegalParameterException.class));
        STATUS_TABLE = m;
    }

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    /**
     * Instantiates a new DigestAuthenticateRespPars.
     *
     * @param response from the SAM DigestAuthenticateCmdBuild
     */
    public DigestAuthenticateRespPars(ApduResponse response) {
        super(response);
    }
}
