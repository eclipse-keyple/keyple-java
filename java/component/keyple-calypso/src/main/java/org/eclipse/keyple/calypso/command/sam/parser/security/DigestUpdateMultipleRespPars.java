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


import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.sam.AbstractSamResponseParser;
import org.eclipse.keyple.calypso.command.sam.builder.security.DigestUpdateMultipleCmdBuild;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamAccessForbiddenException;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamIllegalParameterException;
import org.eclipse.keyple.calypso.command.sam.exception.CalypsoSamIncorrectInputDataException;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * Digest update multiple response parser. See specs: Calypso / page 54 / 7.4.2 - Session MAC
 * computation
 */
public class DigestUpdateMultipleRespPars extends AbstractSamResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractSamResponseParser.STATUS_TABLE);
        m.put(0x6700,
                new StatusProperties("Incorrect Lc.", CalypsoSamIllegalParameterException.class));
        m.put(0x6985, new StatusProperties("Preconditions not satisfied.",
                CalypsoSamAccessForbiddenException.class));
        m.put(0x6A80,
                new StatusProperties("Incorrect value in the incoming data: incorrect structure.",
                        CalypsoSamIncorrectInputDataException.class));
        m.put(0x6B00,
                new StatusProperties("Incorrect P1.", CalypsoSamIllegalParameterException.class));
        STATUS_TABLE = m;
    }

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    /**
     * Instantiates a new DigestUpdateMultipleRespPars.
     *
     * @param response the response
     * @param builder the reference to the builder that created this parser
     */
    public DigestUpdateMultipleRespPars(ApduResponse response,
            DigestUpdateMultipleCmdBuild builder) {
        super(response, builder);
    }
}
