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
package org.eclipse.keyple.calypso.command.po.builder;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.command.po.parser.GetDataFciRespPars;
import org.eclipse.keyple.seproxy.message.ApduResponse;

/**
 * This class provides the dedicated constructor to build the Get data APDU commands.
 *
 * This command can not be sent in session because it would generate a 6Cxx status in contact mode
 * and thus make calculation of the digest impossible.
 *
 */
public final class GetDataFciCmdBuild extends AbstractPoCommandBuilder<GetDataFciRespPars> {

    private static final CalypsoPoCommands command = CalypsoPoCommands.GET_DATA_FCI;

    /**
     * Instantiates a new GetDataFciCmdBuild.
     *
     * @param poClass indicates which CLA byte should be used for the Apdu
     */
    public GetDataFciCmdBuild(PoClass poClass) {
        super(command, null);

        request = setApduRequest(poClass.getValue(), command, (byte) 0x00, (byte) 0x6F, null,
                (byte) 0x00);
    }

    @Override
    public GetDataFciRespPars createResponseParser(ApduResponse apduResponse) {
        return new GetDataFciRespPars(apduResponse);
    }
}
