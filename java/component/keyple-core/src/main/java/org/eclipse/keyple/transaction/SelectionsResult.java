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

import java.util.ArrayList;
import java.util.List;

/**
 * The SelectionsResult class holds the result of a selection process.
 * <p>
 * embeds a list of {@link MatchingSelection}
 * <p>
 * provides a set of methods to retrieve the active selection (getActiveSelection) or a particular
 * selection specified by its index.
 */
public class SelectionsResult {
    private boolean hasActiveSelection = false;
    private List<MatchingSelection> matchingSelectionList = new ArrayList<MatchingSelection>();

    /**
     * Append a {@link MatchingSelection} to the internal list
     * 
     * @param matchingSelection the item to add
     */
    public void addMatchingSelection(MatchingSelection matchingSelection) {
        matchingSelectionList.add(matchingSelection);
        /* test if the current selection is active */
        if (matchingSelection.getMatchingSe().isSelected()) {
            hasActiveSelection = true;
        }
    }

    /**
     * @return the currently active (matching) selection
     */
    public MatchingSelection getActiveSelection() {
        MatchingSelection activeSelection = null;
        for (MatchingSelection matchingSelection : matchingSelectionList) {
            if (matchingSelection != null && matchingSelection.getMatchingSe().isSelected()) {
                activeSelection = matchingSelection;
                break;
            }
        }
        return activeSelection;
    }

    /**
     * @return the {@link MatchingSelection} list
     */
    public List<MatchingSelection> getMatchingSelections() {
        return matchingSelectionList;
    }

    /**
     * Gets the {@link MatchingSelection} for the specified index.
     * <p>
     * Returns null if no {@link MatchingSelection} was found.
     * 
     * @param selectionIndex the selection index
     * @return the {@link MatchingSelection} or null
     */
    public MatchingSelection getMatchingSelection(int selectionIndex) {
        for (MatchingSelection matchingSelection : matchingSelectionList) {
            if (matchingSelection.getSelectionIndex() == selectionIndex) {
                return matchingSelection;
            }
        }
        return null;
    }

    /**
     * @return true if an active selection is present
     */
    public boolean hasActiveSelection() {
        return hasActiveSelection;
    }
}
