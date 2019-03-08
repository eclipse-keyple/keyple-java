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
package org.eclipse.keyple.seproxy.plugin;



import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.seproxy.message.*;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings({"PMD.ModifiedCyclomaticComplexity", "PMD.CyclomaticComplexity",
        "PMD.StdCyclomaticComplexity"})
/**
 * Local reader class implementing the logical channel opening based on the selection of the SE
 * application
 */
public abstract class AbstractSelectionLocalReader extends AbstractLocalReader
        implements ObservableReader {
    private static final Logger logger =
            LoggerFactory.getLogger(AbstractSelectionLocalReader.class);

    protected AbstractSelectionLocalReader(String pluginName, String readerName) {
        super(pluginName, readerName);
    }

    /** ==== ATR filtering and application selection by AID ================ */

    /**
     * Build a select application command, transmit it to the SE and deduct the SelectionStatus.
     * 
     * @param seSelector the targeted application SE selector
     * @return the SelectionStatus
     * @throws KeypleIOReaderException if a reader error occurs
     */
    protected SelectionStatus openLogicalChannel(SeSelector seSelector)
            throws KeypleIOReaderException {
        ApduResponse fciResponse;
        byte[] atr = getATR();
        boolean selectionHasMatched = true;
        SelectionStatus selectionStatus;

        /** Perform ATR filtering if requested */
        if (seSelector.getAtrFilter() != null) {
            if (atr == null) {
                throw new KeypleIOReaderException("Didn't get an ATR from the SE.");
            }

            if (logger.isTraceEnabled()) {
                logger.trace("[{}] openLogicalChannel => ATR: {}", this.getName(),
                        ByteArrayUtils.toHex(atr));
            }
            if (!seSelector.getAtrFilter().atrMatches(atr)) {
                logger.trace("[{}] openLogicalChannel => ATR didn't match. SELECTOR = {}",
                        this.getName(), seSelector);
                selectionHasMatched = false;
            }
        }

        /**
         * Perform application selection if requested and if ATR filtering matched or was not
         * requested
         */
        if (selectionHasMatched && seSelector.getAidSelector() != null) {
            final SeSelector.AidSelector aidSelector = seSelector.getAidSelector();
            final byte aid[] = aidSelector.getAidToSelect();
            if (aid == null) {
                throw new IllegalArgumentException("AID must not be null for an AidSelector.");
            }
            if (logger.isTraceEnabled()) {
                logger.trace("[{}] openLogicalChannel => Select Application with AID = {}",
                        this.getName(), ByteArrayUtils.toHex(aid));
            }
            /*
             * build a get response command the actual length expected by the SE in the get response
             * command is handled in transmitApdu
             */
            byte[] selectApplicationCommand = new byte[6 + aid.length];
            selectApplicationCommand[0] = (byte) 0x00; // CLA
            selectApplicationCommand[1] = (byte) 0xA4; // INS
            selectApplicationCommand[2] = (byte) 0x04; // P1: select by name
            if (!aidSelector.isSelectNext()) {
                selectApplicationCommand[3] = (byte) 0x00; // P2: requests the first occurrence
            } else {
                selectApplicationCommand[3] = (byte) 0x02; // P2: requests the next occurrence
            }
            selectApplicationCommand[4] = (byte) (aid.length); // Lc
            System.arraycopy(aid, 0, selectApplicationCommand, 5, aid.length); // data
            selectApplicationCommand[5 + aid.length] = (byte) 0x00; // Le

            /*
             * we use here processApduRequest to manage case 4 hack. The successful status codes
             * list for this command is provided.
             */
            fciResponse = processApduRequest(
                    new ApduRequest("Internal Select Application", selectApplicationCommand, true,
                            aidSelector.getSuccessfulSelectionStatusCodes()));

            if (!fciResponse.isSuccessful()) {
                logger.trace(
                        "[{}] openLogicalChannel => Application Selection failed. SELECTOR = {}",
                        this.getName(), aidSelector);
            }
            /*
             * The ATR filtering matched or was not requested. The selection status is determined by
             * the answer to the select application command.
             */
            selectionStatus = new SelectionStatus(new AnswerToReset(atr), fciResponse,
                    fciResponse.isSuccessful());
        } else {
            /*
             * The ATR filtering didn't match or no AidSelector was provided. The selection status
             * is determined by the ATR filtering.
             */
            selectionStatus = new SelectionStatus(new AnswerToReset(atr),
                    new ApduResponse(null, null), selectionHasMatched);
        }
        return selectionStatus;
    }
}
