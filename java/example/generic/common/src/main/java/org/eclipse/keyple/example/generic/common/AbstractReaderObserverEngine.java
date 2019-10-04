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
package org.eclipse.keyple.example.generic.common;


import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class is intended to be extended by the applications classes in which the SE
 * insertion, selection, removal is factorized here.
 */
public abstract class AbstractReaderObserverEngine implements ObservableReader.ReaderObserver {

    private static Logger logger = LoggerFactory.getLogger(AbstractReaderObserverEngine.class);


    protected abstract void processSeMatch(
            AbstractDefaultSelectionsResponse defaultSelectionsResponse);

    protected abstract void processSeInsertion(); // alternative AID selection

    protected abstract void processSeRemoval();

    protected abstract void processUnexpectedSeRemoval();


    boolean currentlyProcessingSe = false;


    public void update(final ReaderEvent event) {
        /* Run the PO processing asynchronously in a detach thread */
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (event.getEventType() != ReaderEvent.EventType.SE_INSERTED
                        && logger.isInfoEnabled()) {
                    logger.info(event.getReaderName());
                    logger.info("Start the processing of the SE...");
                }

                switch (event.getEventType()) {
                    case SE_INSERTED:
                        currentlyProcessingSe = true;
                        processSeInsertion(); // optional, to process alternative AID selection
                        currentlyProcessingSe = false;
                        break;

                    case SE_MATCHED:
                        currentlyProcessingSe = true;
                        processSeMatch(event.getDefaultSelectionsResponse()); // to process the
                                                                              // selected
                        // application
                        currentlyProcessingSe = false;
                        break;

                    case AWAITING_SE_INSERTION:
                        if (currentlyProcessingSe) {
                            processUnexpectedSeRemoval(); // to clean current SE processing
                            logger.error("Unexpected SE Removal");
                        } else {
                            processSeRemoval();
                            if (logger.isInfoEnabled()) {
                                logger.info("Waiting for a SE...");
                            }
                        }
                        currentlyProcessingSe = false;
                        break;
                    case AWAITING_SE_REMOVAL:
                        logger.info("Waiting for PO removal...");
                        break;
                    default:
                        logger.error("IO Error");
                }

                if (event.getEventType() == ReaderEvent.EventType.SE_INSERTED
                        || event.getEventType() == ReaderEvent.EventType.SE_MATCHED) {
                    /**
                     * Informs the underlying layer of the end of the SE processing, in order to
                     * manage the removal sequence.
                     * <p>
                     * If closing has already been requested, this method will do nothing.
                     */
                    try {
                        ((ObservableReader) SeProxyService.getInstance()
                                .getPlugin(event.getPluginName()).getReader(event.getReaderName()))
                                        .terminate();
                    } catch (KeypleReaderNotFoundException e) {
                        e.printStackTrace();
                    } catch (KeyplePluginNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
}
