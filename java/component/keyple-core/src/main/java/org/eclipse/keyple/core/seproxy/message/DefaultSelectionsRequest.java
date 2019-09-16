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

import java.util.Set;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;

/**
 * Class containing the Set of {@link SeRequest} used to make a default selection at the
 * {@link ObservableReader} level.
 */
public final class DefaultSelectionsRequest extends AbstractDefaultSelectionsRequest {

    public DefaultSelectionsRequest(Set<SeRequest> selectionSeRequestSet,
            MultiSeRequestProcessing multiSeRequestProcessing, ChannelState channelState) {
        super(selectionSeRequestSet, multiSeRequestProcessing, channelState);
    }

    public DefaultSelectionsRequest(Set<SeRequest> selectionSeRequestSet) {
        this(selectionSeRequestSet, MultiSeRequestProcessing.FIRST_MATCH, ChannelState.KEEP_OPEN);
    }

    @Override
    public Set<SeRequest> getSelectionSeRequestSet() {
        return selectionSeRequestSet;
    }
}
