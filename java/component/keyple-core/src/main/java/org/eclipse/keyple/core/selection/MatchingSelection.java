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
package org.eclipse.keyple.core.selection;

import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.seproxy.message.SeResponse;

/**
 * The MatchingSelection class holds the result of a single selection case.
 */
public class MatchingSelection {
    private final AbstractMatchingSe matchingSe;
    private final AbstractSeSelectionRequest seSelectionRequest;
    private final SeResponse selectionSeResponse;
    private final int selectionIndex;

    /**
     * Constructor
     *
     * @param selectionIndex the selection index
     * @param seSelectionRequest the selection request
     * @param matchingSe the matching SE
     * @param selectionSeResponse the selection SeResponse
     */
    MatchingSelection(int selectionIndex, AbstractSeSelectionRequest seSelectionRequest,
            AbstractMatchingSe matchingSe, SeResponse selectionSeResponse) {
        this.selectionIndex = selectionIndex;
        this.seSelectionRequest = seSelectionRequest;
        this.matchingSe = matchingSe;
        this.selectionSeResponse = selectionSeResponse;
    }

    /**
     * @return the AbstractMatchingSe
     */
    public AbstractMatchingSe getMatchingSe() {
        return matchingSe;
    }

    /**
     * Get the parser for the targeted response.
     * 
     * @param commandIndex the command index
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
