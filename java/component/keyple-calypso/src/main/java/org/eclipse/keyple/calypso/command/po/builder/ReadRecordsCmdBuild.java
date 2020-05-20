/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
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
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * The {@link ReadRecordsCmdBuild} class provides the dedicated constructor to build the Read
 * Records APDU command.
 */
public final class ReadRecordsCmdBuild extends AbstractPoCommandBuilder<ReadRecordsRespPars> {

    private static final CalypsoPoCommand command = CalypsoPoCommand.READ_RECORDS;

    public enum ReadMode {
        ONE_RECORD, MULTIPLE_RECORD
    }

    // Construction arguments used for parsing
    private final int sfi;
    private final int firstRecordNumber;
    private final ReadMode readMode;

    /**
     * Instantiates a new read records cmd build.
     *
     * @param poClass indicates which CLA byte should be used for the Apdu
     * @param sfi the sfi top select
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param readMode read mode, requests the reading of one or all the records
     * @param expectedLength the expected length of the record(s)
     * @throws IllegalArgumentException - if record number &lt; 1
     * @throws IllegalArgumentException - if the request is inconsistent
     */
    public ReadRecordsCmdBuild(PoClass poClass, int sfi, int firstRecordNumber, ReadMode readMode,
            int expectedLength) throws IllegalArgumentException {
        super(command, null);

        this.sfi = sfi;
        this.firstRecordNumber = firstRecordNumber;
        this.readMode = readMode;

        byte p1 = (byte) firstRecordNumber;
        byte p2 = (sfi == (byte) 0x00) ? (byte) 0x05 : (byte) ((byte) (sfi * 8) + 5);
        if (readMode == ReadMode.ONE_RECORD) {
            p2 = (byte) (p2 - (byte) 0x01);
        }
        byte le = (byte) expectedLength;
        this.request = setApduRequest(poClass.getValue(), command, p1, p2, null, le);

        if (logger.isDebugEnabled()) {
            String extraInfo = String.format("SFI=%02X, REC=%d, READMODE=%s, EXPECTEDLENGTH=%d",
                    sfi, firstRecordNumber, readMode, expectedLength);
            this.addSubName(extraInfo);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReadRecordsRespPars createResponseParser(ApduResponse apduResponse) {
        return new ReadRecordsRespPars(apduResponse, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSessionBufferUsed() {
        return false;
    }

    /** @return the SFI of the accessed file */
    public int getSfi() {
        return sfi;
    }

    /** @return the number of the first record to read */
    public int getFirstRecordNumber() {
        return firstRecordNumber;
    }

    /** @return the readJustOneRecord flag */
    public ReadMode getReadMode() {
        return readMode;
    }
}
