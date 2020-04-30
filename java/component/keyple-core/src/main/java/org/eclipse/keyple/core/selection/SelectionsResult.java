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
    private boolean hasActiveSelection = false;
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
     */
    void addMatchingSe(int selectionIndex, AbstractMatchingSe matchingSe) {
        if (matchingSe != null)
            matchingSeMap.put(selectionIndex, matchingSe);
        /* test if the current selection is active */
        if (matchingSe.isSelected()) {
            hasActiveSelection = true;
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
        for (Map.Entry<Integer, AbstractMatchingSe> entry : matchingSeMap.entrySet()) {
            if (entry.getValue().isSelected()) {
                return entry.getValue();
            }
        }
        throw new IllegalStateException("No active Matching SE is available");
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
        return hasActiveSelection;
    }
}
