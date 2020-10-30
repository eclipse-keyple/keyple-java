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
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;

/** Stubplugin allows to simulate a {@link Plugin} and a {@link StubSecureElement} */
public interface StubPlugin extends ObservablePlugin {

  /**
   * Plug a Stub Reader
   *
   * @param name : name of the created reader
   * @param synchronous : should the stubreader added synchronously (without waiting for the
   *     observation thread). An READER_CONNECTED event is raised in both cases
   */
  void plugStubReader(String name, Boolean synchronous);

  /**
   * Plug a Stub Reader
   *
   * @param name : name of the created reader
   * @param isContactless : true if the created reader is contactless, false if not.
   * @param synchronous : should the stubreader added synchronously (without waiting for the
   *     observation thread). An READER_CONNECTED event is raised in both cases
   */
  void plugStubReader(String name, boolean isContactless, Boolean synchronous);

  /**
   * Plug a list of stub Reader at once
   *
   * @param names : names of readers to be connected
   * @param synchronous : should the stubreader be added synchronously (without waiting for the
   *     observation thread). An READER_CONNECTED event is raised in both cases
   */
  void plugStubReaders(Set<String> names, Boolean synchronous);

  /**
   * Unplug a Stub Reader
   *
   * @param name the name of the reader
   * @throws KeypleReaderException in case of a reader exception
   * @param synchronous : should the stubreader be removed synchronously (without waiting for the
   *     observation thread). An READER_DISCONNECTED event is raised in both cases
   */
  void unplugStubReader(String name, Boolean synchronous);

  /**
   * Unplug a list of readers
   *
   * @param names : names of the reader to be unplugged
   * @param synchronous : should the stubreader removed synchronously (without waiting for the
   *     observation thread). An READER_DISCONNECTED event is raised in both cases
   */
  void unplugStubReaders(Set<String> names, Boolean synchronous);
}
