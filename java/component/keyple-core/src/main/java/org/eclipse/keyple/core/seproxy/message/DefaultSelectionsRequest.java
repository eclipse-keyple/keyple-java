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
package org.eclipse.keyple.core.seproxy.message;

import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;

/**
 * Class containing the {@link org.eclipse.keyple.core.seproxy.message.SeRequestSet} used to make a
 * default selection at the {@link ObservableReader} level.
 */
public class DefaultSelectionsRequest extends AbstractDefaultSelectionsRequest {
    /** The {@link org.eclipse.keyple.core.seproxy.message.SeRequestSet} */
    private final SeRequestSet selectionSeRequestSet;

    public DefaultSelectionsRequest(SeRequestSet selectionSeRequestSet) {
        this.selectionSeRequestSet = selectionSeRequestSet;
    }

    @Override
    public SeRequestSet getSelectionSeRequestSet() {
        return selectionSeRequestSet;
    }
}
