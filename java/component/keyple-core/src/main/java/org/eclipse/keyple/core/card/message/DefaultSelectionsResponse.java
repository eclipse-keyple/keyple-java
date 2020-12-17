/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.card.message;

import java.util.List;
import org.eclipse.keyple.core.service.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.service.event.ReaderEvent;

/**
 * This POJO wraps the default selections responses in return to the default selections made when a
 * card is inserted.
 *
 * <p>It provides a list of {@link CardSelectionResponse} as the result of the selection process.
 *
 * <p>Note: this class extends the {@link AbstractDefaultSelectionsResponse} class which is the one
 * handled at the application level.
 *
 * @since 0.9
 */
public final class DefaultSelectionsResponse extends AbstractDefaultSelectionsResponse {

  private final List<CardSelectionResponse> cardSelectionResponses;

  /**
   * Builds a DefaultSelectionsResponse from the list of {@link CardSelectionResponse} received from
   * the reader during the selection process. It transports the selection results into the {@link
   * ReaderEvent} when the reader is observed and also during explicit selections in the case of an
   * unobserved reader.
   *
   * <p>The list of {@link CardSelectionResponse} corresponds to the list of {@link
   * CardSelectionRequest} present in the {@link DefaultSelectionsRequest}. The first {@link
   * CardSelectionResponse} corresponds to the first {@link CardRequest} and so on.
   *
   * <p>When a {@link CardRequest} has not resulted in a response, then the corresponding {@link
   * CardSelectionResponse} in the list is null.
   *
   * <p>Depending on the setting of the selection, the process either processes all selection cases
   * provided in the {@link DefaultSelectionsRequest} or stops at the first selection case that
   * results in a response. In the latter case, the {@link CardSelectionResponse} list may be
   * shorter than the {@link CardRequest} list.
   *
   * @param cardSelectionResponses A list of {@link CardSelectionResponse} (should not be null).
   * @since 0.9
   */
  public DefaultSelectionsResponse(List<CardSelectionResponse> cardSelectionResponses) {
    this.cardSelectionResponses = cardSelectionResponses;
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.9
   */
  public final List<CardSelectionResponse> getCardSelectionResponses() {
    return cardSelectionResponses;
  }
}
