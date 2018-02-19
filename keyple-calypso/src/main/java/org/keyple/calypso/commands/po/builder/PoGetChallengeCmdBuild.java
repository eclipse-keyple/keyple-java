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
import org.keyple.calypso.commands.po.SendableInSession;
import org.keyple.calypso.commands.utils.RequestUtils;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

/**
 * The Class PoGetChallengeCmdBuild. This class provides the dedicated constructor to build the PO
 * Get Challenge.
 *
 * @author Ixxi
 *
 */
public class PoGetChallengeCmdBuild extends PoCommandBuilder implements SendableInSession {

    private static CalypsoCommands command = CalypsoCommands.PO_GET_CHALLENGE;

    /**
     * Instantiates a new PoGetChallengeCmdBuild.
     *
     * @param revision the revision of the PO
     */
    public PoGetChallengeCmdBuild(PoRevision revision) {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }

        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;

        byte p1 = (byte) 0x01;
        byte p2 = (byte) 0x10;
        byte[] dataIn = null;
        byte optionalLe = (byte) 0x08;
        // CalypsoRequest request = new CalypsoRequest(cla, command, p1, p2, dataIn, optionnalLe);
        ApduRequest apduRequest =
                RequestUtils.constructAPDURequest(cla, command, p1, p2, dataIn, optionalLe);

        this.request = apduRequest;

    }


    public PoGetChallengeCmdBuild(ApduRequest request) throws InconsistentCommandException {
        super(command, request);
        RequestUtils.controlRequestConsistency(command, request);
    }

}
