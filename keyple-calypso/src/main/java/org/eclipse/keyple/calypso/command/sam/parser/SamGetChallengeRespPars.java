/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.sam.parser;



import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ApduResponse;

/**
 * SAM get challenge. See specs: Calypso / Page 108 / 9.5.4 - Get challenge
 */
public class SamGetChallengeRespPars extends AbstractApduResponseParser {
    /**
     * Instantiates a new SamGetChallengeRespPars .
     *
     * @param response of the SamGetChallengeCmdBuild
     */
    public SamGetChallengeRespPars(ApduResponse response) {
        super(response);
    }

    /**
     * Gets the challenge.
     *
     * @return the challenge
     */
    public byte[] getChallenge() {
        return isSuccessful() ? response.getDataOut() : null;
    }
}
