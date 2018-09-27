/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po.builder.session;

import org.eclipse.keyple.calypso.command.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.command.po.PoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;

/**
 * The Class PoGetChallengeCmdBuild. This class provides the dedicated constructor to build the PO
 * Get Challenge.
 */
public class RatificationCmdBuild extends PoCommandBuilder implements PoSendableInSession {

    private static final CalypsoPoCommands command = CalypsoPoCommands.RATIFICATION;

    /**
     * Instantiates a new RatificationCmdBuild.
     *
     * @param revision the revision of the PO
     */
    public RatificationCmdBuild(PoRevision revision) {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }

        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;

        byte p1 = (byte) 0x00;
        byte p2 = (byte) 0x00;
        byte le = (byte) 0x00;

        this.request = setApduRequest(cla, command, p1, p2, null, le);
    }
}
