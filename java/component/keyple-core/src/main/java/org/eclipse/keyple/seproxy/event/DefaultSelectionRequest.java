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
package org.eclipse.keyple.seproxy.event;

import org.eclipse.keyple.seproxy.message.SeRequestSet;

/**
 * Class containing the {@link SeRequestSet} used to make a default selection at the
 * {@link org.eclipse.keyple.seproxy.event.ObservableReader} level.
 */
public class DefaultSelectionRequest {
    /** The {@link SeRequestSet} */
    private final SeRequestSet selectionSeRequestSet;

    public DefaultSelectionRequest(SeRequestSet selectionSeRequestSet) {
        this.selectionSeRequestSet = selectionSeRequestSet;
    }

    public SeRequestSet getSelectionSeRequestSet() {
        return selectionSeRequestSet;
    }
}
