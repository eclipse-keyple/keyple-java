/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po.parser;

import java.nio.ByteBuffer;
import org.keyple.commands.AbstractApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * PO Get challenge response parser. See specs: Calypso / page 108 / 9.54 - Get challenge
 */
public class PoGetChallengeRespPars extends AbstractApduResponseParser {

    /**
     * Instantiates a new PoGetChallengeRespPars.
     *
     * @param response the response from PO Get Challenge APDU Command
     */
    public PoGetChallengeRespPars(ApduResponse response) {
        super(response);
    }

    public ByteBuffer getPoChallenge() {
        return getApduResponse().getDataOut();
    }
}
