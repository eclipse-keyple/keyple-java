/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po.parser;

import org.keyple.commands.ApduResponseParser;
import org.keyple.seproxy.ApduResponse;

/**
 * The Class UpdateRecordRespPars. This class provides status code properties of an Update Record
 * response. the Update Record APDU command
 *
 * @author Ixxi .
 */
public class UpdateRecordRespPars extends ApduResponseParser {


    /**
     * Instantiates a new UpdateRecordRespPars.
     *
     * @param response the response from the Update Records APDU command
     */
    public UpdateRecordRespPars(ApduResponse response) {

        super(response);
        initStatusTable();
    }

    /**
     * Initializes the status table.
     */
    private void initStatusTable() {
        statusTable.put(new byte[] {(byte) 0x64, (byte) 0x00},
                new StatusProperties(false, "Too many modifications in session."));
        statusTable.put(new byte[] {(byte) 0x67, (byte) 0x00},
                new StatusProperties(false, "Lc value not supported."));
        statusTable.put(new byte[] {(byte) 0x69, (byte) 0x81}, new StatusProperties(false,
                "Command forbidden on cyclic files when the record exists and is not record 01h and on binary files."));
        statusTable.put(new byte[] {(byte) 0x69, (byte) 0x82}, new StatusProperties(false,
                "Security conditions not fulfilled (no session, wrong key, encryption required)."));
        statusTable.put(new byte[] {(byte) 0x69, (byte) 0x85}, new StatusProperties(false,
                "Access forbidden (Never access mode, DF is invalidated, etc..)."));
        statusTable.put(new byte[] {(byte) 0x69, (byte) 0x86},
                new StatusProperties(false, "Command not allowed (no current EF)."));
        statusTable.put(new byte[] {(byte) 0x6A, (byte) 0x82},
                new StatusProperties(false, "File not found."));
        statusTable.put(new byte[] {(byte) 0x6A, (byte) 0x83}, new StatusProperties(false,
                "Record is not found (record index is 0 or above NumRec)."));
        statusTable.put(new byte[] {(byte) 0x6B, (byte) 0x00},
                new StatusProperties(false, "P2 value not supported."));
        statusTable.put(new byte[] {(byte) 0x90, (byte) 0x00},
                new StatusProperties(true, "Succesfull execution."));
    }

}
