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
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;
import org.eclipse.keyple.calypso.command.po.parser.SelectFileRespPars;
import org.eclipse.keyple.seproxy.message.ApduResponse;

/**
 * This class provides the dedicated constructor to build the Select File APDU commands.
 *
 */
public final class SelectFileCmdBuild extends AbstractPoCommandBuilder<SelectFileRespPars>
        implements PoSendableInSession {

    private static final CalypsoPoCommands command = CalypsoPoCommands.SELECT_FILE;

    public enum SelectControl {
        FIRST, NEXT, CURRENT_DF
    }

    /**
     * Instantiates a new SelectFileCmdBuild to select the first, next or current file in the
     * current DF.
     *
     * @param poClass indicates which CLA byte should be used for the Apdu
     * @param selectControl the selection mode control: FIRST, NEXT or CURRENT
     */
    public SelectFileCmdBuild(PoClass poClass, SelectControl selectControl) {
        super(command, null);
        byte p1;
        byte p2;
        byte[] selectData = new byte[] {0x00, 0x00};
        switch (selectControl) {
            case FIRST:
                p1 = (byte) 0x02;
                p2 = (byte) 0x00;
                break;
            case NEXT:
                p1 = (byte) 0x02;
                p2 = (byte) 0x02;
                break;
            case CURRENT_DF:
                p1 = (byte) 0x09;
                p2 = (byte) 0x00;
                break;
            default:
                throw new IllegalStateException(
                        "Unsupported selectControl parameter " + selectControl.toString());
        }

        request = setApduRequest(poClass.getValue(), command, p1, p2, selectData, (byte) 0x00);
    }

    /**
     * Instantiates a new SelectFileCmdBuild to select the first, next or current file in the
     * current DF.
     *
     * @param poClass indicates which CLA byte should be used for the Apdu
     * @param selectionPath the file identifier path
     */
    public SelectFileCmdBuild(PoClass poClass, byte[] selectionPath) {
        super(command, null);
        request = setApduRequest(poClass.getValue(), command, (byte) 0x09, (byte) 0x00,
                selectionPath, (byte) 0x00);
    }

    @Override
    public SelectFileRespPars createResponseParser(ApduResponse apduResponse) {
        return new SelectFileRespPars(apduResponse);
    }
}
