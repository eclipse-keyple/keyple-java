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

import java.util.HashMap;
import java.util.Map;

/**
 * The SelectionsResult class holds the result of a selection process.
 * <p>
 * Embeds a map of {@link AbstractMatchingSe}. At most one of these matching SE is active.<br>
 * Provides a set of methods to retrieve the active selection (getActiveMatchingSe) or a particular
 * matching SE specified by its index.
 */
public final class SelectionsResult {
    private Integer activeSelectionIndex = null;
    private final Map<Integer, AbstractMatchingSe> matchingSeMap =
            new HashMap<Integer, AbstractMatchingSe>();

    /**
     * Constructor
     */
    SelectionsResult() {}

    /**
     * Append a {@link AbstractMatchingSe} to the internal list
     *
     * @param selectionIndex the index of the selection that resulted in the matching SE
     * @param matchingSe the matching SE to add
     * @param isSelected true if the currently added matching SE is selected (its logical channel is
     *        open)
     */
    void addMatchingSe(int selectionIndex, AbstractMatchingSe matchingSe, boolean isSelected) {
        if (matchingSe != null)
            matchingSeMap.put(selectionIndex, matchingSe);
        // if the current selection is active, we keep its index
        if (isSelected) {
            activeSelectionIndex = selectionIndex;
        }
    }

    /**
     * Get the active matching SE. I.e. the SE that has been selected. <br>
     * The hasActiveSelection method should be called before.
     * 
     * @return the currently active matching SE
     * @throws IllegalStateException if no active matching SE is found
     */
    public AbstractMatchingSe getActiveMatchingSe() {
        AbstractMatchingSe matchingSe = matchingSeMap.get(activeSelectionIndex);
        if (matchingSe == null) {
            throw new IllegalStateException("No active Matching SE is available");
        }
        return matchingSe;
    }

    /**
     * @return the {@link AbstractMatchingSe} map
     */
    public Map<Integer, AbstractMatchingSe> getMatchingSelections() {
        return matchingSeMap;
    }

    /**
     * Gets the {@link AbstractMatchingSe} for the specified index.
     * <p>
     * Returns null if no {@link AbstractMatchingSe} was found.
     * 
     * @param selectionIndex the selection index
     * @return the {@link AbstractMatchingSe} or null
     */
    public AbstractMatchingSe getMatchingSe(int selectionIndex) {
        return matchingSeMap.get(selectionIndex);
    }

    /**
     * @return true if an active selection is present
     */
    public boolean hasActiveSelection() {
        return activeSelectionIndex != null;
    }

    /**
     * Get the matching status of a selection for which the index is provided. <br>
     * Checks for the presence of an entry in the MatchingSe Map for the given index
     *
     * @param selectionIndex the selection index
     * @return true if the selection has matched
     */
    boolean hasSelectionMatched(int selectionIndex) {
        return matchingSeMap.containsKey(selectionIndex);
    }

    /**
     * @return the index of the active selection
     */
    int getActiveSelectionIndex() {
        if (hasActiveSelection()) {
            return activeSelectionIndex;
        }
        throw new IllegalStateException("No active Matching SE is available");
    }
}
