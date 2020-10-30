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
package org.eclipse.keyple.core.service.event;

import java.util.List;
import org.eclipse.keyple.core.card.message.CardResponse;

/**
 * This abstract class defines the POJO used to carry the response to a default selection request.
 *
 * <p>The response to a default selection is obtained from an observable reader and analyzed by the
 * selection class that created the selection request.
 *
 * @since 0.9
 */
public abstract class AbstractDefaultSelectionsResponse {

  /**
   * Gets the result of the selection as a list of responses corresponding to the requests made in
   * {@link AbstractDefaultSelectionsRequest}.
   *
   * @return A not empty list.
   * @since 0.9
   */
  protected abstract List<CardResponse> getSelectionCardResponses();
}
