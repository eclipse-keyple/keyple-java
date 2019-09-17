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

import java.util.Set;
import org.eclipse.keyple.core.seproxy.message.SeRequest;

/**
 * The {@link AbstractDefaultSelectionsRequest} class is dedicated to
 */
public abstract class AbstractDefaultSelectionsRequest {
    /** The Set of {@link SeRequest} */
    protected final Set<SeRequest> selectionSeRequestSet;

    protected AbstractDefaultSelectionsRequest(Set<SeRequest> selectionSeRequestSet) {
        this.selectionSeRequestSet = selectionSeRequestSet;
    }

    protected abstract Set<SeRequest> getSelectionSeRequestSet();
}
