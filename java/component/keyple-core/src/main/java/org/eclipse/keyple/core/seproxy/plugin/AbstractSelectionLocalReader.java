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
package org.eclipse.keyple.core.seproxy.plugin;



import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.util.ByteArrayUtil;
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

    /**
     * Executes the selection application command and returns the requested data according to
     * AidSelector attributes.
     * 
     * @param aidSelector the selection parameters
     * @return the response to the select application command
     * @throws KeypleIOReaderException if a reader error occurs
     */
    @Override
    protected ApduResponse openChannelForAid(SeSelector.AidSelector aidSelector)
            throws KeypleIOReaderException {
        ApduResponse fciResponse;
        final byte aid[] = aidSelector.getAidToSelect().getValue();
        if (aid == null) {
            throw new IllegalArgumentException("AID must not be null for an AidSelector.");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("[{}] openLogicalChannel => Select Application with AID = {}",
                    this.getName(), ByteArrayUtil.toHex(aid));
        }
        /*
         * build a get response command the actual length expected by the SE in the get response
         * command is handled in transmitApdu
         */
        byte[] selectApplicationCommand = new byte[6 + aid.length];
        selectApplicationCommand[0] = (byte) 0x00; // CLA
        selectApplicationCommand[1] = (byte) 0xA4; // INS
        selectApplicationCommand[2] = (byte) 0x04; // P1: select by name
        // P2: b0,b1 define the File occurrence, b2,b3 define the File control information
        // we use the bitmask defined in the respective enums
        selectApplicationCommand[3] = (byte) (aidSelector.getFileOccurrence().getIsoBitMask()
                | aidSelector.getFileControlInformation().getIsoBitMask());
        selectApplicationCommand[4] = (byte) (aid.length); // Lc
        System.arraycopy(aid, 0, selectApplicationCommand, 5, aid.length); // data
        selectApplicationCommand[5 + aid.length] = (byte) 0x00; // Le

        /*
         * we use here processApduRequest to manage case 4 hack. The successful status codes list
         * for this command is provided.
         */
        fciResponse = processApduRequest(new ApduRequest("Internal Select Application",
                selectApplicationCommand, true, aidSelector.getSuccessfulSelectionStatusCodes()));

        if (!fciResponse.isSuccessful()) {
            logger.trace("[{}] openLogicalChannel => Application Selection failed. SELECTOR = {}",
                    this.getName(), aidSelector);
        }
        return fciResponse;
    }
}
