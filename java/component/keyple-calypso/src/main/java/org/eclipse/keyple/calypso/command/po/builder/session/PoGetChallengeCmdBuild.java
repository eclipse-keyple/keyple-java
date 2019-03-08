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
package org.eclipse.keyple.calypso.command.po.builder.session;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.command.po.PoCommandBuilder;

/**
 * The Class PoGetChallengeCmdBuild. This class provides the dedicated constructor to build the PO
 * Get Challenge.
 */
public final class PoGetChallengeCmdBuild extends PoCommandBuilder {

    private static final CalypsoPoCommands command = CalypsoPoCommands.GET_CHALLENGE;

    /**
     * Instantiates a new PoGetChallengeCmdBuild.
     *
     * @param poClass indicates which CLA byte should be used for the Apdu
     */
    public PoGetChallengeCmdBuild(PoClass poClass) {
        super(command, null);

        byte p1 = (byte) 0x01;
        byte p2 = (byte) 0x10;
        byte le = (byte) 0x08;

        this.request = setApduRequest(poClass.getValue(), command, p1, p2, null, le);
    }
}
