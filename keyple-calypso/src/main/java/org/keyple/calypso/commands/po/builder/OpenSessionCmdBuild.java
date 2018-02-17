/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po.builder;

import org.keyple.calypso.commands.CalypsoCommands;
import org.keyple.calypso.commands.po.PoCommandBuilder;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.utils.RequestUtils;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

/**
 * The Class OpenSessionCmdBuild. This class provides the dedicated constructor to build the Open
 * Secure Session APDU command.
 *
 * @author Ixxi
 *
 */
public class OpenSessionCmdBuild extends PoCommandBuilder {

    private static CalypsoCommands command = CalypsoCommands.PO_OPEN_SESSION;

    /**
     * Instantiates a new OpenSessionCmdBuild.
     *
     * @param revision the revision of the PO
     * @param keyIndex the key index
     * @param samChallenge the sam challenge returned by the CSM Get Challenge APDU command
     * @param recordNumberToRead the record number to read
     * @param sfiToSelect the sfi to select
     * @throws InconsistentCommandException thrown if rev 2.4 and key index is 0
     */

    public OpenSessionCmdBuild(PoRevision revision, byte keyIndex, byte[] samChallenge,
            byte sfiToSelect, byte recordNumberToRead) throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        byte p1;
        byte p2;
        byte[] dataIn;

        switch (this.defaultRevision) {
            //
            case REV2_4:
                if (keyIndex == 0x00) {
                    throw new InconsistentCommandException();
                }
                p1 = (byte) (0x80 + (recordNumberToRead * 8) + keyIndex);
                p2 = (byte) (sfiToSelect * 8);
                dataIn = new byte[samChallenge.length];
                System.arraycopy(samChallenge, 0, dataIn, 0, samChallenge.length);
                break;
            //
            case REV3_2:
                p1 = (byte) ((recordNumberToRead * 8) + keyIndex);
                p2 = (byte) ((sfiToSelect * 8) + 2);
                dataIn = new byte[samChallenge.length + 1];
                dataIn[0] = (byte) 0x00;
                System.arraycopy(samChallenge, 0, dataIn, 1, samChallenge.length);
                break;
            //
            case REV3_1:
            default:
                p1 = (byte) ((recordNumberToRead * 8) + keyIndex);
                p2 = (byte) ((sfiToSelect * 8) + 1);
                dataIn = new byte[samChallenge.length];
                System.arraycopy(samChallenge, 0, dataIn, 0, samChallenge.length);
                break;
        }

        /*
         * CalypsoRequest request = new CalypsoRequest(
         * PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00, command, p1,
         * p2, dataIn);
         */
        this.request = RequestUtils.constructAPDURequest(
                PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00, command,
                p1, p2, dataIn);

    }

    /**
     * Low level constructo
     *
     * @param request Request for building command
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public OpenSessionCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }
}
