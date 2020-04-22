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
import org.eclipse.keyple.calypso.SelectFileControl;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.SelectFileCmdBuild;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoIllegalArgumentException;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoDesynchronisedExchangesException;
import org.eclipse.keyple.core.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.core.selection.AbstractSeSelectionRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialized selection request to manage the specific characteristics of Calypso POs
 */
public final class PoSelectionRequest extends AbstractSeSelectionRequest {
    private static final Logger logger = LoggerFactory.getLogger(PoSelectionRequest.class);
    private final PoClass poClass;

    /**
     * Constructor.
     * 
     * @param poSelector the selector to target a particular SE
     */
    public PoSelectionRequest(PoSelector poSelector) {

        super(poSelector);

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
     *
     * @param sfi the sfi top select
     * @param readDataStructureEnum read mode enum to indicate a SINGLE, MULTIPLE or COUNTER read
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param expectedLength the expected length of the record(s)
     */
    private void prepareReadRecordsCmdInternal(byte sfi, ReadDataStructure readDataStructureEnum,
            byte firstRecordNumber, int expectedLength) {

        /*
         * the readJustOneRecord flag is set to false only in case of multiple read records, in all
         * other cases it is set to true
         */
        boolean readJustOneRecord =
                !(readDataStructureEnum == ReadDataStructure.MULTIPLE_RECORD_DATA);

        addCommandBuilder(new ReadRecordsCmdBuild(poClass, sfi, readDataStructureEnum,
                firstRecordNumber, readJustOneRecord, (byte) expectedLength));
    }

    /**
     * Prepare one or more read record ApduRequest based on the target revision to be executed
     * following the selection.
     * <p>
     * The expected length is provided and its value is checked between 1 and 250.
     *
     * @param sfi the sfi top select
     * @param readDataStructureEnum read mode enum to indicate a SINGLE, MULTIPLE or COUNTER read
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @param expectedLength the expected length of the record(s)
     */
    public void prepareReadRecords(byte sfi, ReadDataStructure readDataStructureEnum,
            byte firstRecordNumber, int expectedLength) {
        if (expectedLength < 1 || expectedLength > 250) {
            throw new IllegalArgumentException("Bad length.");
        }

        prepareReadRecordsCmdInternal(sfi, readDataStructureEnum, firstRecordNumber,
                expectedLength);
    }

    /**
     * Prepare one or more read record ApduRequest based on the target revision to be executed
     * following the selection. No expected length is specified, the record output length is handled
     * automatically.
     *
     * @param sfi the sfi top select
     * @param readDataStructureEnum read mode enum to indicate a SINGLE, MULTIPLE or COUNTER read
     * @param firstRecordNumber the record number to read (or first record to read in case of
     *        several records)
     * @throws CalypsoPoIllegalArgumentException if one of the arguments is incorrect
     */
    public void prepareReadRecords(byte sfi, ReadDataStructure readDataStructureEnum,
            byte firstRecordNumber) throws CalypsoPoIllegalArgumentException {
        if (seSelector.getSeProtocol() == SeCommonProtocols.PROTOCOL_ISO7816_3) {
            throw new CalypsoPoIllegalArgumentException(
                    "In contacts mode, the expected length must be specified.",
                    CalypsoPoCommand.READ_RECORDS);
        }
        prepareReadRecordsCmdInternal(sfi, readDataStructureEnum, firstRecordNumber, 0);
    }

    /**
     * Prepare a select file ApduRequest to be executed following the selection.
     *
     * @param path path from the CURRENT_DF (CURRENT_DF identifier excluded)
     */
    public void prepareSelectFile(byte[] path) {
        addCommandBuilder(new SelectFileCmdBuild(poClass, path));
        if (logger.isTraceEnabled()) {
            logger.trace("Select File: PATH = {}", ByteArrayUtil.toHex(path));
        }
    }

    /**
     * Prepare a select file ApduRequest to be executed following the selection.
     *
     * @param selectControl provides the navigation case: FIRST, NEXT or CURRENT
     */
    public void prepareSelectFile(SelectFileControl selectControl) {
        addCommandBuilder(new SelectFileCmdBuild(poClass, selectControl));
        if (logger.isTraceEnabled()) {
            logger.trace("Navigate: CONTROL = {}", selectControl);
        }
    }

    /**
     * Create a CalypsoPo object containing the selection data received from the plugin
     * 
     * @param seResponse the SE response received
     * @return a {@link CalypsoPo}
     * @throws CalypsoDesynchronisedExchangesException if the number of responses is different from
     *         the number of requests
     */
    @Override
    protected CalypsoPo parse(SeResponse seResponse)
            throws CalypsoDesynchronisedExchangesException {

        List<AbstractApduCommandBuilder> commandBuilders = getCommandBuilders();

        if (commandBuilders.size() != seResponse.getApduResponses().size()) {
            throw new CalypsoDesynchronisedExchangesException(
                    "Mismatch in the number of requests/responses");
        }

        CalypsoPo calypsoPo =
                new CalypsoPo(seResponse, seSelector.getSeProtocol().getTransmissionMode());

        for (AbstractApduCommandBuilder commandBuilder : commandBuilders) {
            // TODO update CalypsoPo with the received data
        }

        return calypsoPo;
    }
}
