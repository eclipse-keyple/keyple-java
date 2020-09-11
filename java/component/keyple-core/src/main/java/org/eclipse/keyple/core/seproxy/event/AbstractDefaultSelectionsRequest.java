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
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.message.SeRequest;

/**
 * This abstract class defines the default selection request to be processed when an SE is inserted
 * in an observable reader.
 *
 * <p>The default selection is defined by:
 *
 * <ul>
 *   <li>a list of {@link SeRequest} corresponding to one or more selection cases
 *   <li>a {@link MultiSeRequestProcessing} indicator specifying whether all planned selections are
 *       to be executed or whether to stop at the first one that is successful
 *   <li>a {@link ChannelControl} indicator controlling the physical channel to stipulate whether it
 *       should be closed or left open at the end of the selection process
 * </ul>
 *
 * <p>The purpose of this abstract class is to hide the constructor that is defined as public in its
 * implementation {@link DefaultSelectionsRequest}.
 */
public abstract class AbstractDefaultSelectionsRequest {
  private final List<SeRequest> selectionSeRequests;
  private final MultiSeRequestProcessing multiSeRequestProcessing;
  private final ChannelControl channelControl;

  /**
   * Constructor<br>
   * This object is constructed from a list of selection cases and two enum constants guiding the
   * expected behaviour of the selection process.
   *
   * <p>The {@link MultiSeRequestProcessing} enum is used to attempt to execute all the selection
   * cases: PROCESS_ALL (for example in order to list all the applications present in a secure
   * element) or FIRST_MATCH (to target a single application).
   *
   * <p>The {@link ChannelControl} enum controls the closing of the physical channel at the end of
   * the selection.
   *
   * <p>Note: the {@link SeRequest} list should be carefully ordered in accordance with the SEs
   * expected in the application to optimize the processing time of the selection process. The first
   * selection case in the list will be processed first.
   *
   * @param selectionSeRequests a list of {@link SeRequest} embedding the selection data (should not
   *     be null)
   * @param multiSeRequestProcessing an enum constant of type {@link MultiSeRequestProcessing}
   * @param channelControl an enum constant of type {@link ChannelControl}
   */
  protected AbstractDefaultSelectionsRequest(
      List<SeRequest> selectionSeRequests,
      MultiSeRequestProcessing multiSeRequestProcessing,
      ChannelControl channelControl) {
    this.selectionSeRequests = selectionSeRequests;
    this.multiSeRequestProcessing = multiSeRequestProcessing;
    this.channelControl = channelControl;
  }

  /**
   * Gets the indication whether the selection process should stop after the first matching case or
   * process all of them.
   *
   * @return an enum constant of type {@link MultiSeRequestProcessing} (FIRST_MATCH or PROCESS_ALL)
   */
  public final MultiSeRequestProcessing getMultiSeRequestProcessing() {
    return multiSeRequestProcessing;
  }

  /**
   * Gets the indication whether the logic channel is to be kept open or closed
   *
   * @return an enum constant of type {@link ChannelControl} (KEEP_OPEN or CLOSE_AFTER)
   */
  public final ChannelControl getChannelControl() {
    return channelControl;
  }

  /**
   * Gets the list of selection cases provided in the default selection
   *
   * @return a list of {@link SeRequest}
   */
  public final List<SeRequest> getSelectionSeRequests() {
    return selectionSeRequests;
  }
}
