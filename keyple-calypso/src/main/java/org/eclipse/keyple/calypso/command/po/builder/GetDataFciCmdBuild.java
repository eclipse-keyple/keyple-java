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

package org.eclipse.keyple.calypso.command.po.builder;

import org.eclipse.keyple.calypso.command.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.command.po.PoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoRevision;

/**
 * This class provides the dedicated constructor to build the Get data APDU commands.
 *
 * This command can not be sent in session because it would generate a 6Cxx status in contact mode
 * and thus make calculation of the digest impossible.
 *
 */
public class GetDataFciCmdBuild extends PoCommandBuilder {

    private static final CalypsoPoCommands command = CalypsoPoCommands.GET_DATA_FCI;

    /**
     * Instantiates a new GetDataFciCmdBuild.
     *
     * @param revision the PO revision
     */
    public GetDataFciCmdBuild(PoRevision revision) {
        super(command, null);
        byte cla = PoRevision.REV2_4.equals(revision) ? (byte) 0x94 : (byte) 0x00;

        request = setApduRequest(cla, command, (byte) 0x00, (byte) 0x6F, null, (byte) 0x00);
    }
}
