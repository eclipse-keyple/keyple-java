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

/**
 * Indicates the action to be taken on the physical channel at the end of the transmission of the
 * request.
 *
 * @since 0.9
 */
public enum ChannelControl {
  /** lefts the physical channel open */
  KEEP_OPEN,
  /**
   * terminates the communication with the card (instantaneously closes the physical channel or
   * initiates the card removal sequence depending on the observation mode)
   */
  CLOSE_AFTER
}
