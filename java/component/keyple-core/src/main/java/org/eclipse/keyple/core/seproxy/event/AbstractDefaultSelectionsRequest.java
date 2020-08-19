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
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.message.SeRequest;

/**
 * The abstract class defining the default selection request to be processed when an SE is inserted
 * in an observable reader.
 * <p>
 * The default selection is defined by:
 * <ul>
 * <li>a set of requests corresponding to one or more selection cases
 * <li>a {@link MultiSeRequestProcessing} indicator specifying whether all planned selections are to
 * be executed or whether to stop at the first one that is successful
 * <li>an indicator to control the physical channel to stipulate whether it should be closed or left
 * open at the end of the selection process
 * </ul>
 * The purpose of this abstract class is to hide the constructor that is defined as public in its
 * implementation {@link org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest}.
 */

public abstract class AbstractDefaultSelectionsRequest {
    private final List<SeRequest> selectionSeRequests;
    private final MultiSeRequestProcessing multiSeRequestProcessing;
    private final ChannelControl channelControl;

    protected AbstractDefaultSelectionsRequest(List<SeRequest> selectionSeRequests,
            MultiSeRequestProcessing multiSeRequestProcessing, ChannelControl channelControl) {
        this.selectionSeRequests = selectionSeRequests;
        this.multiSeRequestProcessing = multiSeRequestProcessing;
        this.channelControl = channelControl;
    }

    /**
     * @return the flag indicating whether the selection process should stop after the first
     *         matching or process all
     */
    public final MultiSeRequestProcessing getMultiSeRequestProcessing() {
        return multiSeRequestProcessing;
    }

    /**
     * @return the flag indicating whether the logic channel is to be kept open or closed
     */
    public final ChannelControl getChannelControl() {
        return channelControl;
    }

    /**
     * @return the list of requests that make up the selection
     */
    public final List<SeRequest> getSelectionSeRequests() {
        return selectionSeRequests;
    }
}
