/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.calypso.android.omapi.utils

import org.eclipse.keyple.core.command.AbstractApduCommandBuilder
import org.eclipse.keyple.core.selection.AbstractMatchingSe
import org.eclipse.keyple.core.selection.AbstractSeSelectionRequest
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.message.SeResponse
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode

class GenericSeSelectionRequest(seSelector: SeSelector) : AbstractSeSelectionRequest<AbstractApduCommandBuilder>(seSelector) {
    private var transmissionMode: TransmissionMode = seSelector.seProtocol.transmissionMode

    override fun parse(seResponse: SeResponse): AbstractMatchingSe {
        class GenericMatchingSe(
            selectionResponse: SeResponse,
            transmissionMode: TransmissionMode
        ) : AbstractMatchingSe(selectionResponse, transmissionMode)
        return GenericMatchingSe(seResponse, transmissionMode)
    }
}
