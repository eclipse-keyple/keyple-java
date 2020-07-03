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

import org.eclipse.keyple.calypso.command.po.builder.storedvalue.SvUndebitCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.storedvalue.SvGetRespPars;
import org.eclipse.keyple.calypso.command.sam.AbstractSamCommandBuilder;
import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommand;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.calypso.command.sam.parser.security.SvPrepareOperationRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * Builder for the SAM SV Undebit APDU command.
 */
public class SvPrepareUndebitCmdBuild extends AbstractSamCommandBuilder {
    /** The command reference. */
    private static final CalypsoSamCommand command = CalypsoSamCommand.SV_PREPARE_UNDEBIT;

    /**
     * Instantiates a new SvPrepareUndebitCmdBuild to prepare a transaction to cancel a previous
     * debit transaction.
     *
     * @param samRevision the SAM revision
     * @param svGetRespPars the SV get response parser
     * @param svUndebitCmdBuildDebitCmdBuild the SV undebit command builder
     */
    public SvPrepareUndebitCmdBuild(SamRevision samRevision, SvGetRespPars svGetRespPars,
            SvUndebitCmdBuild svUndebitCmdBuildDebitCmdBuild) {
        super(command, null);

        byte cla = samRevision.getClassByte();
        byte p1 = (byte) 0x01;
        byte p2 = (byte) 0xFF;
        int svGetDataLength = svGetRespPars.getApduResponse().getBytes().length;
        byte[] data = new byte[16 + svGetDataLength]; // header(4) + SvDebit data (12) = 16 bytes

        System.arraycopy(svGetRespPars.getSvGetCommandHeader(), 0, data, 0, 4);
        System.arraycopy(svGetRespPars.getApduResponse().getBytes(), 0, data, 4, svGetDataLength);
        System.arraycopy(svUndebitCmdBuildDebitCmdBuild.getSvUndebitData(), 0, data,
                4 + svGetDataLength, svUndebitCmdBuildDebitCmdBuild.getSvUndebitData().length);

        request = setApduRequest(cla, command, p1, p2, data, null);
    }

    @Override
    public SvPrepareOperationRespPars createResponseParser(ApduResponse apduResponse) {
        return new SvPrepareOperationRespPars(apduResponse, this);
    }
}
