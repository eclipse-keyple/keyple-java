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
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.protocol.ContactsProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.transaction.SeSelectionRequest;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialized selection request to manage the specific characteristics of Calypso POs
 */
public final class PoSelectionRequest extends SeSelectionRequest {
    private static final Logger logger = LoggerFactory.getLogger(PoSelectionRequest.class);

    private int commandIndex;
    private List<Class<? extends AbstractApduResponseParser>> parsingClassList =
            new ArrayList<Class<? extends AbstractApduResponseParser>>();
    private Map<Integer, Byte> readRecordFirstRecordNumberMap = new HashMap<Integer, Byte>();
    private Map<Integer, ReadDataStructure> readRecordDataStructureMap =
            new HashMap<Integer, ReadDataStructure>();

    private final PoClass poClass;

    /**
     * Constructor.
     * 
     * @param seSelector the selector to target a particular SE
     * @param channelState tell if the channel is to be closed or not after the command
     * @param protocolFlag the targeted protocol
     */
    public PoSelectionRequest(SeSelector seSelector, ChannelState channelState,
            SeProtocol protocolFlag) {

        super(seSelector, channelState, protocolFlag);

        commandIndex = 0;

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
     * @return the command index indicating the order of the command in the command list
     */
    private int prepareReadRecordsCmdInternal(byte sfi, ReadDataStructure readDataStructureEnum,
            byte firstRecordNumber, int expectedLength, String extraInfo) {

        /*
         * the readJustOneRecord flag is set to false only in case of multiple read records, in all
         * other cases it is set to true
         */
        boolean readJustOneRecord =
                !(readDataStructureEnum == ReadDataStructure.MULTIPLE_RECORD_DATA);

        addApduRequest(
                new ReadRecordsCmdBuild(poClass, sfi, readDataStructureEnum, firstRecordNumber,
                        readJustOneRecord, (byte) expectedLength, extraInfo).getApduRequest());

        if (logger.isTraceEnabled()) {
            logger.trace("ReadRecords: SFI = {}, RECNUMBER = {}, JUSTONE = {}, EXPECTEDLENGTH = {}",
                    sfi, firstRecordNumber, readJustOneRecord, expectedLength);
        }

        /* keep read record parameters in the dedicated Maps */
        readRecordFirstRecordNumberMap.put(commandIndex, firstRecordNumber);
        readRecordDataStructureMap.put(commandIndex, readDataStructureEnum);

        /* set the parser for the response of this command */
        parsingClassList.add(ReadRecordsRespPars.class);

        /* return and post increment the command index */
        return commandIndex++;
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
     * @return the command index indicating the order of the command in the command list
     */
    public int prepareReadRecordsCmd(byte sfi, ReadDataStructure readDataStructureEnum,
            byte firstRecordNumber, int expectedLength, String extraInfo) {
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
     * @return the command index indicating the order of the command in the command list
     */
    public int prepareReadRecordsCmd(byte sfi, ReadDataStructure readDataStructureEnum,
            byte firstRecordNumber, String extraInfo) {
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
     * @param path path from the CURRENT_DF (CURRENT_DF identifier excluded)
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @return the command index indicating the order of the command in the command list
     */
    public int prepareSelectFileCmd(byte[] path, String extraInfo) {
        addApduRequest(new SelectFileCmdBuild(poClass, path).getApduRequest());
        if (logger.isTraceEnabled()) {
            logger.trace("Select File: PATH = {}", ByteArrayUtils.toHex(path));
        }

        /* set the parser for the response of this command */
        parsingClassList.add(SelectFileRespPars.class);

        /* return and post increment the command index */
        return commandIndex++;
    }

    /**
     * Prepare a select file ApduRequest to be executed following the selection.
     * <p>
     *
     * @param selectControl provides the navigation case: FIRST, NEXT or CURRENT
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @return the command index indicating the order of the command in the command list
     */
    public int prepareSelectFileCmd(SelectFileCmdBuild.SelectControl selectControl,
            String extraInfo) {
        addApduRequest(new SelectFileCmdBuild(poClass, selectControl).getApduRequest());
        if (logger.isTraceEnabled()) {
            logger.trace("Navigate: CONTROL = {}", selectControl);
        }

        /* set the parser for the response of this command */
        parsingClassList.add(SelectFileRespPars.class);

        /* return and post increment the command index */
        return commandIndex++;
    }

    /**
     * Prepare a custom read ApduRequest to be executed following the selection.
     * 
     * @param name the name of the command (will appear in the ApduRequest log)
     * @param apduRequest the ApduRequest (the correct instruction byte must be provided)
     * @return the command index indicating the order of the command in the command list
     */
    public int preparePoCustomReadCmd(String name, ApduRequest apduRequest) {
        addApduRequest(new PoCustomReadCommandBuilder(name, apduRequest).getApduRequest());
        if (logger.isTraceEnabled()) {
            logger.trace("CustomReadCommand: APDUREQUEST = {}", apduRequest);
        }
        /* return and post increment the command index */
        return commandIndex++;
    }

    /**
     * Prepare a custom modification ApduRequest to be executed following the selection.
     *
     * @param name the name of the command (will appear in the ApduRequest log)
     * @param apduRequest the ApduRequest (the correct instruction byte must be provided)
     * @return the command index indicating the order of the command in the command list
     */
    public int preparePoCustomModificationCmd(String name, ApduRequest apduRequest) {
        addApduRequest(new PoCustomModificationCommandBuilder(name, apduRequest).getApduRequest());
        if (logger.isTraceEnabled()) {
            logger.trace("CustomModificationCommand: APDUREQUEST = {}", apduRequest);
        }
        /* return and post increment the command index */
        return commandIndex++;
    }

    /**
     * Return the parser corresponding to the command whose index is provided.
     * 
     * @param seResponse the received SeResponse containing the commands raw responses
     * @param commandIndex the command index
     * @return a parser of the type matching the command
     */
    @Override
    public AbstractApduResponseParser getCommandParser(SeResponse seResponse, int commandIndex) {
        if (commandIndex >= parsingClassList.size()) {
            throw new IllegalArgumentException(
                    "Incorrect command index while getting command parser.");
        }
        if (seResponse.getApduResponses().size() != parsingClassList.size()) {
            throw new IllegalArgumentException(
                    "The number of responses and commands doesn't match.");
        }
        Class<? extends AbstractApduResponseParser> parsingClass =
                parsingClassList.get(commandIndex);
        AbstractApduResponseParser parser;
        if (parsingClass == ReadRecordsRespPars.class) {
            parser = new ReadRecordsRespPars(seResponse.getApduResponses().get(commandIndex),
                    readRecordDataStructureMap.get(commandIndex),
                    readRecordFirstRecordNumberMap.get(commandIndex));
        } else if (parsingClass == SelectFileRespPars.class) {
            parser = new SelectFileRespPars(seResponse.getApduResponses().get(commandIndex));
        } else {
            throw new IllegalArgumentException("No parser available for this command.");
        }
        return parser;
    }

    /**
     * Create a CalypsoPo object containing the selection data received from the plugin
     * 
     * @param seResponse the SE response received
     * @return a {@link CalypsoPo}
     */
    @Override
    protected CalypsoPo parse(SeResponse seResponse) {
        return new CalypsoPo(seResponse, seSelector.getExtraInfo());
    }
}
