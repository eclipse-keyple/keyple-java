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

import org.eclipse.keyple.calypso.command.sam.AbstractSamResponseParser;
import org.eclipse.keyple.core.selection.AbstractSeSelectionRequest;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.message.SeResponse;

/**
 * Specialized selection request to manage the specific characteristics of Calypso SAMs
 */
public class SamSelectionRequest extends AbstractSeSelectionRequest {
    /**
     * Create a {@link SamSelectionRequest}
     * 
     * @param samSelector the SAM selector
     * @param channelState the action to take after (close or keep open)
     */
    public SamSelectionRequest(SamSelector samSelector, ChannelState channelState) {
        super(samSelector, channelState);
    }

    /**
     * Create a CalypsoSam object containing the selection data received from the plugin
     *
     * @param seResponse the SE response received
     * @return a {@link CalypsoSam}
     */
    @Override
    protected CalypsoSam parse(SeResponse seResponse) {
        return new CalypsoSam(seResponse, seSelector.getSeProtocol().getTransmissionMode(),
                seSelector.getExtraInfo());
    }

    @Override
    public AbstractSamResponseParser getCommandParser(SeResponse seResponse, int commandIndex) {
        /* not yet implemented in keyple-calypso */
        // TODO add a generic command parser
        throw new IllegalStateException("No parsers available for this request.");
    }
}
