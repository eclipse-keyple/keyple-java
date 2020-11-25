/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.plugin;

/**
 * This class adds to AbstractObservableLocalReader methods to allow the reader implementation to
 * call back the core when card insertion and removal events occur.
 *
 * @since 1.0
 */
public abstract class AbstractObservableLocalAutonomousReader
    extends AbstractObservableLocalReader {

  /** {@inheritDoc} */
  protected AbstractObservableLocalAutonomousReader(String pluginName, String readerName) {
    super(pluginName, readerName);
  }

  /**
   * This method must be called when a card is inserted.
   *
   * @since 1.0
   */
  protected final void onCardInserted() {
    onEvent(InternalEvent.CARD_INSERTED);
  }

  /**
   * This method must be called when a card is removed.
   *
   * @since 1.0
   */
  protected final void onCardRemoved() {
    onEvent(InternalEvent.CARD_REMOVED);
  }
}
