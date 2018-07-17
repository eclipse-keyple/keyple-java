/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.commands.utils;

import java.nio.ByteBuffer;
import org.eclipse.keyple.commands.CommandsTable;
import org.eclipse.keyple.seproxy.ApduRequest;

/**
 * Eases the creation of {@link ApduRequest} requests.
 */
public class RequestUtils {

    private RequestUtils() {}

    /**
     * Checks the consistency of the request
     * 
     * @param command
     * @param request
     * @throws java.lang.IllegalArgumentException - if the instruction byte is not the expected one
     */
    public static void controlRequestConsistency(CommandsTable command, ApduRequest request)
            throws IllegalArgumentException {
        // Simplifying the strange logic, but I'm not sure this helps much
        if (request != null && request.getBytes() != null
                && request.getBytes().get(1) != command.getInstructionByte()) {
            throw new IllegalArgumentException(
                    "Inconsistent request: instruction bytes don't match!");
        }
    }

    public static ApduRequest constructAPDURequest(byte cla, CommandsTable ins, byte p1, byte p2,
            ByteBuffer dataIn) {
        return constructAPDURequest(cla, ins, p1, p2, dataIn, null);
    }

    /*
     * public static ApduRequest constructAPDURequest(byte cla, CommandsTable ins, byte p1, byte p2,
     * ByteBuffer dataIn, byte le) { return constructAPDURequest(cla, ins.getInstructionByte(), p1,
     * p2, dataIn, le); }
     */

    public static ApduRequest constructAPDURequest(byte cla, CommandsTable command, byte p1,
            byte p2, ByteBuffer dataIn, Byte le) {

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
