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

/**
 * Simulates a @{@link PoolPlugin} with {@link StubReaderImpl} and {@link StubSmartCard} Manages
 * allocation readers by group reference.
 */
public interface StubPoolPlugin extends PoolPlugin {

  /**
   * Plug synchronously a new {@link StubReader} in the {@link StubPoolPlugin} associated to
   * groupReference and a stub card. A READER_CONNECTED event will be raised.
   *
   * @param groupReference : group reference of the new stub reader (mandatory)
   * @param readerName : name of the new stub reader (mandatory). Each reader should have a unique
   *     name, no matter to what groupReference they are associated to
   * @param card : insert a card at creation (can be null)
   * @return created StubReader
   * @since 1.0
   */
  Reader plugPoolReader(String groupReference, String readerName, StubSmartCard card);

  /**
   * Unplug synchronously all readers associated to a groupReference. A READER_DISCONNECTED event
   * will be raised.
   *
   * @param groupReference groupReference of the reader(s) to be unplugged (mandatory)
   * @since 1.0
   */
  void unplugPoolReaders(String groupReference);

  /**
   * Unplug synchronously a pool reader. A READER_DISCONNECTED event will be raised.
   *
   * @param readerName name of the reader to be unplugged (mandatory)
   * @since 1.0
   */
  void unplugPoolReader(String readerName);
}
