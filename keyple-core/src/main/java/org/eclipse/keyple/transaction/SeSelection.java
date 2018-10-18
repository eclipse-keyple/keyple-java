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
package org.eclipse.keyple.transaction;

import java.util.*;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SeSelection class handles the SE selection process
 */
public class SeSelection {
    private static final Logger logger = LoggerFactory.getLogger(SeSelection.class);

    private final ProxyReader proxyReader;
    private List<MatchingSe> matchingSeList = new ArrayList<MatchingSe>();
    private Set<SeRequest> selectionRequestSet = new LinkedHashSet<SeRequest>();
    private SeResponseSet seResponseSet;
    private MatchingSe selectedSe;

    /**
     * Initializes the SeSelection
     * 
     * @param proxyReader the reader to use to make the selection
     */
    public SeSelection(ProxyReader proxyReader) {
        this.proxyReader = proxyReader;
    }

    /**
     * Prepare a selection: add the selection request from the provided selector to the selection
     * request set.
     * <p>
     * Create a MatchingSe, retain it in a list and return it
     * 
     * @param seSelector the selector to prepare
     * @return a MatchingSe for further information request about this selector
     */
    public MatchingSe prepareSelector(SeSelector seSelector) {
        if (logger.isTraceEnabled()) {
            logger.trace("SELECTORREQUEST = {}", seSelector.getSelectorRequest());
        }
        selectionRequestSet.add(seSelector.getSelectorRequest());
        MatchingSe matchingSe = new MatchingSe(seSelector);
        matchingSeList.add(matchingSe);
        return matchingSe;
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
     * The returned boolean indicates if at least one response was successful.
     * <p>
     * If one of the response also corresponds to a request for which the channel has been asked to
     * be kept open, then it is retained as a selection answer.
     * <p>
     * Responses that have not matched the current PO are set to null.
     * 
     * @return boolean true or false
     * @throws KeypleReaderException
     */
    public boolean processSelection() throws KeypleReaderException {
        boolean selectionSuccessful = false;
        if (logger.isTraceEnabled()) {
            logger.trace("Transmit SELECTIONREQUEST ({} request(s))", selectionRequestSet.size());
        }
        seResponseSet = proxyReader.transmit(new SeRequestSet(selectionRequestSet));
        /* Check SeReponses */
        Iterator<MatchingSe> matchingSeIterator = matchingSeList.iterator();
        for (SeResponse seResponse : seResponseSet.getResponses()) {
            if (seResponse != null) {
                /* test if the selection is successful: we should have either a FCI or an ATR */
                if (seResponse.getFci() != null || seResponse.getAtr() != null) {
                    /* at least one is successful */
                    selectionSuccessful = true;
                    /* update the matchingSe list */
                    if (matchingSeIterator.hasNext()) {
                        MatchingSe matchingSe = matchingSeIterator.next();
                        matchingSe.setSelectionResponse(seResponse);
                        if (matchingSe.isSelectable()) {
                            selectedSe = matchingSe;
                        }
                    } else {
                        throw new IllegalStateException(
                                "The number of selection responses exceeds the number of prepared selectors.");
                    }
                }
            } else {
                if (matchingSeIterator.hasNext()) {
                    /* skip not matching response */
                    matchingSeIterator.next();
                } else {
                    throw new IllegalStateException(
                            "The number of selection responses exceeds the number of prepared selectors.");
                }
            }
        }
        return selectionSuccessful;
    }

    /**
     * Returns the {@link MatchingSe} if there is one, null if not
     * 
     * @return a {@link MatchingSe} or null
     */
    public MatchingSe getSelectedSe() {
        return selectedSe;
    }

    /**
     * Returns the updated list of prepared {@link MatchingSe} updated with the responses to the
     * selection requests sent.
     * 
     * @return a list of {@link MatchingSe}
     */
    public List<MatchingSe> getMatchingSeList() {
        return matchingSeList;
    }
}
