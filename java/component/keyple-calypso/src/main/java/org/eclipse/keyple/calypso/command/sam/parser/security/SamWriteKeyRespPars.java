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
package org.eclipse.keyple.calypso.command.sam.parser.security;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.calypso.command.sam.AbstractSamResponseParser;
import org.eclipse.keyple.calypso.command.sam.exception.*;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * SAM Write Key response parser.
 */
public class SamWriteKeyRespPars extends AbstractSamResponseParser {

    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractSamResponseParser.STATUS_TABLE);
        m.put(0x6700,
                new StatusProperties("Incorrect Lc.", CalypsoSamIllegalParameterException.class));
        m.put(0x6900, new StatusProperties("An event counter cannot be incremented.",
                CalypsoSamCounterOverflowException.class));
        m.put(0x6985, new StatusProperties("Preconditions not satisfied.",
                CalypsoSamAccessForbiddenException.class));
        m.put(0x6988, new StatusProperties("Incorrect signature.",
                CalypsoSamSecurityDataException.class));
        m.put(0x6A00, new StatusProperties("P1 or P2 incorrect.",
                CalypsoSamIllegalParameterException.class));
        m.put(0x6A80, new StatusProperties("Incorrect plain or decrypted data.",
                CalypsoSamIncorrectInputDataException.class));
        m.put(0x6A83, new StatusProperties("Record not found: deciphering key not found.",
                CalypsoSamDataAccessException.class));
        m.put(0x6A87, new StatusProperties("Lc inconsistent with P1 or P2.",
                CalypsoSamIncorrectInputDataException.class));
        STATUS_TABLE = m;
    }

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    /**
     * Instantiates a new {@link UnlockRespPars}.
     *
     * @param response the response
     */
    public SamWriteKeyRespPars(ApduResponse response) {
        super(response);
    }
}
