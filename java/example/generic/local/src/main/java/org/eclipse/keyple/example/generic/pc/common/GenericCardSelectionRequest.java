/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.example.generic.pc.common;

import org.eclipse.keyple.core.card.message.CardSelectionResponse;
import org.eclipse.keyple.core.card.selection.AbstractCardSelectionRequest;
import org.eclipse.keyple.core.card.selection.AbstractSmartCard;
import org.eclipse.keyple.core.card.selection.CardSelector;

/** Create a new class extending AbstractCardSelectionRequest */
public class GenericCardSelectionRequest extends AbstractCardSelectionRequest {
  public GenericCardSelectionRequest(CardSelector cardSelector) {
    super(cardSelector);
  }

  @Override
  protected AbstractSmartCard parse(CardSelectionResponse cardSelectionResponse) {
    class GenericSmartCard extends AbstractSmartCard {
      public GenericSmartCard(CardSelectionResponse cardSelectionResponse) {
        super(cardSelectionResponse);
      }
    }
    return new GenericSmartCard(cardSelectionResponse);
  }
}
