/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.csm.builder;

import java.nio.ByteBuffer;
import org.keyple.calypso.commands.csm.AbstractCsmCommandBuilder;
import org.keyple.calypso.commands.csm.CalypsoSmCommands;
import org.keyple.calypso.commands.csm.CsmRevision;
import org.keyple.calypso.commands.utils.RequestUtils;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

/**
 * Builder for the CSM Digest Init APDU command.
 */
public class DigestInitCmdBuild extends AbstractCsmCommandBuilder {

    /**
     * The command.
     */
    private static CalypsoSmCommands command = CalypsoSmCommands.DIGEST_INIT;

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
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public DigestInitCmdBuild(CsmRevision revision, boolean verificationMode, boolean rev3_2Mode,
            byte workKeyRecordNumber, byte workKeyKif, byte workKeyKVC, ByteBuffer digestData)
            throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }

        if (workKeyRecordNumber == 0x00 && (workKeyKif == 0x00 || workKeyKVC == 0x00)) {
            throw new InconsistentCommandException();
        }
        if (digestData == null) {
            throw new InconsistentCommandException();
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

        ByteBuffer dataIn;

        if (p2 == (byte) 0xFF) {
            dataIn = ByteBuffer.allocate(2 + digestData.limit());
            dataIn.put(workKeyKif);
            dataIn.put(workKeyKVC);
            dataIn.put(digestData);
        } else {
            dataIn = null;
        }
        // CalypsoRequest calypsoRequest = new CalypsoRequest(cla, CalypsoCommands.CSM_DIGEST_INIT,
        // p1, p2, dataIn);
        request = RequestUtils.constructAPDURequest(cla, CalypsoSmCommands.DIGEST_INIT, p1, p2,
                dataIn);

    }

    /**
     * Instantiates a new digest init cmd build.
     *
     * @param request the request
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public DigestInitCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }
}
