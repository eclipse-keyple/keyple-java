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


import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class is intended to be extended by the applications classes in which the SE insertion,
 * selection, removal is factorized here.
 */
public abstract class AbstractSelectionEngine implements ObservableReader.ReaderObserver {
    private static Logger logger = LoggerFactory.getLogger(AbstractSelectionEngine.class);
    private SeSelection seSelection;
    private ProxyReader reader;

    /**
     * This method will be invoked when a SE has been inserted.
     * 
     * @param selectedSe a {@link MatchingSe} object or null of no SE was selected
     */
    protected abstract void operateSeTransaction(MatchingSe selectedSe);

    /**
     * This method will be invoked upon SE removal
     */
    protected abstract void operateSeRemoval();

    /**
     * This method will be invoked to build the selection SeRequestSet in the two following cases:
     * <ul>
     * <li>when a SE_INSERTED event occurs, just before being sent to the SE</li>
     * <li>when the setImplicitSelectionMode method is called to set the prepared selection</li>
     * </ul>
     * <p>
     * This method must use the prepareSelector method to build the desired selection operation.
     */
    protected abstract void prepareSelection();

    /**
     * Set the selection mode to "implicit" (the explicit mode is the default mode) When the mode is
     * "implicit" the selection operation is processed by the ProxyReader as soon as the SE is
     * inserted and a SE_SELECTED event is notified.
     * 
     * @exception IllegalStateException is thrown if the selection is not initialized
     */
    protected final void setImplicitSelectionMode() {
        prepareSelection();
        if (seSelection == null) {
            throw new IllegalStateException(
                    "Selection must be initialized before setting the implicit selection.");
        }
        this.reader.setSelectionOperation(seSelection.getSelectionOperation());
    }

    /**
     * Initialize the selection process.
     * 
     * @param reader the reader on which the selection has to be operated
     */
    protected final void initializeSelection(ProxyReader reader) {
        seSelection = new SeSelection(reader);
        this.reader = reader;
    }

    /**
     * Add a selection case to the selection process
     * 
     * @param seSelector selection case descriptor
     * @return the MatchinSe object
     * @exception IllegalStateException is thrown if the selection is not initialized
     */
    protected final MatchingSe prepareSelector(SeSelector seSelector) {
        if (seSelection == null) {
            throw new IllegalStateException(
                    "Selection must be initialized before invoking prepareSelector.");
        }
        return seSelection.prepareSelector(seSelector);
    }

    /*
     * This method is called when an reader event occurs according to the Observer pattern
     */
    public void update(ReaderEvent event) {
        switch (event.getEventType()) {
            case SE_INSERTED:
                if (logger.isInfoEnabled()) {
                    logger.info("SE INSERTED");
                    logger.info("Start the processing of the SE...");
                }
                try {
                    prepareSelection();
                    if (seSelection.processSelection()) {
                        operateSeTransaction(seSelection.getSelectedSe());
                    } else {
                        logger.info("The process selection didn't return any matching SE");
                    }
                } catch (KeypleReaderException e) {
                    e.printStackTrace();
                }
                break;
            case SE_SELECTED:
                if (logger.isInfoEnabled()) {
                    logger.info("SE SELECTED");
                    logger.info("Start the processing of the SE...");
                }
                if (seSelection.processSelection(event.getSelectionResponseSet())) {
                    operateSeTransaction(seSelection.getSelectedSe());
                } else {
                    logger.info("The process selection didn't return any matching SE");
                }
                break;
            case SE_REMOVAL:
                if (logger.isInfoEnabled()) {
                    logger.info("SE REMOVED");
                    logger.info("Waiting for a SE...");
                }
                operateSeRemoval();
                break;
            default:
                logger.error("IO Error");
        }
    }
}
