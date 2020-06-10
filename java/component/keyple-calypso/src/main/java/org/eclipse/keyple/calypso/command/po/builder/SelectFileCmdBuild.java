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

import org.eclipse.keyple.calypso.SelectFileControl;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.parser.SelectFileRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;

/**
 * This class provides the dedicated constructor to build the Select File APDU commands.
 *
 */
public final class SelectFileCmdBuild extends AbstractPoCommandBuilder<SelectFileRespPars> {

    private static final CalypsoPoCommand command = CalypsoPoCommand.SELECT_FILE;

    /* Construction arguments */
    private final byte[] path;
    private final SelectFileControl selectFileControl;

    /**
     * Instantiates a new SelectFileCmdBuild to select the first, next or current file in the
     * current DF.
     *
     * @param poClass indicates which CLA byte should be used for the Apdu
     * @param selectFileControl the selection mode control: FIRST, NEXT or CURRENT
     */
    public SelectFileCmdBuild(PoClass poClass, SelectFileControl selectFileControl) {
        super(command, null);

        this.path = null;
        this.selectFileControl = selectFileControl;

        byte cla = poClass.getValue();
        byte p1;
        byte p2;
        byte[] selectData = new byte[] {0x00, 0x00};
        switch (selectFileControl) {
            case FIRST_EF:
                p1 = (byte) 0x02;
                p2 = (byte) 0x00;
                break;
            case NEXT_EF:
                p1 = (byte) 0x02;
                p2 = (byte) 0x02;
                break;
            case CURRENT_DF:
                p1 = (byte) 0x09;
                p2 = (byte) 0x00;
                break;
            default:
                throw new IllegalStateException(
                        "Unsupported selectFileControl parameter " + selectFileControl.toString());
        }

        request = setApduRequest(cla, command, p1, p2, selectData, (byte) 0x00);

        if (logger.isDebugEnabled()) {
            String extraInfo = String.format("SELECTIONCONTROL=%s", selectFileControl);
            this.addSubName(extraInfo);
        }
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

        this.path = selectionPath;
        this.selectFileControl = null;

        // handle the REV1 case
        byte p1 = (byte) (poClass == PoClass.LEGACY ? 0x08 : 0x09);

        request = setApduRequest(poClass.getValue(), command, p1, (byte) 0x00, selectionPath,
                (byte) 0x00);

        if (logger.isDebugEnabled()) {
            String extraInfo =
                    String.format("SELECTIONPATH=%s", ByteArrayUtil.toHex(selectionPath));
            this.addSubName(extraInfo);
        }
    }

    @Override
    public SelectFileRespPars createResponseParser(ApduResponse apduResponse) {
        return new SelectFileRespPars(apduResponse, this);
    }

    /**
     * This command doesn't modify the contents of the PO and therefore doesn't uses the session
     * buffer.
     *
     * @return false
     */
    @Override
    public boolean isSessionBufferUsed() {
        return false;
    }

    /**
     * The selection path can be null if the chosen constructor targets the current EF
     *
     * @return the selection path or null
     */
    public byte[] getPath() {
        return path;
    }

    /**
     * The file selection control can be null if the chosen constructor targets an explicit path
     *
     * @return the select file control or null
     */
    public SelectFileControl getSelectFileControl() {
        return selectFileControl;
    }
}
