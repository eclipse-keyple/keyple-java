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
package org.eclipse.keyple.distributed.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.keyple.core.service.Plugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;

/**
 * (package-private)<br>
 * Abstract class for all Remote Plugins.
 *
 * <p>This object behaves both as a {@link AbstractMessageHandler} and as a {@link
 * org.eclipse.keyple.core.service.Plugin}.
 *
 * @since 1.0
 */
abstract class AbstractRemotePlugin extends AbstractMessageHandler implements Plugin {

  private final String name;

  /**
   * (package-private)<br>
   * Map of remote reader by remote name.
   *
   * @since 1.0
   */
  final Map<String, Reader> readers;

  /**
   * (package-private)<br>
   *
   * <ul>
   *   <li>Instantiates a new ReaderPlugin.
   *   <li>Retrieve the current readers list.
   *   <li>Initialize the list of readers invoking the abstract method initNativeReaders.
   *   <li>When readers initialisation failed, a KeypleReaderException is thrown.
   * </ul>
   *
   * @param name The name of the plugin.
   * @throws KeypleReaderException when an issue is raised with reader
   * @since 1.0
   */
  AbstractRemotePlugin(String name) {
    super();
    this.name = name;
    this.readers = new ConcurrentHashMap<String, Reader>();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public final String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public final Map<String, Reader> getReaders() {
    return readers;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public final Set<String> getReaderNames() {
    return readers.keySet();
  }

  /**
   * (package-private)<br>
   * Init connected local readers (from third party library) and returns a map of corresponding
   * {@link Reader} with their name as key and each {@link Reader} is a new instance.
   *
   * <p>this method is invoked once in the plugin constructor.
   *
   * @return a not null map.
   * @throws KeypleReaderIOException if the communication with the reader or the Card has failed.
   * @since 1.0
   */
  abstract Map<String, Reader> initNativeReaders();
}
