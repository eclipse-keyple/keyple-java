/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.utils;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.keyple.calypso.commands.CalypsoCommands;
import org.keyple.commands.CommandsTable;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.seproxy.ApduRequest;

/**
 * This class eases the construction of APDURequest.
 *
 * @author Ixxi
 */
public class RequestUtils {

    private RequestUtils() {}

    public static void controlRequestConsistency(CalypsoCommands command, ApduRequest request)
            throws InconsistentCommandException {
        boolean isRequestInconsistent = true;
        if (request != null) {
            if (request.getbytes() != null) {
                if (request.getbytes().length >= 2) {
                    if (command.getInstructionByte() == request.getbytes()[1]) {
                        isRequestInconsistent = false;
                    }
                }
            }
        }
        if (isRequestInconsistent) {
            throw new InconsistentCommandException();
        }
    }

    public static ApduRequest constructAPDURequest(byte cla, CommandsTable ins, byte p1, byte p2,
            byte[] dataIn) {
        return constructAPDURequest(cla, ins.getInstructionByte(), p1, p2, dataIn, null);
    }

    public static ApduRequest constructAPDURequest(byte cla, CommandsTable ins, byte p1, byte p2,
            byte[] dataIn, byte le) {
        return constructAPDURequest(cla, ins.getInstructionByte(), p1, p2, dataIn, le);
    }

    private static ApduRequest constructAPDURequest(byte cla, byte ins, byte p1, byte p2,
            byte[] dataIn, Byte le) {

        if (dataIn == null) {
            dataIn = new byte[0];
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
            if (dataIn.length == 0 && le == 0x00) {
                localCaseId = 1;
            }
            if (dataIn.length == 0 && le != 0x00) {
                localCaseId = 2;
            }
            if (dataIn.length != 0 && le == 0x00) {
                localCaseId = 3;
            }
            if (dataIn.length != 0 && le != 0x00) {
                localCaseId = 4;
            }
        }

        List<Byte> apdu = new ArrayList<Byte>();

        apdu.add(cla);
        apdu.add(ins);
        apdu.add(p1);
        apdu.add(p2);

        if (dataIn.length != 0) {
            apdu.add((byte) dataIn.length);
            for (byte d : dataIn) {
                apdu.add(d);
            }
        }


        if (forceLe) {
            if (localCaseId == 4) {
                apdu.add((byte) 0x00);
            } else {
                apdu.add(le);
            }
        }
        byte[] array = ArrayUtils.toPrimitive(apdu.toArray(new Byte[0]));
        return new ApduRequest(array, localCaseId == 4);
    }
}
