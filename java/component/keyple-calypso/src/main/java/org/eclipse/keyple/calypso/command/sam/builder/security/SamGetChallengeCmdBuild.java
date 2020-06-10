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
package org.eclipse.keyple.calypso.command.sam.builder.security;

import org.eclipse.keyple.calypso.command.sam.AbstractSamCommandBuilder;
import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommand;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.parser.security.SamGetChallengeRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * Builder for the SAM Get Challenge APDU command.
 */
public class SamGetChallengeCmdBuild extends AbstractSamCommandBuilder<SamGetChallengeRespPars> {

    /** The command reference. */
    private static final CalypsoSamCommand command = CalypsoSamCommand.GET_CHALLENGE;

    /**
     * Instantiates a new SamGetChallengeCmdBuild.
     *
     * @param revision of the SAM (SAM)
     * @param expectedResponseLength the expected response length
     * @throws IllegalArgumentException - if the expected response length has wrong value.
     */
    public SamGetChallengeCmdBuild(SamRevision revision, byte expectedResponseLength) {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        if (expectedResponseLength != 0x04 && expectedResponseLength != 0x08) {
            throw new IllegalArgumentException(String.format(
                    "Bad challenge length! Expected 4 or 8, got %s", expectedResponseLength));
        }
        byte cla = this.defaultRevision.getClassByte();
        byte p1 = 0x00;
        byte p2 = 0x00;

        request = setApduRequest(cla, command, p1, p2, null, expectedResponseLength);
    }

    @Override
    public SamGetChallengeRespPars createResponseParser(ApduResponse apduResponse) {
        return new SamGetChallengeRespPars(apduResponse, this);
    }
}
