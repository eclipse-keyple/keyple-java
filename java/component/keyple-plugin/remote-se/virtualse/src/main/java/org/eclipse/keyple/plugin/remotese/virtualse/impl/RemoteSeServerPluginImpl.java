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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerPlugin;
import org.eclipse.keyple.plugin.remotese.virtualse.RemoteSeServerReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of RemoteSeServerPlugin
 *
 */
final class RemoteSeServerPluginImpl extends AbstractRemoteSePlugin
    implements RemoteSeServerPlugin {

  private static final Logger logger = LoggerFactory.getLogger(RemoteSeServerPluginImpl.class);

  private ExecutorService eventNotificationPool;

  /* The observers of this object */
  private final List<PluginObserver> observers;

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
  RemoteSeServerPluginImpl(String name, ExecutorService eventNotificationPool) {
    super(name);
    this.eventNotificationPool = eventNotificationPool;
    this.parameters = new HashMap<String, String>();
    this.observers = new ArrayList<PluginObserver>();
  }

  @Override
  protected ConcurrentMap<String, SeReader> initNativeReaders() throws KeypleReaderIOException {
    return new ConcurrentHashMap<String, SeReader>();
  }

  @Override
  protected void onMessage(KeypleMessageDto msg) {}

  @Override
  public void terminateService(String virtualReaderName, Object userOutputData) {}

  @Override
  public RemoteSeServerReader getReader(String name) throws KeypleReaderNotFoundException {
    RemoteSeServerReader seReader = (RemoteSeServerReader) readers.get(name);
    if (seReader == null) {
      throw new KeypleReaderNotFoundException(name);
    }
    return seReader;
  };

  @Override
  public void addObserver(PluginObserver observer) {
    Assert.getInstance().notNull(observer, "Plugin Observer");

    if (observers.add(observer) && logger.isTraceEnabled()) {
      logger.trace(
          "[{}] Added plugin observer '{}'", getName(), observer.getClass().getSimpleName());
    }
    ;
  }

  @Override
  public void removeObserver(PluginObserver observer) {
    Assert.getInstance().notNull(observer, "Plugin Observer");

    if (observers.remove(observer) && logger.isTraceEnabled()) {
      logger.trace(
          "[{}] Removed plugin observer '{}'", getName(), observer.getClass().getSimpleName());
    }
    ;
  }

  @Override
  public void clearObservers() {
    observers.clear();
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] Clear reader observers", this.getName());
    }
  }

  @Override
  public int countObservers() {
    return observers.size();
  }
}
