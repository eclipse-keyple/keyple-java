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
package org.eclipse.keyple.example.common.generic;


import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
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
            AbstractDefaultSelectionsResponse defaultSelectionsResponse) throws KeypleException;

    protected abstract void processSeInserted(); // alternative AID selection

    protected abstract void processSeRemoved();

    protected abstract void processUnexpectedSeRemoval();


    /**
     * This flag helps to determine whether the SE_REMOVED event was expected or not (case of SE
     * withdrawal during processing).
     */
    boolean currentlyProcessingSe = false;

    public void update(final ReaderEvent event) {
        logger.info("New reader event: {}", event.getReaderName());

        switch (event.getEventType()) {
            case SE_INSERTED: {
                /* Run the PO processing asynchronously in a detach thread */
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        currentlyProcessingSe = true;
                        processSeInserted(); // optional, to process alternative AID selection
                        /**
                         * Informs the underlying layer of the end of the SE processing, in order to
                         * manage the removal sequence.
                         * <p>
                         * If closing has already been requested, this method will do nothing.
                         */
                        try {
                            ((ObservableReader) SeProxyService.getInstance()
                                    .getPlugin(event.getPluginName())
                                    .getReader(event.getReaderName())).notifySeProcessed();
                        } catch (KeypleReaderNotFoundException e) {
                            e.printStackTrace();
                        } catch (KeyplePluginNotFoundException e) {
                            e.printStackTrace();
                        }
                        currentlyProcessingSe = false;
                    }
                });
                thread.start();
            }
                break;

            case SE_MATCHED: {
                /* Run the PO processing asynchronously in a detach thread */
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        currentlyProcessingSe = true;
                        try {
                            processSeMatch(event.getDefaultSelectionsResponse()); // to process the
                        } catch (KeypleException e) {
                            // TODO Rework error management
                            e.printStackTrace();
                        }
                        // selected
                        // application
                        /**
                         * Informs the underlying layer of the end of the SE processing, in order to
                         * manage the removal sequence.
                         * <p>
                         * If closing has already been requested, this method will do nothing.
                         */
                        try {
                            ((ObservableReader) SeProxyService.getInstance()
                                    .getPlugin(event.getPluginName())
                                    .getReader(event.getReaderName())).notifySeProcessed();
                        } catch (KeypleReaderNotFoundException e) {
                            e.printStackTrace();
                        } catch (KeyplePluginNotFoundException e) {
                            e.printStackTrace();
                        }
                        currentlyProcessingSe = false;
                    }
                });
                thread.start();
            }
                break;

            case SE_REMOVED:
                if (currentlyProcessingSe) {
                    processUnexpectedSeRemoval(); // to clean current SE processing
                    logger.error("Unexpected SE Removal");
                } else {
                    processSeRemoved();
                    if (logger.isInfoEnabled()) {
                        logger.info("Waiting for a SE...");
                    }
                }
                currentlyProcessingSe = false;
                break;
            case TIMEOUT_ERROR:
                logger.error(
                        "Timeout Error: the processing time or the time limit for removing the SE"
                                + " has been exceeded.");
                // do the appropriate processing here but do not prevent the return of this update
                // method (e. g. by
                // raising an exception)
        }
    }
}
