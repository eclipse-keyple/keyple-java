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

import java.util.Set;
import org.eclipse.keyple.core.service.event.ObservablePlugin;

/**
 * This plugin allows to simulate card communication by creating @{@link StubReaderImpl} and @{@link
 * StubSmartCard}. Plug a new StubReader with StubPlugin#plugStubReader and insert an implementation
 * of your own of {@link StubSmartCard} to start simulation communication.
 */
public interface StubPlugin extends ObservablePlugin {

  /**
   * Plug a new {@link StubReader} available in the plugin
   *
   * @param name : name of the created reader
   * @param synchronous : should the stubreader added synchronously (without waiting for the
   *     observation thread). A READER_CONNECTED event is raised in both cases
   * @since 1.0
   */
  void plugStubReader(String name, Boolean synchronous);

  /**
   * Plug a new {@link StubReader} available in the plugin
   *
   * @param name : name of the created reader
   * @param isContactless : true if the created reader should be contactless, false if not.
   * @param synchronous : should the stubreader added synchronously (without waiting for the
   *     observation thread). A READER_CONNECTED event is raised in both cases
   * @since 1.0
   */
  void plugStubReader(String name, boolean isContactless, Boolean synchronous);

  /**
   * Plug multiple new {@link StubReader} available in the plugin
   *
   * @param names : names of readers to be connected
   * @param synchronous : should the stubreader be added synchronously (without waiting for the
   *     observation thread). A READER_CONNECTED event is raised in both cases
   * @since 1.0
   */
  void plugStubReaders(Set<String> names, Boolean synchronous);

  /**
   * Unplug a {@link StubReader}
   *
   * @param name the name of the reader
   * @throws IllegalStateException in case of a reader exception
   * @param synchronous : should the stubreader be removed synchronously (without waiting for the
   *     observation thread). A READER_DISCONNECTED event is raised in both cases
   * @since 1.0
   */
  void unplugStubReader(String name, Boolean synchronous);

  /**
   * Unplug a list of {@link StubReader}
   *
   * @param names : names of the reader to be unplugged
   * @param synchronous : should the stubreader removed synchronously (without waiting for the
   *     observation thread). A READER_DISCONNECTED event is raised in both cases
   * @since 1.0
   */
  void unplugStubReaders(Set<String> names, Boolean synchronous);
}
