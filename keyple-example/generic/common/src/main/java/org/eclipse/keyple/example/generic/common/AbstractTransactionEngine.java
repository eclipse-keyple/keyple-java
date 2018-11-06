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
package org.eclipse.keyple.example.generic.common;

import java.util.regex.Pattern;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTransactionEngine {
    private static Logger logger = LoggerFactory.getLogger(AbstractTransactionEngine.class);

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
     * Abstract method to be implemented by the class that extends it in order to process particular
     * transactions.
     */
    public abstract void operateSeTransaction();

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
                operateSeTransaction();
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
