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

import org.eclipse.keyple.core.seproxy.message.SeRequestSet;

public abstract class AbstractDefaultSelectionsRequest {
    /** The {@link org.eclipse.keyple.core.seproxy.message.SeRequestSet} */
    protected final SeRequestSet selectionSeRequestSet;

    protected AbstractDefaultSelectionsRequest(SeRequestSet selectionSeRequestSet) {
        this.selectionSeRequestSet = selectionSeRequestSet;
    }

    public abstract SeRequestSet getSelectionSeRequestSet();
}
