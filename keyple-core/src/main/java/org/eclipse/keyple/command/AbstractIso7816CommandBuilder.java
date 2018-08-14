/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.command;

import java.nio.ByteBuffer;
import org.eclipse.keyple.seproxy.ApduRequest;

@SuppressWarnings({"PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity",
        "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity"})
/**
 * Iso7816 APDU command builder. It has to be extended by all PO and CSM command builder classes, it
 * provides, through the AbstractApduCommandBuilder superclass,the generic getters to retrieve: the
 * name of the command, the built APDURequest, the corresponding AbstractApduResponseParser class.
 */
public abstract class AbstractIso7816CommandBuilder extends AbstractApduCommandBuilder {

    /**
     * Abstract constructor to build an APDU request with a command reference and a byte array.
     *
     * @param commandReference command reference
     * @param request request
     */
    public AbstractIso7816CommandBuilder(CommandsTable commandReference, ApduRequest request) {
        super(commandReference, request);
    }

    /**
     * Helper method to create an ApduRequest from separated elements.
     * <p>
     * Case 4 is determined from provided arguments: if outgoing data is present and ingoing data is
     * expected (le > 0), we are in case 4.
     * <p>
     * le must be set to null when no outgoing data is expected.
     * 
     * @param cla
     * @param command
     * @param p1
     * @param p2
     * @param dataIn
     * @param le
     * @return an ApduRequest
     */
    protected ApduRequest setApduRequest(byte cla, CommandsTable command, byte p1, byte p2,
            ByteBuffer dataIn, Byte le) {
        if (dataIn == null) {
            // TODO: Drop this
            dataIn = ByteBuffer.allocate(0);
        } else {
            dataIn.position(0);
        }

        byte ins = command.getInstructionByte();

        boolean forceLe;
        if (le == null) {
            le = 0;
            forceLe = false;
        } else {
            forceLe = true;
        }

        int localCaseId = 0;
        {
            // try to retrieve case
            if (dataIn.limit() == 0 && le == 0x00) {
                localCaseId = 1;
            }
            if (dataIn.limit() == 0 && le != 0x00) {
                localCaseId = 2;
            }
            if (dataIn.limit() != 0 && le == 0x00) {
                localCaseId = 3;
            }
            if (dataIn.limit() != 0 && le != 0x00) {
                localCaseId = 4;
            }
        }

        ByteBuffer apdu = ByteBuffer.allocate(261);

        apdu.put(cla);
        apdu.put(ins);
        apdu.put(p1);
        apdu.put(p2);

        if (dataIn.limit() != 0) {
            apdu.put((byte) dataIn.limit());
            apdu.put(dataIn);
        }


        if (forceLe) {
            if (localCaseId == 4) {
                apdu.put((byte) 0x00);
            } else {
                apdu.put(le);
            }
        }

        apdu.limit(apdu.position());
        apdu.position(0);

        // byte[] array = ArrayUtils.toPrimitive(apdu.toArray(new Byte[0]));
        return new ApduRequest(apdu, localCaseId == 4).setName(command.getName());
    }
}
