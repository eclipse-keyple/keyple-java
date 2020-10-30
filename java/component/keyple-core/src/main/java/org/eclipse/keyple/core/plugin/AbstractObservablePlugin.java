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
package org.eclipse.keyple.core.plugin;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class class provides the means to make a plugin observable.
 *
 * <p>It allows:
 *
 * <ul>
 *   <li>the management of the observers (add/remove/count) (see the {@link
 *       ObservablePlugin.PluginObserver} interface)
 *   <li>the notification of {@link PluginEvent} to all the observers
 * </ul>
 *
 * @since 0.9
 */
public abstract class AbstractObservablePlugin extends AbstractPlugin
    implements ObservablePluginNotifier {
  private static final Logger logger = LoggerFactory.getLogger(AbstractObservablePlugin.class);

  private List<ObservablePlugin.PluginObserver> observers;
  /*
   * this object will be used to synchronize the access to the observers list in order to be
   * thread safe
   */
  private final Object sync = new Object();

  /**
   * Constructor.
   *
   * @param name A not empty String containing the name of the plugin
   * @throws KeypleReaderException when an issue is raised with reader
   * @since 0.9
   */
  protected AbstractObservablePlugin(String name) {
    super(name);
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.9
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
   * {@inheritDoc}
   *
   * @since 0.9
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

  /**
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  public void clearObservers() {
    if (observers != null) {
      this.observers.clear();
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  public final int countObservers() {
    return observers == null ? 0 : observers.size();
  }

  /**
   * {@inheritDoc}
   *
   * @since 0.9
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

  @Override
  public void unregister() {
    super.unregister();
    notifyObservers(
        new PluginEvent(
            this.getName(),
            readers.keySet(),
            PluginEvent.EventType.UNREGISTERED)); // Why do we need to pass readers name?
    clearObservers();
  }
}
