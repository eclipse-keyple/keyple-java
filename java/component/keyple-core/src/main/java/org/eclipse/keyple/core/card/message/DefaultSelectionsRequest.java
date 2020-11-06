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
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.service.event.AbstractDefaultSelectionsRequest;

/**
 * This POJO defines the default selection request to be processed when a card is inserted in an
 * observable reader.
 *
 * <p>The default selection is defined by:
 *
 * <ul>
 *   <li>A list of {@link CardSelectionRequest} corresponding to one or more selection cases.
 *   <li>A {@link MultiSelectionProcessing} indicator specifying whether all planned selections are
 *       to be executed or whether to stop at the first one that is successful.
 *   <li>A {@link ChannelControl} indicator controlling the physical channel to stipulate whether it
 *       should be closed or left open at the end of the selection process.
 * </ul>
 *
 * Note: this class extends the {@link AbstractDefaultSelectionsRequest} class which is the one
 * handled at the application level.
 *
 * @since 0.9
 */
public final class DefaultSelectionsRequest extends AbstractDefaultSelectionsRequest {

  private final List<CardSelectionRequest> cardSelectionRequests;
  private final MultiSelectionProcessing multiSelectionProcessing;
  private final ChannelControl channelControl;

  /**
   * Builds a DefaultSelectionsRequest from a list of selection cases and two enum constants guiding
   * the expected behaviour of the selection process.
   *
   * <p>The {@link MultiSelectionProcessing} enum is used to attempt to execute all the selection
   * cases: {@link MultiSelectionProcessing#PROCESS_ALL} (for example in order to list all the
   * applications present in a card) or {@link MultiSelectionProcessing#FIRST_MATCH} (to target a
   * single application).
   *
   * <p>The {@link ChannelControl} enum controls the closing of the physical channel at the end of
   * the selection.
   *
   * <p>Note: the {@link CardSelectionRequest} list should be carefully ordered in accordance with
   * the cards expected in the application to optimize the processing time of the selection process.
   * The first selection case in the list will be processed first.
   *
   * @param cardSelectionRequests A list of {@link CardSelectionRequest} embedding the selection
   *     data (must be not empty).
   * @param multiSelectionProcessing The multi request processing mode (must be not null).
   * @param channelControl The channel control (must be not null).
   * @since 0.9
   */
  public DefaultSelectionsRequest(
      List<CardSelectionRequest> cardSelectionRequests,
      MultiSelectionProcessing multiSelectionProcessing,
      ChannelControl channelControl) {

    this.cardSelectionRequests = cardSelectionRequests;
    this.multiSelectionProcessing = multiSelectionProcessing;
    this.channelControl = channelControl;
  }

  /** {@inheritDoc} */
  public final MultiSelectionProcessing getMultiSelectionProcessing() {
    return multiSelectionProcessing;
  }

  /** {@inheritDoc} */
  public final ChannelControl getChannelControl() {
    return channelControl;
  }

  /** {@inheritDoc} */
  public final List<CardSelectionRequest> getCardSelectionRequests() {
    return cardSelectionRequests;
  }
}
