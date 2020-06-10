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
import org.eclipse.keyple.calypso.command.sam.parser.security.DigestUpdateMultipleRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * This class provides the dedicated constructor to build the SAM Digest Update Multiple APDU
 * command.
 */
public class DigestUpdateMultipleCmdBuild
        extends AbstractSamCommandBuilder<DigestUpdateMultipleRespPars> {

    /** The command. */
    private static final CalypsoSamCommand command = CalypsoSamCommand.DIGEST_UPDATE_MULTIPLE;

    /**
     * Instantiates a new DigestUpdateMultipleCmdBuild.
     *
     * @param revision the revision
     * @param encryptedSession the encrypted session flag, true if encrypted
     * @param digestData the digest data
     */
    public DigestUpdateMultipleCmdBuild(SamRevision revision, boolean encryptedSession,
            byte[] digestData) {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        byte cla = this.defaultRevision.getClassByte();
        byte p1 = (byte) 0x00;
        byte p2 = encryptedSession ? (byte) 0x80 : (byte) 0x00;

        if (digestData == null || digestData.length > 255) {
            throw new IllegalArgumentException("Digest data null or too long!");
        }

        request = setApduRequest(cla, command, p1, p2, digestData, null);
    }

    @Override
    public DigestUpdateMultipleRespPars createResponseParser(ApduResponse apduResponse) {
        return new DigestUpdateMultipleRespPars(apduResponse, this);
    }
}
