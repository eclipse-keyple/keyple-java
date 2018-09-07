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

/**
 * Iso7816 APDU command builder.
 * <p>
 * It has to be extended by all PO and CSM command builder classes.
 * <p>
 * It provides, through the AbstractApduCommandBuilder superclass, the generic getters to retrieve:
 * <ul>
 * <li>the name of the command,</li>
 * <li>the built APDURequest,</li>
 * <li>the corresponding AbstractApduResponseParser class.</li>
 * </ul>
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
     * The case4 flag is determined from provided arguments. It is true if
     * <ul>
     * <li>Le is not null and equal to 0</li>
     * <li>dataIn is not nul</li>
     * </ul>
     * <p>
     * If dataIn is not null and Le != 0 an IllegalArgumentException is thrown
     * <p>
     * Le must be set to null when no outgoing data is expected.
     * <p>
     * dataIn must be set to null when no ingoing data
     * 
     * @param cla class of instruction
     * @param command instruction code
     * @param p1 instruction parameter 1
     * @param p2 instruction parameter 2
     * @param dataIn bytes sent in the data field of the command. dataIn.limit will be Lc (Number of
     *        bytes present in the data field of the command)
     * @param le maximum number of bytes expected in the data field of the response to the command
     *        (set to 0 is the case where ingoing and outgoing are present. Let the lower layer to
     *        handle the actual length [case4])
     * @return an ApduRequest
     */
    protected ApduRequest setApduRequest(byte cla, CommandsTable command, byte p1, byte p2,
            ByteBuffer dataIn, Byte le) {
        boolean case4;
        /* sanity check */
        if (dataIn != null && le != null && le != 0) {
            throw new IllegalArgumentException(
                    "Le must be equal to 0 when not null and ingoing data are present.");
        }

        /* Buffer allocation for all APDUs */
        ByteBuffer apdu = ByteBuffer.allocate(261);

        /* Build APDU buffer from provided arguments */
        apdu.put(cla);
        apdu.put(command.getInstructionByte());
        apdu.put(p1);
        apdu.put(p2);

        /* ISO7618 case determination and Le management */
        if (dataIn != null) {
            /* set ByteBuffer index at the beginning */
            dataIn.position(0);
            /* append Lc and ingoing data */
            apdu.put((byte) dataIn.limit());
            apdu.put(dataIn);
            if (le != null) {
                /*
                 * case4: ingoing and outgoing data, Le is always set to 0 (see Calypso Reader
                 * Recommendations - T84)
                 */
                case4 = true;
                apdu.put((byte) 0x00);
            } else {
                /* case3: ingoing data only, no Le */
                case4 = false;
            }
        } else {
            if (le != null) {
                /* case2: outgoing data only */
                apdu.put(le);
            } else {
                /* case1: no ingoing, no outgoing data, P3/Le = 0 */
                apdu.put((byte) 0x00);
            }
            case4 = false;
        }

        /* Reset ByteBuffer indexes */
        apdu.limit(apdu.position());
        apdu.position(0);

        return new ApduRequest(apdu, case4).setName(command.getName());
    }
}
