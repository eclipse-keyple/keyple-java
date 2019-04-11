/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
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

import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.message.SeResponse;

/**
 * The MatchingSelection class holds the result of a single selection case.
 */
public class MatchingSelection {
    private final MatchingSe matchingSe;
    private final SeSelectionRequest seSelectionRequest;
    private final SeResponse selectionSeResponse;
    private final int selectionIndex;

    /**
     * Constructor
     *
     * @param selectionIndex
     * @param seSelectionRequest
     * @param matchingSe
     * @param selectionSeResponse
     */
    public MatchingSelection(int selectionIndex, SeSelectionRequest seSelectionRequest,
            MatchingSe matchingSe, SeResponse selectionSeResponse) {
        this.selectionIndex = selectionIndex;
        this.seSelectionRequest = seSelectionRequest;
        this.matchingSe = matchingSe;
        this.selectionSeResponse = selectionSeResponse;
    }

    /**
     * @return the MatchingSe
     */
    public MatchingSe getMatchingSe() {
        return matchingSe;
    }

    /**
     * Get the parser for the targeted response.
     * 
     * @param commandIndex
     * @return a parser object
     */
    public AbstractApduResponseParser getResponseParser(int commandIndex) {
        return seSelectionRequest.getCommandParser(selectionSeResponse, commandIndex);
    }

    /**
     * @return the info string provided with the Selector
     */
    public String getExtraInfo() {
        return seSelectionRequest.getSeSelector().getExtraInfo();
    }

    /**
     * @return the index of the selection (order in the prepareSelection command). 0 is the first
     *         selection.
     */
    public int getSelectionIndex() {
        return selectionIndex;
    }
}
