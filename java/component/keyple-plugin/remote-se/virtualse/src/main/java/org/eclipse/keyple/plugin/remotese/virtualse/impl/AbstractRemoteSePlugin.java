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
package org.eclipse.keyple.plugin.remotese.virtualse.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractPlugin;
import org.eclipse.keyple.plugin.remotese.core.impl.AbstractKeypleMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Abstract class for all Remote SE Plugins.
 *
 * <p>This object behaves both as a {@link AbstractKeypleMessageHandler} and as a {@link
 * AbstractPlugin}.
 */
abstract class AbstractRemoteSePlugin extends AbstractKeypleMessageHandler implements ReaderPlugin {

  private static final Logger logger = LoggerFactory.getLogger(AbstractRemoteSePlugin.class);

  private final String name;
  protected final Map<String, SeReader> readers;

  /**
   * (package-private)<br>
   * Constructor.
   *
   * <ul>
   *   <li>Instantiates a new ReaderPlugin.
   *   <li>Retrieve the current readers list.
   *   <li>Initialize the list of readers calling the abstract method initNativeReaders.
   *   <li>When readers initialisation failed, a KeypleReaderException is thrown.
   * </ul>
   *
   * @param name The name of the plugin.
   * @throws KeypleReaderException when an issue is raised with reader
   */
  AbstractRemoteSePlugin(String name) {
    super();
    this.name = name;
    this.readers = new ConcurrentHashMap<String, SeReader>();
    this.readers.putAll(initNativeReaders());
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
  public final Map<String, SeReader> getReaders() {
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
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public final SeReader getReader(String name) {
    SeReader seReader = readers.get(name);
    if (seReader == null) {
      throw new KeypleReaderNotFoundException(name);
    }
    return seReader;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public final void setParameters(Map<String, String> parameters) {
    for (Map.Entry<String, String> en : parameters.entrySet()) {
      setParameter(en.getKey(), en.getValue());
    }
  }

  /**
   * (protected)<br>
   * Init connected native readers (from third party library) and returns a map of corresponding
   * {@link SeReader} with their name as key and each {@link SeReader} is a new instance.
   *
   * <p>this method is called once in the plugin constructor.
   *
   * @return a not null map.
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  protected abstract ConcurrentMap<String, SeReader> initNativeReaders()
      throws KeypleReaderIOException;
}
