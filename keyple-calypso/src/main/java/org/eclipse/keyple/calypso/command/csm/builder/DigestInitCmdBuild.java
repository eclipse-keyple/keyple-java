/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.calypso.command.csm.builder;


import org.eclipse.keyple.calypso.command.csm.CalypsoSmCommands;
import org.eclipse.keyple.calypso.command.csm.CsmCommandBuilder;
import org.eclipse.keyple.calypso.command.csm.CsmRevision;

/**
 * Builder for the CSM Digest Init APDU command.
 */
public class DigestInitCmdBuild extends CsmCommandBuilder {

    /**
     * The command.
     */
    private static final CalypsoSmCommands command = CalypsoSmCommands.DIGEST_INIT;

    /**
     * Instantiates a new DigestInitCmdBuild.
     *
     * @param revision of the CSM(SAM)
     * @param verificationMode the verification mode
     * @param rev3_2Mode the rev 3 2 mode
     * @param workKeyRecordNumber the work key record number
     * @param workKeyKif from the AbstractOpenSessionCmdBuild response
     * @param workKeyKVC from the AbstractOpenSessionCmdBuild response
     * @param digestData all data out from the AbstractOpenSessionCmdBuild response
     * @throws java.lang.IllegalArgumentException - if the work key record number
     * @throws java.lang.IllegalArgumentException - if the digest data is null
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public DigestInitCmdBuild(CsmRevision revision, boolean verificationMode, boolean rev3_2Mode,
            byte workKeyRecordNumber, byte workKeyKif, byte workKeyKVC, byte[] digestData)
            throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }

        if (workKeyRecordNumber == 0x00 && (workKeyKif == 0x00 || workKeyKVC == 0x00)) {
            throw new IllegalArgumentException("Bad key record number, kif or kvc!");
        }
        if (digestData == null) {
            throw new IllegalArgumentException("Digest data is null!");
        }
        byte cla = CsmRevision.S1D.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x80;
        byte p1 = 0x00;
        if (verificationMode) {
            p1 = (byte) (p1 + 1);
        }
        if (rev3_2Mode) {
            p1 = (byte) (p1 + 2);
        }

        byte p2 = (byte) 0xFF;
        if (workKeyKif == (byte) 0xFF) {
            p2 = workKeyRecordNumber;
        }

        byte[] dataIn;

        if (p2 == (byte) 0xFF) {
            dataIn = new byte[2 + digestData.length];
            dataIn[0] = workKeyKif;
            dataIn[1] = workKeyKVC;
            System.arraycopy(digestData, 0, dataIn, 2, digestData.length);
        } else {
            dataIn = null;
        }
        // CalypsoRequest calypsoRequest = new CalypsoRequest(cla, CalypsoCommands.CSM_DIGEST_INIT,
        // p1, p2, dataIn);
        request = setApduRequest(cla, CalypsoSmCommands.DIGEST_INIT, p1, p2, dataIn, null);

    }
}
