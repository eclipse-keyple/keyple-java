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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SeSelection class handles the SE selection process
 */
public final class SeSelection {
    private static final Logger logger = LoggerFactory.getLogger(SeSelection.class);

    private final ProxyReader proxyReader;
    private List<MatchingSe> matchingSeList = new ArrayList<MatchingSe>();
    private Set<SeRequest> selectionRequestSet = new LinkedHashSet<SeRequest>();
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
     * Create a MatchingSe, retain it in a list and return it. The MatchingSe may be an extended
     * class
     * 
     * @param seSelector the selector to prepare
     * @return a MatchingSe for further information request about this selector
     */
    public MatchingSe prepareSelector(SeSelector seSelector) {
        if (logger.isTraceEnabled()) {
            logger.trace("SELECTORREQUEST = {}, EXTRAINFO = {}", seSelector.getSelectorRequest(),
                    seSelector.getExtraInfo());
        }
        selectionRequestSet.add(seSelector.getSelectorRequest());
        MatchingSe matchingSe = null;
        try {
            Constructor constructor =
                    seSelector.getMatchingClass().getConstructor(seSelector.getSelectorClass());
            matchingSe = (MatchingSe) constructor.newInstance(seSelector);
            matchingSeList.add(matchingSe);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return matchingSe;
    }

    /**
     * Parses the response to a selection operation sent to a SE and sets the selectedSe if any
     * <p>
     * The returned boolean indicates if at least one response was successful.
     * <p>
     * If one of the response also corresponds to a request for which the channel has been asked to
     * be kept open, then it is retained as a selection answer.
     * <p>
     * Responses that have not matched the current SE are set to null.
     * 
     * @param seResponseSet the response from the reader to the selection SeRequestSet
     * @return boolean true if a SE was selected
     */
    public boolean processSelection(SeResponseSet seResponseSet) {
        boolean selectionSuccessful = false;
        /* Check SeResponses */
        Iterator<MatchingSe> matchingSeIterator = matchingSeList.iterator();
        for (SeResponse seResponse : seResponseSet.getResponses()) {
            if (seResponse != null) {
                /* test if the selection is successful: we should have either a FCI or an ATR */
                if (seResponse.getSelectionStatus().hasMatched()) {
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
     * @return boolean true if a SE was selected
     * @throws KeypleReaderException if the requests transmission failed
     */
    public boolean processSelection() throws KeypleReaderException {
        if (logger.isTraceEnabled()) {
            logger.trace("Transmit SELECTIONREQUEST ({} request(s))", selectionRequestSet.size());
        }

        /* Communicate with the SE to do the selection */
        SeResponseSet seResponseSet =
                proxyReader.transmitSet(new SeRequestSet(selectionRequestSet));

        return processSelection(seResponseSet);
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

    /**
     * The SelectionOperation is the SeRequestSet to process in ordered to select a SE among others
     * through the selection process. This method is useful to build the prepared selection to be
     * executed by a reader just after a SE insertion.
     * 
     * @return the SeRequestSet previously prepared with prepareSelector
     */
    public SeRequestSet getSelectionOperation() {
        return new SeRequestSet(selectionRequestSet);
    }
}
