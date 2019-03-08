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
package org.eclipse.keyple.transaction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.event.DefaultSelectionRequest;
import org.eclipse.keyple.seproxy.event.SelectionResponse;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.message.SeRequestSet;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.message.SeResponseSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SeSelection class handles the SE selection process.
 * <p>
 * It provides a way to do explicit SE selection or to post process a default SE selection.
 */
public final class SeSelection {
    private static final Logger logger = LoggerFactory.getLogger(SeSelection.class);

    private final SeReader seReader;
    private List<MatchingSe> matchingSeList = new ArrayList<MatchingSe>();
    private SeRequestSet selectionRequestSet = new SeRequestSet(new LinkedHashSet<SeRequest>());
    private MatchingSe selectedSe;

    /**
     * Initializes the SeSelection
     * 
     * @param seReader the reader to use to make the selection
     */
    public SeSelection(SeReader seReader) {
        this.seReader = (ProxyReader) seReader;
    }

    /**
     * Prepare a selection: add the selection request from the provided selector to the selection
     * request set.
     * <p>
     * Create a MatchingSe, retain it in a list and return it. The MatchingSe may be an extended
     * class
     * 
     * @param seSelectionRequest the selector to prepare
     * @return a MatchingSe for further information request about this selector
     */
    public MatchingSe prepareSelection(SeSelectionRequest seSelectionRequest) {
        if (logger.isTraceEnabled()) {
            logger.trace("SELECTORREQUEST = {}, EXTRAINFO = {}",
                    seSelectionRequest.getSelectionRequest(),
                    seSelectionRequest.getSeSelector().getExtraInfo());
        }
        selectionRequestSet.add(seSelectionRequest.getSelectionRequest());
        MatchingSe matchingSe = null;
        try {
            Constructor constructor = seSelectionRequest.getMatchingClass()
                    .getConstructor(seSelectionRequest.getSelectionClass());
            matchingSe = (MatchingSe) constructor.newInstance(seSelectionRequest);
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
     * Process the selection response either from a
     * {@link org.eclipse.keyple.seproxy.event.ReaderEvent} (default selection) or from an explicit
     * selection.
     * <p>
     * The responses from the {@link SeResponseSet} is parsed and checked.
     * <p>
     * If one of the responses has matched, the corresponding {@link MatchingSe} is updated with the
     * data from the response.
     * <p>
     * If the updated {@link MatchingSe} is selectable (logical channel requested to be kept open)
     * the selectedSe field is updated (making the MatchingSe available through getSelectedSe).
     *
     * @param selectionResponse the selection response
     * @return true if a successful selection has been made.
     */
    private boolean processSelection(SelectionResponse selectionResponse) {
        boolean selectionSuccessful = false;

        /* null pointer exception protection */
        if (selectionResponse == null) {
            logger.error("selectionResponse shouldn't be null in processSelection.");
            return false;
        }

        /* resets MatchingSe previous data */
        for (MatchingSe matchingSe : matchingSeList) {
            matchingSe.reset();
        }
        /* Check SeResponses */
        Iterator<MatchingSe> matchingSeIterator = matchingSeList.iterator();
        for (SeResponse seResponse : selectionResponse.getSelectionSeResponseSet().getResponses()) {
            if (seResponse != null) {
                /* test if the selection is successful: we should have either a FCI or an ATR */
                if (seResponse.getSelectionStatus() != null
                        && seResponse.getSelectionStatus().hasMatched()) {
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
                } else {

                    if (!matchingSeIterator.hasNext()) {
                        throw new IllegalStateException(
                                "The number of selection responses exceeds the number of prepared selectors.");
                    }

                    matchingSeIterator.next();
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
     * Parses the response to a selection operation sent to a SE and sets the selectedSe if any
     * <p>
     * The returned boolean indicates if at least one response was successful.
     * <p>
     * If one of the response also corresponds to a request for which the channel has been asked to
     * be kept open, then it is retained as a selection answer.
     * <p>
     * Responses that have not matched the current SE are set to null.
     *
     * @param selectionResponse the response from the reader to the {@link DefaultSelectionRequest}
     * @return boolean true if a SE was selected
     */
    public boolean processDefaultSelection(SelectionResponse selectionResponse) {
        if (logger.isTraceEnabled()) {
            logger.trace("Process default SELECTIONRESPONSE ({} response(s))",
                    selectionResponse.getSelectionSeResponseSet().getResponses().size());
        }

        return processSelection(selectionResponse);
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
    public boolean processExplicitSelection() throws KeypleReaderException {
        if (logger.isTraceEnabled()) {
            logger.trace("Transmit SELECTIONREQUEST ({} request(s))",
                    selectionRequestSet.getRequests().size());
        }

        /* Communicate with the SE to do the selection */
        SeResponseSet seResponseSet = ((ProxyReader) seReader).transmitSet(selectionRequestSet);

        return processSelection(new SelectionResponse(seResponseSet));
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
     * The SelectionOperation is the DefaultSelectionRequest to process in ordered to select a SE
     * among others through the selection process. This method is useful to build the prepared
     * selection to be executed by a reader just after a SE insertion.
     * 
     * @return the {@link DefaultSelectionRequest} previously prepared with prepareSelection
     */
    public DefaultSelectionRequest getSelectionOperation() {
        return new DefaultSelectionRequest(selectionRequestSet);
    }
}
