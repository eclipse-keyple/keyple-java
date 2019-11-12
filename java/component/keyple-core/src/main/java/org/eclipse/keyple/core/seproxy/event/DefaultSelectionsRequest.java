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
 * The interface defining the default selection request to be processed when an SE is inserted in an
 * observable reader.
 * <p>
 * The default selection is defined by:
 * <ul>
 * <li>a set of requests corresponding to one or more selection cases
 * <li>a {@link MultiSeRequestProcessing} indicator specifying whether all planned selections are to
 * be executed or whether to stop at the first one that is successful
 * <li>an indicator to control the physical channel to stipulate whether it should be closed or left
 * open at the end of the selection process
 * </ul>
 */

public interface DefaultSelectionsRequest {
    /**
     * @return the selection request set
     */
    Set<SeRequest> getSelectionSeRequestSet();

    /**
     * @return the multi SE request mode
     */
    MultiSeRequestProcessing getMultiSeRequestProcessing();

    /**
     * @return the channel control
     */
    ChannelControl getChannelControl();
}
