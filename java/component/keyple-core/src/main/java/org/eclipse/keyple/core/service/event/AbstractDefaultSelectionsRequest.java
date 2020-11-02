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
import org.eclipse.keyple.core.card.message.ChannelControl;
import org.eclipse.keyple.core.card.message.SelectionRequest;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;

/**
 * This abstract class defines the POJO used to carry the default selection request data.
 *
 * <p>The default selection request is obtained from the selection preparation process and provided
 * to an observable reader.
 *
 * @since 0.9
 */
public abstract class AbstractDefaultSelectionsRequest {

  /**
   * Gets the indication whether the selection process should stop after the first matching case or
   * process all of them.
   *
   * @return A not null value.
   * @since 0.9
   */
  protected abstract MultiSelectionProcessing getMultiSelectionProcessing();

  /**
   * Gets the indication whether the logic channel is to be kept open or closed
   *
   * @return A not null value.
   * @since 0.9
   */
  protected abstract ChannelControl getChannelControl();

  /**
   * Gets the list of selection cases provided in the default selection
   *
   * @return A not empty list.
   * @since 0.9
   */
  protected abstract List<SelectionRequest> getSelectionRequests();
}
