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
package org.eclipse.keyple.example.common.generic;

import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.selection.AbstractSeSelectionRequest;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.message.CardResponse;

/** Create a new class extending AbstractSeSelectionRequest */
public class GenericSeSelectionRequest extends AbstractSeSelectionRequest {
  public GenericSeSelectionRequest(SeSelector seSelector) {
    super(seSelector);
  }

  @Override
  protected AbstractMatchingSe parse(CardResponse cardResponse) {
    class GenericMatchingSe extends AbstractMatchingSe {
      public GenericMatchingSe(CardResponse selectionResponse) {
        super(selectionResponse);
      }
    }
    return new GenericMatchingSe(cardResponse);
  }
}
