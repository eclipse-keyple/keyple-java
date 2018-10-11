/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.transaction;



import org.eclipse.keyple.calypso.command.po.PoCustomCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.transaction.SeSelector;

/**
 * Specialized selector to manage the specific characteristics of Calypso POs
 */
public class PoSelector extends SeSelector {
    private final RevTarget revTarget;

    /**
     * Selection targets definition
     */
    public enum RevTarget {
        TARGET_REV1, TARGET_REV2, TARGET_REV3, TARGET_REV2_REV3
    }

    /**
     * Calypso PO revision 1 selector
     * 
     * @param atrRegex regular expression to check the AnswerToReset of the current PO
     * @param dfLid long ID of the DF to be selected
     * @param keepChannelOpen indicates whether the logical channel should remain open
     * @param protocolFlag the protocol flag to filter POs according to their communication protocol
     */
    public PoSelector(String atrRegex, short dfLid, boolean keepChannelOpen,
            SeProtocol protocolFlag) {
        super(atrRegex, keepChannelOpen, protocolFlag);
        revTarget = RevTarget.TARGET_REV1;
    }

    /**
     * Calypso PO revision 2 and above selector
     * 
     * @param poAid the AID of the targeted PO
     * @param keepChannelOpen indicates whether the logical channel should remain open
     * @param protocolFlag the protocol flag to filter POs according to their communication protocol
     * @param revTarget the targeted revisions. The following possible ReadRecords commands will be
     *        built taking this value into account
     */
    public PoSelector(byte[] poAid, boolean keepChannelOpen, SeProtocol protocolFlag,
            RevTarget revTarget) {
        super(poAid, keepChannelOpen, protocolFlag);
        if (revTarget == RevTarget.TARGET_REV1) {
            throw new IllegalArgumentException("Calypso PO Rev1 cannot be selected with AID.");
        }
        this.revTarget = revTarget;
    }

    /**
     * Prepare one or more read record ApduRequest based on the target revision to be executed
     * following the selection.
     * <p>
     * In the case of a mixed target (rev2 or rev3) two commands are prepared. The first one in rev3
     * format, the second one in rev2 format (mainly class byte)
     * 
     * @param sfi the sfi top select
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param readJustOneRecord the read just one record
     * @param expectedLength the expected length of the record(s)
     * @param extraInfo extra information included in the logs (can be null or empty)
     */
    public void prepareReadRecordsCmd(byte sfi, byte firstRecordNumber, boolean readJustOneRecord,
            byte expectedLength, String extraInfo) {
        switch (this.revTarget) {
            case TARGET_REV1:
                seSelectionApduRequestList
                        .add(new ReadRecordsCmdBuild(PoRevision.REV1_0, sfi, firstRecordNumber,
                                readJustOneRecord, expectedLength, extraInfo).getApduRequest());
                break;
            case TARGET_REV2:
                seSelectionApduRequestList
                        .add(new ReadRecordsCmdBuild(PoRevision.REV2_4, sfi, firstRecordNumber,
                                readJustOneRecord, expectedLength, extraInfo).getApduRequest());
                break;
            case TARGET_REV3:
                seSelectionApduRequestList
                        .add(new ReadRecordsCmdBuild(PoRevision.REV3_1, sfi, firstRecordNumber,
                                readJustOneRecord, expectedLength, extraInfo).getApduRequest());
                break;
            case TARGET_REV2_REV3:
                seSelectionApduRequestList
                        .add(new ReadRecordsCmdBuild(PoRevision.REV3_1, sfi, firstRecordNumber,
                                readJustOneRecord, expectedLength, extraInfo).getApduRequest());
                seSelectionApduRequestList
                        .add(new ReadRecordsCmdBuild(PoRevision.REV2_4, sfi, firstRecordNumber,
                                readJustOneRecord, expectedLength, extraInfo).getApduRequest());
                break;
        }
    }

    /**
     * Prepare a custom ApduRequest to be executed following the selection.
     * 
     * @param name the name of the command (will appear in the ApduRequest log)
     * @param apduRequest the ApduRequest (the correct instruction byte must be provided)
     */
    public void preparePoCustomCmd(String name, ApduRequest apduRequest) {
        seSelectionApduRequestList
                .add(new PoCustomCommandBuilder(name, apduRequest).getApduRequest());
    }
}
