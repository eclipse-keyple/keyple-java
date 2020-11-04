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

import org.eclipse.keyple.core.card.command.AbstractApduCommandBuilder
import org.eclipse.keyple.core.card.message.CardSelectionResponse
import org.eclipse.keyple.core.card.selection.AbstractCardSelectionRequest
import org.eclipse.keyple.core.card.selection.AbstractSmartCard
import org.eclipse.keyple.core.card.selection.CardSelector

class GenericCardSelectionRequest(cardSelector: CardSelector) : AbstractCardSelectionRequest<AbstractApduCommandBuilder>(cardSelector) {
    override fun parse(CardSelectionResponse: CardSelectionResponse): AbstractSmartCard {
        class GenericSmartCard(
            CardSelectionResponse: CardSelectionResponse
        ) : AbstractSmartCard(CardSelectionResponse)
        return GenericSmartCard(CardSelectionResponse)
    }
}
