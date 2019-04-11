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

    /*
     * list of target classes and selection requests used to build the MatchingSe list in return of
     * processSelection methods
     */
    private List<SeSelectionRequest> seSelectionRequestList = new ArrayList<SeSelectionRequest>();
    private SeRequestSet selectionRequestSet = new SeRequestSet(new LinkedHashSet<SeRequest>());
    private int selectionIndex;

    /**
     * Initializes the SeSelection
     */
    public SeSelection() {
        selectionIndex = 0;
    }

    /**
     * Prepare a selection: add the selection request from the provided selector to the selection
     * request set.
     * <p>
     *
     * @param seSelectionRequest the selector to prepare
     * @return the selection index giving the current selection position in the selection request.
     */
    public int prepareSelection(SeSelectionRequest seSelectionRequest) {
        if (logger.isTraceEnabled()) {
            logger.trace("SELECTORREQUEST = {}, EXTRAINFO = {}",
                    seSelectionRequest.getSelectionRequest(),
                    seSelectionRequest.getSeSelector().getExtraInfo());
        }
        /* build the SeRequest set transmitted to the SE */
        selectionRequestSet.add(seSelectionRequest.getSelectionRequest());
        /* keep the selection request */
        seSelectionRequestList.add(seSelectionRequest);
        /* return and post increment the selection index */
        return selectionIndex++;
    }

    /**
     * Process the selection response either from a
     * {@link org.eclipse.keyple.seproxy.event.ReaderEvent} (default selection) or from an explicit
     * selection.
     * <p>
     * The responses from the {@link SeResponseSet} is parsed and checked.
     * <p>
     * A {@link MatchingSe} list is build and returned. Non matching SE are signaled by a null
     * element in the list
     * 
     * @param selectionResponse the selection response
     * @return the {@link SelectionsResult} containing the result of all prepared selection cases,
     *         including {@link MatchingSe} and {@link SeResponse}.
     */
    private SelectionsResult processSelection(SelectionResponse selectionResponse) {
        SelectionsResult selectionsResult = new SelectionsResult();

        /* null pointer exception protection */
        if (selectionResponse == null) {
            logger.error("selectionResponse shouldn't be null in processSelection.");
            return null;
        }
        int selectionIndex = 0;

        /* Check SeResponses */
        for (SeResponse seResponse : selectionResponse.getSelectionSeResponseSet().getResponses()) {
            if (seResponse != null) {
                /* test if the selection is successful: we should have either a FCI or an ATR */
                if (seResponse.getSelectionStatus() != null
                        && seResponse.getSelectionStatus().hasMatched()) {
                    /*
                     * create a MatchingSe with the class deduced from the selection request during
                     * the selection preparation
                     */
                    MatchingSe matchingSe =
                            seSelectionRequestList.get(selectionIndex).parse(seResponse);

                    selectionsResult.addMatchingSelection(new MatchingSelection(selectionIndex,
                            seSelectionRequestList.get(selectionIndex), matchingSe, seResponse));
                }
            }
            selectionIndex++;
        }
        return selectionsResult;
    }

    /**
     * Parses the response to a selection operation sent to a SE and return a list of
     * {@link MatchingSe}
     * <p>
     * Selection cases that have not matched the current SE are set to null.
     *
     * @param selectionResponse the response from the reader to the {@link DefaultSelectionRequest}
     * @return the {@link SelectionsResult} containing the result of all prepared selection cases,
     *         including {@link MatchingSe} and {@link SeResponse}.
     */
    public SelectionsResult processDefaultSelection(SelectionResponse selectionResponse) {
        if (logger.isTraceEnabled()) {
            logger.trace("Process default SELECTIONRESPONSE ({} response(s))",
                    selectionResponse.getSelectionSeResponseSet().getResponses().size());
        }

        return processSelection(selectionResponse);
    }

    /**
     * Execute the selection process and return a list of {@link MatchingSe}.
     * <p>
     * Selection requests are transmitted to the SE through the supplied SeReader.
     * <p>
     * The process stops in the following cases:
     * <ul>
     * <li>All the selection requests have been transmitted</li>
     * <li>A selection request matches the current SE and the keepChannelOpen flag was true</li>
     * </ul>
     * <p>
     *
     * @param seReader the SeReader on which the selection is made
     * @return the {@link SelectionsResult} containing the result of all prepared selection cases,
     *         including {@link MatchingSe} and {@link SeResponse}.
     * @throws KeypleReaderException if the requests transmission failed
     */
    public SelectionsResult processExplicitSelection(SeReader seReader)
            throws KeypleReaderException {
        if (logger.isTraceEnabled()) {
            logger.trace("Transmit SELECTIONREQUEST ({} request(s))",
                    selectionRequestSet.getRequests().size());
        }

        /* Communicate with the SE to do the selection */
        SeResponseSet seResponseSet = ((ProxyReader) seReader).transmitSet(selectionRequestSet);

        return processSelection(new SelectionResponse(seResponseSet));
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
