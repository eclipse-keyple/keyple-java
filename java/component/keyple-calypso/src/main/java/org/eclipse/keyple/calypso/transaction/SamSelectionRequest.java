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
package org.eclipse.keyple.calypso.transaction;

import org.eclipse.keyple.calypso.transaction.sam.CalypsoSam;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.transaction.SeSelectionRequest;

public class SamSelectionRequest extends SeSelectionRequest {
    public SamSelectionRequest(SeSelector seSelector, ChannelState channelState,
            SeProtocol protocolFlag) {
        super(seSelector, channelState, protocolFlag);
    }

    /**
     * Create a CalypsoSam object containing the selection data received from the plugin
     *
     * @param seResponse the SE response received
     * @return a {@link CalypsoSam}
     */
    @Override
    protected CalypsoSam parse(SeResponse seResponse) {
        return new CalypsoSam(seResponse, seSelector.getExtraInfo());
    }
}
