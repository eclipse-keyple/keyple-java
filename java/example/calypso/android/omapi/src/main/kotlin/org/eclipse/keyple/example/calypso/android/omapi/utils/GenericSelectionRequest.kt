package org.eclipse.keyple.example.calypso.android.omapi.utils

import org.eclipse.keyple.core.selection.AbstractMatchingSe
import org.eclipse.keyple.core.selection.AbstractSeSelectionRequest
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.message.SeResponse
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode


class GenericSeSelectionRequest(seSelector: SeSelector) : AbstractSeSelectionRequest(seSelector) {
    private var transmissionMode: TransmissionMode = seSelector.seProtocol.transmissionMode

    override fun parse(seResponse: SeResponse): AbstractMatchingSe {
        class GenericMatchingSe(selectionResponse: SeResponse,
                                transmissionMode: TransmissionMode, extraInfo: String) : AbstractMatchingSe(selectionResponse, transmissionMode, extraInfo)
        return GenericMatchingSe(seResponse, transmissionMode, "Generic Matching SE")
    }
}