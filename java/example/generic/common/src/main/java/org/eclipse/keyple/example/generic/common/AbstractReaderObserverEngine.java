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


import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.event.SelectionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class is intended to be extended by the applications classes in which the SE
 * insertion, selection, removal is factorized here.
 */
public abstract class AbstractReaderObserverEngine implements ObservableReader.ReaderObserver {

    private static Logger logger = LoggerFactory.getLogger(AbstractReaderObserverEngine.class);


    protected abstract void processSeMatch(SelectionResponse selectionResponse);

    protected abstract void processSeInsertion(); // alternative AID selection

    protected abstract void processSeRemoval();

    protected abstract void processUnexpectedSeRemoval();


    boolean currentlyProcessingSe = false;


    public void update(ReaderEvent event) {
        if (event.getEventType() != ReaderEvent.EventType.SE_INSERTED && logger.isInfoEnabled()) {
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
                processSeMatch(event.getDefaultSelectionResponse()); // to process the selected
                // application
                currentlyProcessingSe = false;
                break;

            case SE_REMOVAL:
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
            default:

                logger.error("IO Error");
        }
    }
}
