/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.csm.parser;

import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * This class provides status code properties and the getters to access to the structured fields of
 * a Get Challenge response.
 *
 * @author Ixxi
 *
 */
public class CsmGetChallengeRespPars extends ApduResponseParser {

    /** The CSM(SAM) challenge. */
    private byte[] challenge;

    /**
     * Instantiates a new CsmGetChallengeRespPars .
     *
     * @param response of the CsmGetChallengeCmdBuild
     */
    public CsmGetChallengeRespPars(ApduResponse response) {
        super(response);
        initStatusTable();
        if (isSuccessful()) {
            challenge = response.getbytes();
        }
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] {(byte) 0x90, (byte) 0x00},
                new StatusProperties(true, "Successful execution."));
    }

    /**
     * Gets the challenge.
     *
     * @return the challenge
     */
    public byte[] getChallenge() {
        if (challenge != null) {
            return challenge.clone();
        } else {
            return new byte[0];
        }
    }

}
