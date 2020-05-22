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
package org.eclipse.keyple.core.seproxy.event;


import java.util.List;
import org.eclipse.keyple.core.seproxy.message.SeResponse;

/**
 * The abstract class defining the default selections response in return to the default selection
 * made when the SE was inserted..
 * <p>
 * The default selections response provides a list of {@link SeResponse}<br>
 * The purpose of this abstract class is to hide the constructor that is defined as public in its
 * implementation {@link org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest}.
 */
public abstract class AbstractDefaultSelectionsResponse {
    private final List<SeResponse> selectionSeResponses;

    protected AbstractDefaultSelectionsResponse(List<SeResponse> selectionSeResponses) {
        this.selectionSeResponses = selectionSeResponses;
    }

    /**
     * @return the list of {@link SeResponse}
     */
    public final List<SeResponse> getSelectionSeResponses() {
        return selectionSeResponses;
    }
}
