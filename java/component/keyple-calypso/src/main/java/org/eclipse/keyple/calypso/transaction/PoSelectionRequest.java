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
package org.eclipse.keyple.calypso.transaction;



import java.util.*;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.PoCustomModificationCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoCustomReadCommandBuilder;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.SelectFileCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.command.po.parser.SelectFileRespPars;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.eclipse.keyple.seproxy.message.ApduResponse;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.protocol.ContactsProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.transaction.SeSelectionRequest;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialized selector to manage the specific characteristics of Calypso POs
 */
public final class PoSelectionRequest extends SeSelectionRequest {
    private static final Logger logger = LoggerFactory.getLogger(PoSelectionRequest.class);

    private final PoClass poClass;

    /** The list to contain the parsers associated to the prepared commands */
    private List<AbstractApduResponseParser> poResponseParserList =
            new ArrayList<AbstractApduResponseParser>();

    /**
     *
     * @param seSelector
     */
    public PoSelectionRequest(SeSelector seSelector, ChannelState channelState,
            SeProtocol protocolFlag) {
        super(seSelector, channelState, protocolFlag);

        setMatchingClass(CalypsoPo.class);
        setSelectionClass(PoSelectionRequest.class);

        /* No AID selector for a legacy Calypso PO */
        if (seSelector.getAidSelector() == null) {
            poClass = PoClass.LEGACY;
        } else {
            poClass = PoClass.ISO;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Calypso {} selector", poClass);
        }
    }

    /**
     * Prepare one or more read record ApduRequest based on the target revision to be executed
     * following the selection.
     * <p>
     * In the case of a mixed target (rev2 or rev3) two commands are prepared. The first one in rev3
     * format, the second one in rev2 format (mainly class byte)
     * 
     * @param sfi the sfi top select
     * @param readDataStructureEnum read mode enum to indicate a SINGLE, MULTIPLE or COUNTER read
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param expectedLength the expected length of the record(s)
     * @param extraInfo extra information included in the logs (can be null or empty)
     */
    private ReadRecordsRespPars prepareReadRecordsCmdInternal(byte sfi,
            ReadDataStructure readDataStructureEnum, byte firstRecordNumber, int expectedLength,
            String extraInfo) {

        /*
         * the readJustOneRecord flag is set to false only in case of multiple read records, in all
         * other cases it is set to true
         */
        boolean readJustOneRecord =
                !(readDataStructureEnum == ReadDataStructure.MULTIPLE_RECORD_DATA);

        addApduRequest(new ReadRecordsCmdBuild(poClass, sfi, firstRecordNumber, readJustOneRecord,
                (byte) expectedLength, extraInfo).getApduRequest());

        if (logger.isTraceEnabled()) {
            logger.trace("ReadRecords: SFI = {}, RECNUMBER = {}, JUSTONE = {}, EXPECTEDLENGTH = {}",
                    sfi, firstRecordNumber, readJustOneRecord, expectedLength);
        }

        /* create a parser to be returned to the caller */
        ReadRecordsRespPars poResponseParser =
                new ReadRecordsRespPars(firstRecordNumber, readDataStructureEnum);

        /*
         * keep the parser in the CommandParser list
         */
        poResponseParserList.add(poResponseParser);

        return poResponseParser;
    }

    /**
     * Prepare one or more read record ApduRequest based on the target revision to be executed
     * following the selection.
     * <p>
     * The expected length is provided and its value is checked between 1 and 250.
     * <p>
     * In the case of a mixed target (rev2 or rev3) two commands are prepared. The first one in rev3
     * format, the second one in rev2 format (mainly class byte)
     *
     * @param sfi the sfi top select
     * @param readDataStructureEnum read mode enum to indicate a SINGLE, MULTIPLE or COUNTER read
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param expectedLength the expected length of the record(s)
     * @param extraInfo extra information included in the logs (can be null or empty)
     */
    public ReadRecordsRespPars prepareReadRecordsCmd(byte sfi,
            ReadDataStructure readDataStructureEnum, byte firstRecordNumber, int expectedLength,
            String extraInfo) {
        if (expectedLength < 1 || expectedLength > 250) {
            throw new IllegalArgumentException("Bad length.");
        }
        return prepareReadRecordsCmdInternal(sfi, readDataStructureEnum, firstRecordNumber,
                expectedLength, extraInfo);
    }

    /**
     * Prepare one or more read record ApduRequest based on the target revision to be executed
     * following the selection. No expected length is specified, the record output length is handled
     * automatically.
     * <p>
     * In the case of a mixed target (rev2 or rev3) two commands are prepared. The first one in rev3
     * format, the second one in rev2 format (mainly class byte)
     *
     * @param sfi the sfi top select
     * @param readDataStructureEnum read mode enum to indicate a SINGLE, MULTIPLE or COUNTER read
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param extraInfo extra information included in the logs (can be null or empty)
     */
    public ReadRecordsRespPars prepareReadRecordsCmd(byte sfi,
            ReadDataStructure readDataStructureEnum, byte firstRecordNumber, String extraInfo) {
        if (protocolFlag == ContactsProtocols.PROTOCOL_ISO7816_3) {
            throw new IllegalArgumentException(
                    "In contacts mode, the expected length must be specified.");
        }
        return prepareReadRecordsCmdInternal(sfi, readDataStructureEnum, firstRecordNumber, 0,
                extraInfo);
    }

    /**
     * Prepare a select file ApduRequest to be executed following the selection.
     * <p>
     * 
     * @param path path from the MF (MF identifier excluded)
     * @param extraInfo extra information included in the logs (can be null or empty)
     */
    public SelectFileRespPars prepareSelectFileDfCmd(byte[] path, String extraInfo) {
        addApduRequest(
                new SelectFileCmdBuild(poClass, SelectFileCmdBuild.SelectControl.PATH_FROM_MF,
                        SelectFileCmdBuild.SelectOptions.FCI, path).getApduRequest());
        if (logger.isTraceEnabled()) {
            logger.trace("Select File: PATH = {}", ByteArrayUtils.toHex(path));
        }

        /* create a parser to be returned to the caller */
        SelectFileRespPars poResponseParser = new SelectFileRespPars();

        /*
         * keep the parser in a CommandParser list with the number of apduRequest associated with it
         */
        poResponseParserList.add(poResponseParser);

        return poResponseParser;
    }

    /**
     * Prepare a custom read ApduRequest to be executed following the selection.
     * 
     * @param name the name of the command (will appear in the ApduRequest log)
     * @param apduRequest the ApduRequest (the correct instruction byte must be provided)
     */
    public void preparePoCustomReadCmd(String name, ApduRequest apduRequest) {
        addApduRequest(new PoCustomReadCommandBuilder(name, apduRequest).getApduRequest());
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
        addApduRequest(new PoCustomModificationCommandBuilder(name, apduRequest).getApduRequest());
        if (logger.isTraceEnabled()) {
            logger.trace("CustomModificationCommand: APDUREQUEST = {}", apduRequest);
        }
    }


    /**
     * Loops on the SeResponse and updates the list of parsers previously memorized
     *
     * @param seResponse the seResponse from the PO
     */
    protected void updateParsersWithResponses(SeResponse seResponse) {
        /* attempt to update the parsers only if the list is not empty! */
        if (poResponseParserList.size() != 0) {
            Iterator<AbstractApduResponseParser> parserIterator = poResponseParserList.iterator();
            /* double loop to set apdu responses to corresponding parsers */
            for (ApduResponse apduResponse : seResponse.getApduResponses()) {
                if (!parserIterator.hasNext()) {
                    throw new IllegalStateException("Parsers list and responses list mismatch! ");
                }
                parserIterator.next().setApduResponse(apduResponse);
                if (!apduResponse.isSuccessful()) {
                }
            }
        }
    }
}
