/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.sam.builder.security;

import org.eclipse.keyple.calypso.command.po.builder.storedvalue.SvReloadCmdBuild;
import org.eclipse.keyple.calypso.command.sam.AbstractSamCommandBuilder;
import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommand;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.parser.security.SvPrepareOperationRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * Builder for the SAM SV Prepare Load APDU command.
 */
public class SvPrepareLoadCmdBuild extends AbstractSamCommandBuilder<SvPrepareOperationRespPars> {
    /** The command reference. */
    private static final CalypsoSamCommand command = CalypsoSamCommand.SV_PREPARE_LOAD;

    /**
     * Instantiates a new SvPrepareLoadCmdBuild to a load transaction.
     * <p>
     * Build the SvPrepareLoad APDU from the SvGet command and response, the SvReload partial
     * command
     * 
     * @param samRevision the SAM revision
     * @param svGetHeader the SV Get command header
     * @param svGetData a byte array containing the data from the SV get command and response
     * @param svReloadCmdBuild the SV reload command builder
     */
    public SvPrepareLoadCmdBuild(SamRevision samRevision, byte[] svGetHeader, byte[] svGetData,
            SvReloadCmdBuild svReloadCmdBuild) {
        super(command, null);

        byte cla = samRevision.getClassByte();
        byte p1 = (byte) 0x01;
        byte p2 = (byte) 0xFF;
        byte[] data = new byte[19 + svGetData.length]; // header(4) + SvReload data (15) = 19 bytes

        System.arraycopy(svGetHeader, 0, data, 0, 4);
        System.arraycopy(svGetData, 0, data, 4, svGetData.length);
        System.arraycopy(svReloadCmdBuild.getSvReloadData(), 0, data, 4 + svGetData.length,
                svReloadCmdBuild.getSvReloadData().length);

        request = setApduRequest(cla, command, p1, p2, data, null);
    }

    @Override
    public SvPrepareOperationRespPars createResponseParser(ApduResponse apduResponse) {
        return new SvPrepareOperationRespPars(apduResponse, this);
    }
}
