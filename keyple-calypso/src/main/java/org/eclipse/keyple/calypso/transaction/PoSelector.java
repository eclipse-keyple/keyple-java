/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.keyple.calypso.transaction;



import org.eclipse.keyple.calypso.command.po.PoCustomModificationCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoCustomReadCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.transaction.SeSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialized selector to manage the specific characteristics of Calypso POs
 */
public class PoSelector extends SeSelector {
    private static final Logger logger = LoggerFactory.getLogger(PoSelector.class);

    private final RevisionTarget revisionTarget;

    /**
     * Selection targets definition
     */
    public enum RevisionTarget {
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
        revisionTarget = RevisionTarget.TARGET_REV1;
        if (logger.isTraceEnabled()) {
            logger.trace("Calypso {} selector", revisionTarget);
        }
    }

    /**
     * Calypso PO revision 2 and above selector
     * 
     * @param poAid the AID of the targeted PO
     * @param keepChannelOpen indicates whether the logical channel should remain open
     * @param protocolFlag the protocol flag to filter POs according to their communication protocol
     * @param revisionTarget the targeted revisions. The following possible ReadRecords commands
     *        will be built taking this value into account
     */
    public PoSelector(byte[] poAid, boolean keepChannelOpen, SeProtocol protocolFlag,
            RevisionTarget revisionTarget) {
        super(poAid, keepChannelOpen, protocolFlag);

        if (revisionTarget == RevisionTarget.TARGET_REV1) {
            throw new IllegalArgumentException("Calypso PO Rev1 cannot be selected with AID.");
        }
        this.revisionTarget = revisionTarget;

        // TODO check if the following affirmation is true for rev2
        /*
         * with Rev2 and 3. SW=6283 in response to a selection (application invalidated) is
         * considered as successful
         */
        selectApplicationSuccessfulStatusCodes.add((short) 0x6283);
        logger.trace("Calypso rev {} selector, SUCCESSFULSTATUSCODES = {}", this.revisionTarget,
                selectApplicationSuccessfulStatusCodes);
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
        switch (this.revisionTarget) {
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
        if (logger.isTraceEnabled()) {
            logger.trace("ReadRecords: SFI = {}, RECNUMBER = {}, JUSTONE = {}, EXPECTEDLENGTH = {}",
                    sfi, firstRecordNumber, readJustOneRecord, expectedLength);
        }
    }

    /**
     * Prepare a custom read ApduRequest to be executed following the selection.
     * 
     * @param name the name of the command (will appear in the ApduRequest log)
     * @param apduRequest the ApduRequest (the correct instruction byte must be provided)
     */
    public void preparePoCustomReadCmd(String name, ApduRequest apduRequest) {
        seSelectionApduRequestList
                .add(new PoCustomReadCommandBuilder(name, apduRequest).getApduRequest());
        if (logger.isTraceEnabled()) {
            logger.trace("CustomReadCommand: APDUREQUEST = {}", apduRequest);
        }
    }

    /**
     * Prepare a custom modification ApduRequest to be executed following the selection.
     *
     * @param name the name of the command (will appear in the ApduRequest log)
     * @param apduRequest the ApduRequest (the correct instruction byte must be provided)
     */
    public void preparePoCustomModificationCmd(String name, ApduRequest apduRequest) {
        seSelectionApduRequestList
                .add(new PoCustomModificationCommandBuilder(name, apduRequest).getApduRequest());
        if (logger.isTraceEnabled()) {
            logger.trace("CustomModificationCommand: APDUREQUEST = {}", apduRequest);
        }
    }
}
