/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.calypso.command.po.builder;

import org.eclipse.keyple.calypso.command.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.command.po.PoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.PoSendableInSession;

/**
 * The Class ReadRecordsCmdBuild. This class provides the dedicated constructor to build the Read
 * Records APDU command.
 */
public class ReadRecordsCmdBuild extends PoCommandBuilder implements PoSendableInSession {

    /** The command. */
    private static final CalypsoPoCommands command = CalypsoPoCommands.READ_RECORDS;

    /**
     * Instantiates a new read records cmd build.
     *
     * @param revision the revision of the PO
     * @param sfi the sfi top select
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param readJustOneRecord the read just one record
     * @param expectedLength the expected length of the record(s)
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if record number &lt; 1
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public ReadRecordsCmdBuild(PoRevision revision, byte sfi, byte firstRecordNumber,
            boolean readJustOneRecord, byte expectedLength, String extraInfo)
            throws IllegalArgumentException {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }

        if (firstRecordNumber < 1) {
            throw new IllegalArgumentException("Bad record number (< 1)");
        }

        byte cla = PoRevision.REV2_4.equals(this.defaultRevision) ? (byte) 0x94 : (byte) 0x00;
        byte p1 = firstRecordNumber;
        byte p2 = (sfi == (byte) 0x00) ? (byte) 0x05 : (byte) ((byte) (sfi * 8) + 5);
        if (readJustOneRecord) {
            p2 = (byte) (p2 - (byte) 0x01);
        }
        this.request = setApduRequest(cla, command, p1, p2, null, expectedLength);
        if (extraInfo != null) {
            this.addSubName(extraInfo);
        }
    }

    /**
     * Instantiates a new read records cmd build without specifying the expected length. This
     * constructor is allowed only in contactless mode.
     *
     * @param revision the revision of the PO
     * @param sfi the sfi top select
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param readJustOneRecord the read just one record
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if record number &lt; 1
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public ReadRecordsCmdBuild(PoRevision revision, byte sfi, byte firstRecordNumber,
            boolean readJustOneRecord, String extraInfo) throws IllegalArgumentException {
        this(revision, sfi, firstRecordNumber, readJustOneRecord, (byte) 0x00, extraInfo);
    }
}
