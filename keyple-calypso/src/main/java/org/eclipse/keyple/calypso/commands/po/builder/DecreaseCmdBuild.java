/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.commands.po.builder;

import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.commands.PoSendableInSession;
import org.eclipse.keyple.calypso.commands.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.commands.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.commands.po.PoRevision;
import org.eclipse.keyple.calypso.commands.utils.RequestUtils;
import org.eclipse.keyple.commands.CommandsTable;
import org.eclipse.keyple.commands.InconsistentCommandException;
import org.eclipse.keyple.seproxy.ApduRequest;

/**
 * The Class DecreaseCmdBuild. This class provides the dedicated constructor to build the Decrease
 * APDU command.
 *
 */
public class DecreaseCmdBuild extends AbstractPoCommandBuilder implements PoSendableInSession {

    /** The command. */
    private static CommandsTable command = CalypsoPoCommands.DECREASE;

    /**
     * Instantiates a new decrease cmd build from command parameters.
     *
     * @param revision the revision of the PO
     * @param sfi SFI of the file to select or 00h for current EF
     * @param counterNumber >= 01h: Counters file, number of the counter. 00h: Simulated Counter
     *        file.
     * @param decValue Value to subtract to the counter (defined as a positive int <= 16777215
     *        [FFFFFFh])
     * @throws InconsistentCommandException the inconsistent command exception
     */

    public DecreaseCmdBuild(PoRevision revision, byte sfi, byte counterNumber, int decValue)
            throws InconsistentCommandException {
        super(command, null);

        if (revision != null) {
            this.defaultRevision = revision;
        }

        // check if the incValue is in the allowed interval
        if (decValue < 0 || decValue > 0xFFFFFF) {
            throw new InconsistentCommandException();
        }

        // convert the integer value into a 3-byte buffer
        ByteBuffer decValueBuffer = ByteBuffer.allocate(3);
        decValueBuffer.put(0, (byte) ((decValue >> 16) & 0xFF));
        decValueBuffer.put(1, (byte) ((decValue >> 8) & 0xFF));
        decValueBuffer.put(2, (byte) (decValue & 0xFF));

        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;
        byte p1 = counterNumber;
        byte p2 = (byte) (sfi * 8);

        this.request =
                RequestUtils.constructAPDURequest(cla, command, p1, p2, decValueBuffer, (byte) 3);
    }

    /**
     * Instantiates a new decrease cmd build from an ApduRequest.
     *
     * @param request the request
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public DecreaseCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }
}
