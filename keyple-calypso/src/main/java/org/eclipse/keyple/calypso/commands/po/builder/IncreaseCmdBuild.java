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
     * @param incValue Value to add to the counter (3 bytes)
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public IncreaseCmdBuild(PoRevision revision, byte counterNumber, byte sfi, ByteBuffer incValue)
            throws InconsistentCommandException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }

        if (incValue.limit() != 3) {
            throw new InconsistentCommandException();
        }

        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;
        byte p1 = counterNumber;
        byte p2 = (byte) (sfi * 8);

        this.request = RequestUtils.constructAPDURequest(cla, command, p1, p2, incValue, (byte) 3);
    }

    /**
     * Instantiates a new increase cmd build from an ApduRequest.
     *
     * @param request the request
     * @throws InconsistentCommandException the inconsistent command exception
     */
    public IncreaseCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }
}
