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

import org.eclipse.keyple.core.seproxy.event.ObservableReader;

public interface StubReader extends ObservableReader {

  /**
   * Insert a card Stub into the reader. Will raise a SE_INSERTED event.
   *
   * @param _se stub secure element to be inserted in the reader
   */
  void insertSe(StubSecureElement _se);

  /** Remove card from reader if any */
  void removeSe();

  /**
   * Get inserted card
   *
   * @return card, can be null if no card inserted
   */
  StubSecureElement getSe();
}
