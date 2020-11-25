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

import org.eclipse.keyple.core.service.PoolPlugin;
import org.eclipse.keyple.core.service.Reader;

/** StubPoolPlugin allows to simulate a {@link PoolPlugin} and a {@link StubSmartCard} */
public interface StubPoolPlugin extends PoolPlugin {

  /**
   * Plug synchronously a new @{@link StubReaderImpl} in Pool with groupReference and a StubSE. A
   * READER_CONNECTED event will be raised.
   *
   * @param groupReference : group refence of the new stub reader
   * @param readerName : name of the new stub reader
   * @param card : insert a card at creation (can be null)
   * @return created StubReader
   * @since 1.0
   */
  Reader plugStubPoolReader(String groupReference, String readerName, StubSmartCard card);

  /**
   * Unplug synchronously a new reader by groupReference. A READER_DISCONNECTED event will be
   * raised.
   *
   * @param groupReference groupReference of the reader to be unplugged
   * @since 1.0
   */
  void unplugStubPoolReader(String groupReference);
}
