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
package org.eclipse.keyple.plugin.stub;

import org.eclipse.keyple.core.service.event.ObservableReader;

public interface StubReader extends ObservableReader {

  /**
   * Insert a card Stub into the reader. Will raise a CARD_INSERTED event.
   *
   * @param smartCard stub card to be inserted in the reader
   * @since 1.0
   */
  void insertCard(StubSmartCard smartCard);

  /**
   * Remove card from reader if any
   *
   * @since 1.0
   */
  void removeCard();

  /**
   * Get inserted card
   *
   * @return instance of a card, can be null if no card inserted
   * @since 1.0
   */
  StubSmartCard getSmartcard();
}
