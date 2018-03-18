/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.csm.parser;


import java.nio.ByteBuffer;
import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * CSM get challenge. See specs: Calypso / Page 108 / 9.5.4 - Get challenge
 */
public class CsmGetChallengeRespPars extends ApduResponseParser {
    /**
     * Instantiates a new CsmGetChallengeRespPars .
     *
     * @param response of the CsmGetChallengeCmdBuild
     */
    public CsmGetChallengeRespPars(ApduResponse response) {
        super(response);
    }

    /**
     * Gets the challenge.
     *
     * @return the challenge
     */
    public ByteBuffer getChallenge() {
        return isSuccessful() ? response.getDataOut() : null;
    }
}
