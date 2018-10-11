/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.transaction;

import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handled the SE selection process
 */
public class SeSelection {
    private static final Logger logger = LoggerFactory.getLogger(SeSelection.class);

    private final ProxyReader proxyReader;
    private Set<SeRequest> selectionRequestSet = new LinkedHashSet<SeRequest>();

    /**
     * Initializes the SeSelection
     * 
     * @param proxyReader the reader to use to make the selection
     */
    public SeSelection(ProxyReader proxyReader) {
        this.proxyReader = proxyReader;
    }

    /**
     * Adds a SeSelector to the list of selection requests
     * 
     * @param seSelector
     */
    public void addSelector(SeSelector seSelector) {
        if (logger.isTraceEnabled()) {
            logger.trace("SELECTORREQUEST = {}", seSelector.getSelectorRequest());
        }
        selectionRequestSet.add(seSelector.getSelectorRequest());
    }

    /**
     * Execute the selection process.
     * <p>
     * The selection requests are transmitted to the SE.
     * <p>
     * The process stops in the following cases:
     * <ul>
     * <li>All the selection requests have been transmitted</li>
     * <li>A selection request matches the current SE and the keepChannelOpen flag was true</li>
     * </ul>
     * <p>
     * The returned SeResponseSet contains as many SeResponse as there has been a transmission of
     * SeRequest.
     * <p>
     * Responses that have not matched the current PO are set to null.
     * 
     * @return
     * @throws KeypleReaderException
     */
    public SeResponseSet processSelection() throws KeypleReaderException {
        if (logger.isTraceEnabled()) {
            logger.trace("Transmit SELECTIONREQUEST ({} request(s))", selectionRequestSet.size());
        }
        return proxyReader.transmit(new SeRequestSet(selectionRequestSet));
    }
}
