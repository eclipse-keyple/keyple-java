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
package org.eclipse.keyple.core.selection;

import java.util.*;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsResponse;
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
     * list of selection requests used to build the AbstractMatchingSe list in return of
     * processSelection methods
     */
    private final List<AbstractSeSelectionRequest> seSelectionRequests =
            new ArrayList<AbstractSeSelectionRequest>();
    private MultiSeRequestProcessing multiSeRequestProcessing;
    private ChannelControl channelControl;

    /**
     * Constructor.
     * 
     * @param multiSeRequestProcessing the multi se processing mode
     * @param channelControl indicates if the channel has to be closed at the end of the processing
     */
    public SeSelection(MultiSeRequestProcessing multiSeRequestProcessing,
            ChannelControl channelControl) {
        this.multiSeRequestProcessing = multiSeRequestProcessing;
        this.channelControl = channelControl;
    }

    /**
     * Alternate constructor for standard usages.
     */
    public SeSelection() {
        this(MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.KEEP_OPEN);
    }

    /**
     * Prepare a selection: add the selection request from the provided selector to the selection
     * request set.
     * <p>
     *
     * @param seSelectionRequest the selector to prepare
     * @return the selection index giving the current selection position in the selection request.
     */
    public int prepareSelection(AbstractSeSelectionRequest seSelectionRequest) {
        if (logger.isTraceEnabled()) {
            logger.trace("SELECTORREQUEST = {}", seSelectionRequest.getSelectionRequest());
        }
        /* keep the selection request */
        seSelectionRequests.add(seSelectionRequest);
        /* return the selection index */
        return seSelectionRequests.size();
    }

    /**
     * Process the selection response either from a
     * {@link org.eclipse.keyple.core.seproxy.event.ReaderEvent} (default selection) or from an
     * explicit selection.
     * <p>
     * The responses from the List of {@link SeResponse} is parsed and checked.
     * <p>
     * A {@link AbstractMatchingSe} list is build and returned. Non matching SE are signaled by a
     * null element in the list
     * 
     * @param defaultSelectionsResponse the selection response
     * @return the {@link SelectionsResult} containing the result of all prepared selection cases,
     *         including {@link AbstractMatchingSe} and {@link SeResponse}.
     */
    private SelectionsResult processSelection(
            AbstractDefaultSelectionsResponse defaultSelectionsResponse) throws KeypleException {
        SelectionsResult selectionsResult = new SelectionsResult();

        int index = 0;

        /* Check SeResponses */
        for (SeResponse seResponse : ((DefaultSelectionsResponse) defaultSelectionsResponse)
                .getSelectionSeResponseSet()) {
            /* test if the selection is successful: we should have either a FCI or an ATR */
            if (seResponse != null && seResponse.getSelectionStatus() != null
                    && seResponse.getSelectionStatus().hasMatched()) {
                /*
                 * create a AbstractMatchingSe with the class deduced from the selection request
                 * during the selection preparation
                 */
                AbstractMatchingSe matchingSe = seSelectionRequests.get(index).parse(seResponse);

                selectionsResult.addMatchingSelection(new MatchingSelection(index,
                        seSelectionRequests.get(index), matchingSe, seResponse));
            }
            index++;
        }
        return selectionsResult;
    }

    /**
     * Parses the response to a selection operation sent to a SE and return a list of
     * {@link AbstractMatchingSe}
     * <p>
     * Selection cases that have not matched the current SE are set to null.
     *
     * @param defaultSelectionsResponse the response from the reader to the
     *        {@link AbstractDefaultSelectionsRequest}
     * @return the {@link SelectionsResult} containing the result of all prepared selection cases,
     *         including {@link AbstractMatchingSe} and {@link SeResponse}.
     * @throws KeypleException if an error occurs during the selection process
     */
    public SelectionsResult processDefaultSelection(
            AbstractDefaultSelectionsResponse defaultSelectionsResponse) throws KeypleException {

        /* null pointer exception protection */
        if (defaultSelectionsResponse == null) {
            logger.error("defaultSelectionsResponse shouldn't be null in processSelection.");
            return null;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Process default SELECTIONRESPONSE ({} response(s))",
                    ((DefaultSelectionsResponse) defaultSelectionsResponse)
                            .getSelectionSeResponseSet().size());
        }

        return processSelection(defaultSelectionsResponse);
    }

    /**
     * Execute the selection process and return a list of {@link AbstractMatchingSe}.
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
     *         including {@link AbstractMatchingSe} and {@link SeResponse}.
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    public SelectionsResult processExplicitSelection(SeReader seReader) throws KeypleException {
        Set<SeRequest> selectionRequestSet = new LinkedHashSet<SeRequest>();
        for (AbstractSeSelectionRequest seSelectionRequest : seSelectionRequests) {
            selectionRequestSet.add(seSelectionRequest.getSelectionRequest());
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Transmit SELECTIONREQUEST ({} request(s))", selectionRequestSet.size());
        }

        /* Communicate with the SE to do the selection */
        List<SeResponse> seResponseList = ((ProxyReader) seReader).transmitSet(selectionRequestSet,
                multiSeRequestProcessing, channelControl);

        return processSelection(new DefaultSelectionsResponse(seResponseList));
    }

    /**
     * The SelectionOperation is the {@link AbstractDefaultSelectionsRequest} to process in ordered
     * to select a SE among others through the selection process. This method is useful to build the
     * prepared selection to be executed by a reader just after a SE insertion.
     * 
     * @return the {@link AbstractDefaultSelectionsRequest} previously prepared with
     *         prepareSelection
     */
    public AbstractDefaultSelectionsRequest getSelectionOperation() {
        Set<SeRequest> selectionRequestSet = new LinkedHashSet<SeRequest>();
        for (AbstractSeSelectionRequest seSelectionRequest : seSelectionRequests) {
            selectionRequestSet.add(seSelectionRequest.getSelectionRequest());
        }
        return new DefaultSelectionsRequest(selectionRequestSet, multiSeRequestProcessing,
                channelControl);
    }
}
