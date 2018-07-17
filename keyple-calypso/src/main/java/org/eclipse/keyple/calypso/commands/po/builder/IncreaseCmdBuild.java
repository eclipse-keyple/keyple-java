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
import org.eclipse.keyple.seproxy.ApduRequest;

/**
 * The Class IncreaseCmdBuild. This class provides the dedicated constructor to build the Increase
 * APDU command.
 *
 */
public class IncreaseCmdBuild extends AbstractPoCommandBuilder implements PoSendableInSession {

    /** The command. */
    private static CommandsTable command = CalypsoPoCommands.INCREASE;

    /**
     * Instantiates a new increase cmd build from command parameters.
     *
     * @param revision the revision of the PO
     * @param counterNumber >= 01h: Counters file, number of the counter. 00h: Simulated Counter
     *        file.
     * @param sfi SFI of the file to select or 00h for current EF
     * @param incValue Value to add to the counter (defined as a positive int <= 16777215 [FFFFFFh])
     * @throws java.lang.IllegalArgumentException - if the decrement value is out of range
     * @throws java.lang.IllegalArgumentException - if the command is inconsistent
     */
    public IncreaseCmdBuild(PoRevision revision, byte sfi, byte counterNumber, int incValue)
            throws IllegalArgumentException {
        super(command, null);

        if (revision != null) {
            this.defaultRevision = revision;
        }

        // check if the incValue is in the allowed interval
        if (incValue < 0 || incValue > 0xFFFFFF) {
            throw new IllegalArgumentException("Increment value out of range!");
        }

        // convert the integer value into a 3-byte buffer
        ByteBuffer incValueBuffer = ByteBuffer.allocate(3);
        incValueBuffer.put(0, (byte) ((incValue >> 16) & 0xFF));
        incValueBuffer.put(1, (byte) ((incValue >> 8) & 0xFF));
        incValueBuffer.put(2, (byte) (incValue & 0xFF));

        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;
        byte p1 = counterNumber;
        byte p2 = (byte) (sfi * 8);

        this.request =
                RequestUtils.constructAPDURequest(cla, command, p1, p2, incValueBuffer, (byte) 3);
    }

    /**
     * Instantiates a new increase cmd build from an ApduRequest.
     *
     * @param request the request
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public IncreaseCmdBuild(ApduRequest request) throws IllegalArgumentException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }
}
