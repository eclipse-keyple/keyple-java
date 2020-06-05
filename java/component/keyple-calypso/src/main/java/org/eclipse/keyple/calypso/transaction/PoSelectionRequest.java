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

import java.util.List;
import org.eclipse.keyple.calypso.SelectFileControl;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.exception.CalypsoPoCommandException;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoDesynchronizedExchangesException;
import org.eclipse.keyple.core.selection.AbstractSeSelectionRequest;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Specialized selection request to manage the specific characteristics of Calypso POs */
public final class PoSelectionRequest extends
        AbstractSeSelectionRequest<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> {
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
     * Read a single record from the indicated EF
     *
     * @param sfi the SFI of the EF to read
     * @param recordNumber the record number to read
     * @throws IllegalArgumentException if one of the provided argument is out of range
     */
    public final void prepareReadRecordFile(byte sfi, int recordNumber) {
        addCommandBuilder(CalypsoPoUtils.prepareReadRecordFile(poClass, sfi, recordNumber));
    }

    /**
     * Prepare a select file ApduRequest to be executed following the selection.
     *
     * @param lid LID of the EF to select as a byte array
     * @throws IllegalArgumentException if the argument is not an array of 2 bytes
     */
    public void prepareSelectFile(byte[] lid) {
        addCommandBuilder(CalypsoPoUtils.prepareSelectFile(poClass, lid));
    }

    /**
     * Prepare a select file ApduRequest to be executed following the selection.
     *
     * @param lid LID of the EF to select as a byte array
     * @throws IllegalArgumentException if the argument is not an array of 2 bytes
     */
    public void prepareSelectFile(short lid) {
        byte[] bLid = new byte[] {(byte) ((lid >> 8) & 0xff), (byte) (lid & 0xff),};
        prepareSelectFile(bLid);
    }

    /**
     * Prepare a select file ApduRequest to be executed following the selection.
     *
     * @param selectControl provides the navigation case: FIRST, NEXT or CURRENT
     */
    public void prepareSelectFile(SelectFileControl selectControl) {
        addCommandBuilder(CalypsoPoUtils.prepareSelectFile(poClass, selectControl));
    }

    /**
     * Create a CalypsoPo object containing the selection data received from the plugin
     *
     * @param seResponse the SE response received
     * @return a {@link CalypsoPo}
     * @throws CalypsoDesynchronizedExchangesException if the number of responses is different from
     *         the number of requests
     */
    @Override
    protected CalypsoPo parse(SeResponse seResponse)
            throws CalypsoDesynchronizedExchangesException, CalypsoPoCommandException {

        List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> commandBuilders =
                getCommandBuilders();
        List<ApduResponse> apduResponses = seResponse.getApduResponses();

        if (commandBuilders.size() != apduResponses.size()) {
            throw new CalypsoDesynchronizedExchangesException(
                    "Mismatch in the number of requests/responses");
        }

        CalypsoPo calypsoPo =
                new CalypsoPo(seResponse, seSelector.getSeProtocol().getTransmissionMode());

        if (!commandBuilders.isEmpty()) {
            CalypsoPoUtils.updateCalypsoPo(calypsoPo, commandBuilders, apduResponses);
        }

        return calypsoPo;
    }
}
