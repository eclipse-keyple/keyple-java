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



import org.eclipse.keyple.calypso.command.sam.AbstractSamResponseParser;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * SAM get challenge. See specs: Calypso / Page 108 / 9.5.4 - Get challenge
 */
public class SamGetChallengeRespPars extends AbstractSamResponseParser {
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
