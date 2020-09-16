/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.seproxy.event;

import org.eclipse.keyple.core.seproxy.ReaderPlugin;

/**
 * Provides the API to observe readers in plugins.
 *
 * <p>Allows registered observers to receive a {@link PluginEvent} when a reader is
 * connected/disconnected
 *
 * @since 0.9
 */
public interface ObservablePlugin extends ReaderPlugin {

  /**
   * This interface has to be implemented by plugin observers.
   *
   * @since 0.9
   */
  interface PluginObserver {

    /**
     * Called when a plugin event occurs.
     *
     * <p>Note that this method is called <b>sequentially</b> on all observers.
     *
     * @param event The not null {@link PluginEvent} containing all event information.
     * @since 0.9
     */
    void update(final PluginEvent event);
  }

  /**
   * Register a new plugin observer to be notified when a plugin event occurs.
   *
   * <p>The provided observer will receive all the events produced by this plugin (reader
   * connection, disconnection).
   *
   * <p>It is possible to add as many observers as necessary. They will be notified of events
   * <b>sequentially</b> in the order in which they are added.
   *
   * @param observer An observer object implementing the required interface (should be not null).
   * @since 0.9
   */
  void addObserver(final PluginObserver observer);

  /**
   * Unregister a plugin observer.
   *
   * <p>The observer will no longer receive any of the events produced by this plugin.
   *
   * @param observer The observer object to be unregistered (should be not null).
   * @since 0.9
   */
  void removeObserver(final PluginObserver observer);

  /**
   * Unregister all observers at once.
   *
   * @since 0.9
   */
  void clearObservers();

  /**
   * Provides the current number of registered observers.
   *
   * @return an int
   * @since 0.9
   */
  int countObservers();
}
