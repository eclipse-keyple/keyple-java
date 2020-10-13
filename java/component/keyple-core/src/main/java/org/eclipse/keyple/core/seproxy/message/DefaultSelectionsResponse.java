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
package org.eclipse.keyple.core.seproxy.message;

import java.util.List;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;

/**
 * This abstract class defines a POJO carrying the default selections responses in return to the
 * default selections made when the card was inserted.
 *
 * <p>Its main feature is to provide a list of {@link SeResponse} following a selection process.
 *
 * <p>Note: this class extends the {@link AbstractDefaultSelectionsResponse} class which is the one
 * handled at the application level.
 *
 * @since 0.9
 */
public final class DefaultSelectionsResponse extends AbstractDefaultSelectionsResponse {

  private final List<SeResponse> selectionSeResponses;

  /**
   * Constructor<br>
   * This object is constructed from the list of {@link SeResponse} received from the reader during
   * the selection process. It transports the selection results into the {@link ReaderEvent} when
   * the reader is observed and also during explicit selections in the case of an unobserved reader.
   *
   * <p>The list of {@link SeResponse} corresponds to the list of {@link CardRequest} present in the
   * {@link DefaultSelectionsRequest}. The first {@link SeResponse} corresponds to the first {@link
   * CardRequest} and so on.
   *
   * <p>When a {@link CardRequest} has not resulted in a response, then the corresponding {@link
   * SeResponse} in the list is null.
   *
   * <p>Depending on the setting of the selection, the process either processes all selection cases
   * provided in the {@link DefaultSelectionsRequest} or stops at the first selection case that
   * results in a response. In the latter case, the {@link SeResponse} list may be shorter than the
   * {@link CardRequest} list.
   *
   * @param selectionSeResponses A list of {@link SeResponse} (should not be null).
   * @since 0.9
   */
  public DefaultSelectionsResponse(List<SeResponse> selectionSeResponses) {
    this.selectionSeResponses = selectionSeResponses;
  }

  /** {@inheritDoc} */
  public final List<SeResponse> getSelectionSeResponses() {
    return selectionSeResponses;
  }
}
