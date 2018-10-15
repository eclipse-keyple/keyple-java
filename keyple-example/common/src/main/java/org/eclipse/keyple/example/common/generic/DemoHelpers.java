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

package org.eclipse.keyple.example.common.generic;

import java.util.regex.Pattern;
import org.eclipse.keyple.example.common.calypso.CalypsoBasicInfo;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DemoHelpers {
    private static Logger logger = LoggerFactory.getLogger(DemoHelpers.class);;

    /**
     * Get the terminal which names match the expected pattern
     *
     * @param seProxyService SE Proxy service
     * @param pattern Pattern
     * @return ProxyReader
     * @throws KeypleReaderException Readers are not initialized
     */
    public static ProxyReader getReaderByName(SeProxyService seProxyService, String pattern)
            throws KeypleReaderException {
        Pattern p = Pattern.compile(pattern);
        for (ReaderPlugin plugin : seProxyService.getPlugins()) {
            for (ProxyReader reader : plugin.getReaders()) {
                if (p.matcher(reader.getName()).matches()) {
                    return reader;
                }
            }
        }
        throw new KeypleReaderNotFoundException("Reader name pattern: " + pattern);
    }

    /**
     * Check CSM presence and consistency
     *
     * Throw an exception if the expected CSM is not available
     * 
     * @param csmReader the SAM reader
     */
    public static void checkCsmAndOpenChannel(ProxyReader csmReader) {
        /*
         * check the availability of the CSM doing a ATR based selection, open its physical and
         * logical channels and keep it open
         */
        SeSelection samSelection = new SeSelection(csmReader);

        SeSelector samSelector = new SeSelector(CalypsoBasicInfo.CSM_C1_ATR_REGEX, true, null);

        samSelection.addSelector(samSelector);

        try {
            SeResponse csmCheckResponse = samSelection.processSelection().getSingleResponse();
            if (csmCheckResponse == null) {
                throw new IllegalStateException("Unable to open a logical channel for CSM!");
            } else {
            }
        } catch (KeypleReaderException e) {
            throw new IllegalStateException("Reader exception: " + e.getMessage());

        }
    }

    public abstract void operatePoTransactions();

    /*
     * This method is called when an reader event occurs according to the Observer pattern
     */
    public void update(ReaderEvent event) {
        switch (event.getEventType()) {
            case SE_INSERTED:
                if (logger.isInfoEnabled()) {
                    logger.info("SE INSERTED");
                    logger.info("Start processing of a Calypso PO");
                }
                operatePoTransactions();
                break;
            case SE_REMOVAL:
                if (logger.isInfoEnabled()) {
                    logger.info("SE REMOVED");
                    logger.info("Wait for Calypso PO");
                }
                break;
            default:
                logger.error("IO Error");
        }
    }

}
