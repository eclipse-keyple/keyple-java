/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.utils;

import java.nio.ByteBuffer;
import org.keyple.calypso.commands.CalypsoCommands;
import org.keyple.commands.CommandsTable;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

/**
 * This class eases the construction of APDURequest.
 */
public class RequestUtils {

    private RequestUtils() {}

    public static void controlRequestConsistency(CalypsoCommands command, ApduRequest request)
            throws InconsistentCommandException {
        // Simplifying the strange logic, but I'm not sure this helps much
        if (request != null && request.getBuffer() != null
                && request.getBuffer().get(1) != command.getInstructionByte()) {
            throw new InconsistentCommandException();
        }
    }

    public static ApduRequest constructAPDURequest(byte cla, CommandsTable ins, byte p1, byte p2,
            ByteBuffer dataIn) {
        return constructAPDURequest(cla, ins.getInstructionByte(), p1, p2, dataIn, null);
    }

    public static ApduRequest constructAPDURequest(byte cla, CommandsTable ins, byte p1, byte p2,
            ByteBuffer dataIn, byte le) {
        return constructAPDURequest(cla, ins.getInstructionByte(), p1, p2, dataIn, le);
    }

    static ApduRequest constructAPDURequest(byte cla, byte ins, byte p1, byte p2, ByteBuffer dataIn,
            Byte le) {

        if (dataIn == null) {
            // TODO: Drop this
            dataIn = ByteBuffer.allocate(0);
        } else {
            dataIn.position(0);
        }

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
        return new ApduRequest(apdu, localCaseId == 4);
    }
}
