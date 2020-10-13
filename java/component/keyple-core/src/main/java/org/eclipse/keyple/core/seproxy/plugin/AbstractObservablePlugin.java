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
package org.eclipse.keyple.core.seproxy.plugin;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractObservablePlugin} class provides the means to observe a plugin
 * (insertion/removal of readers).
 */
public abstract class AbstractObservablePlugin extends AbstractPlugin
    implements ObservablePluginNotifier {
  private static final Logger logger = LoggerFactory.getLogger(AbstractObservablePlugin.class);

  /* The observers of this object */
  private List<ObservablePlugin.PluginObserver> observers;
  /*
   * this object will be used to synchronize the access to the observers list in order to be
   * thread safe
   */
  private final Object sync = new Object();

  /**
   * Instantiates a observable plugin.
   *
   * @param name name of the plugin
   * @throws KeypleReaderException when an issue is raised with reader
   */
  protected AbstractObservablePlugin(String name) {
    super(name);
  }

  /**
   * Add a plugin observer.
   *
   * <p>The observer will receive all the events produced by this plugin (reader insertion, removal,
   * etc.)
   *
   * @param observer the observer object
   */
  @Override
  public void addObserver(final ObservablePlugin.PluginObserver observer) {
    if (observer == null) {
      return;
    }

    if (logger.isTraceEnabled()) {
      logger.trace(
          "Adding '{}' as an observer of '{}'.", observer.getClass().getSimpleName(), getName());
    }

    synchronized (sync) {
      if (observers == null) {
        observers = new ArrayList<PluginObserver>(1);
      }
      observers.add(observer);
    }
  }

  /**
   * Remove a plugin observer.
   *
   * <p>The observer will do not receive any of the events produced by this plugin.
   *
   * @param observer the observer object
   */
  @Override
  public void removeObserver(final ObservablePlugin.PluginObserver observer) {
    if (observer == null) {
      return;
    }
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] Deleting a plugin observer", getName());
    }
    synchronized (sync) {
      if (observers != null) {
        observers.remove(observer);
      }
    }
  }

  /** Remove all observers at once */
  @Override
  public void clearObservers() {
    if (observers != null) {
      this.observers.clear();
    }
  }

  /** @return the number of observers */
  @Override
  public final int countObservers() {
    return observers == null ? 0 : observers.size();
  }

  /**
   * This method shall be called only from a SeProxy plugin implementing AbstractPlugin. Push a
   * PluginEvent of the selected AbstractPlugin to its registered Observer.
   *
   * @param event the event
   */
  @Override
  public final void notifyObservers(final PluginEvent event) {

    if (logger.isTraceEnabled()) {
      logger.trace(
          "[{}] Notifying a plugin event to {} observers. EVENTNAME = {} ",
          this.getName(),
          countObservers(),
          event.getEventType().name());
    }
    List<PluginObserver> observersCopy;

    synchronized (sync) {
      if (observers == null) {
        return;
      }
      observersCopy = new ArrayList<PluginObserver>(observers);
    }

    for (ObservablePlugin.PluginObserver observer : observersCopy) {
      observer.update(event);
    }
  }
}
