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
package org.eclipse.keyple.core.seproxy.event;

import java.util.List;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;

/**
 * This POJO defines the default selections responses in return to the default selections made when
 * the SE was inserted.
 *
 * <p>Its main feature is to provide a list of {@link SeResponse} following a selection process.
 *
 * <p>The purpose of this abstract class is to hide the constructor that is defined as public in its
 * implementation {@link DefaultSelectionsResponse}.
 *
 * @since 0.9
 */
public abstract class AbstractDefaultSelectionsResponse {

  private final List<SeResponse> selectionSeResponses;

  /**
   * Constructor<br>
   * This object is constructed from the list of {@link SeResponse} received from the reader during
   * the selection process. It transports the selection results into the {@link ReaderEvent} when
   * the reader is observed and also during explicit selections in the case of an unobserved reader.
   *
   * <p>The list of {@link SeResponse} corresponds to the list of {@link SeRequest} present in the
   * {@link DefaultSelectionsRequest}. The first {@link SeResponse} corresponds to the first {@link
   * SeRequest} and so on.
   *
   * <p>When a {@link SeRequest} has not resulted in a response, then the corresponding {@link
   * SeResponse} in the list is null.
   *
   * <p>Depending on the setting of the selection, the process either processes all selection cases
   * provided in the {@link DefaultSelectionsRequest} or stops at the first selection case that
   * results in a response. In the latter case, the {@link SeResponse} list may be shorter than the
   * {@link SeRequest} list.
   *
   * @param selectionSeResponses A list of {@link SeResponse} (should not be null).
   * @since 0.9
   */
  protected AbstractDefaultSelectionsResponse(List<SeResponse> selectionSeResponses) {
    this.selectionSeResponses = selectionSeResponses;
  }

  /**
   * Gets the result of the selection as a list of responses corresponding to the requests made in
   * {@link AbstractDefaultSelectionsRequest}.
   *
   * @return A not empty list.
   * @since 0.9
   */
  public final List<SeResponse> getSelectionSeResponses() {
    return selectionSeResponses;
  }
}
