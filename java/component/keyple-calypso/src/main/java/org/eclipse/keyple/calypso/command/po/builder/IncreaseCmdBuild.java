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
import org.eclipse.keyple.calypso.command.po.*;
import org.eclipse.keyple.calypso.command.po.parser.IncreaseRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * The Class IncreaseCmdBuild. This class provides the dedicated constructor to build the Increase
 * APDU command.
 *
 */
public final class IncreaseCmdBuild extends AbstractPoCommandBuilder<IncreaseRespPars> {

    /** The command. */
    private static final CalypsoPoCommands command = CalypsoPoCommands.INCREASE;

    /* Construction arguments */
    private final int sfi;
    private final int counterNumber;
    private final int incValue;

    /**
     * Instantiates a new increase cmd build from command parameters.
     *
     * @param poClass indicates which CLA byte should be used for the Apdu
     * @param counterNumber &gt;= 01h: Counters file, number of the counter. 00h: Simulated Counter
     *        file.
     * @param sfi SFI of the file to select or 00h for current EF
     * @param incValue Value to add to the counter (defined as a positive int &lt;= 16777215
     *        [FFFFFFh])
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws IllegalArgumentException - if the decrement value is out of range
     * @throws IllegalArgumentException - if the command is inconsistent
     */
    public IncreaseCmdBuild(PoClass poClass, byte sfi, byte counterNumber, int incValue,
            String extraInfo) throws IllegalArgumentException {
        super(command, null);

        // only counter number >= 1 are allowed
        if (counterNumber < 1) {
            throw new IllegalArgumentException("Counter number out of range!");
        }

        // check if the incValue is in the allowed interval
        if (incValue < 0 || incValue > 0xFFFFFF) {
            throw new IllegalArgumentException("Increment value out of range!");
        }

        // TODO complete argument checking
        byte cla = poClass.getValue();
        this.sfi = sfi;
        this.counterNumber = counterNumber;
        this.incValue = incValue;

        // convert the integer value into a 3-byte buffer
        byte[] incValueBuffer = new byte[3];
        incValueBuffer[0] = (byte) ((incValue >> 16) & 0xFF);
        incValueBuffer[1] = (byte) ((incValue >> 8) & 0xFF);
        incValueBuffer[2] = (byte) (incValue & 0xFF);

        byte p2 = (byte) (sfi * 8);

        /* this is a case4 command, we set Le = 0 */
        this.request = setApduRequest(cla, command, counterNumber, p2, incValueBuffer, (byte) 0x00);
        if (extraInfo != null) {
            this.addSubName(extraInfo);
        }
    }

    @Override
    public IncreaseRespPars createResponseParser(ApduResponse apduResponse) {
        return new IncreaseRespPars(apduResponse, this);
    }

    /**
     * This command can modify the contents of the PO in session and therefore uses the session
     * buffer.
     * 
     * @return true
     */
    @Override
    public boolean isSessionBufferUsed() {
        return true;
    }

    /**
     * @return the SFI of the accessed file
     */
    public int getSfi() {
        return sfi;
    }

    /**
     * @return the counter number
     */
    public int getCounterNumber() {
        return counterNumber;
    }

    /**
     * @return the increment value
     */
    public int getIncValue() {
        return incValue;
    }
}
