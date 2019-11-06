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
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.message.SeRequest;

/**
 * The {@link AbstractDefaultSelectionsRequest} class is dedicated to
 */
public abstract class AbstractDefaultSelectionsRequest {
    /** The Set of {@link SeRequest} */
    protected final Set<SeRequest> selectionSeRequestSet;

    private final MultiSeRequestProcessing multiSeRequestProcessing;

    private final ChannelControl channelControl;

    protected AbstractDefaultSelectionsRequest(Set<SeRequest> selectionSeRequestSet,
            MultiSeRequestProcessing multiSeRequestProcessing, ChannelControl channelControl) {
        this.selectionSeRequestSet = selectionSeRequestSet;
        this.multiSeRequestProcessing = multiSeRequestProcessing;
        this.channelControl = channelControl;
    }

    protected abstract Set<SeRequest> getSelectionSeRequestSet();

    public final MultiSeRequestProcessing getMultiSeRequestProcessing() {
        return multiSeRequestProcessing;
    }

    public ChannelControl getChannelControl() {
        return channelControl;
    }
}
