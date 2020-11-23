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
package org.eclipse.keyple.core.plugin;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractThreadedObservablePlugin} class provides the means to observe a plugin
 * (insertion/removal of readers) using a monitoring thread.
 *
 * @since 0.9
 */
public abstract class AbstractThreadedObservablePlugin extends AbstractObservablePlugin {
  private static final Logger logger =
      LoggerFactory.getLogger(AbstractThreadedObservablePlugin.class);

  /**
   * Instantiates a threaded observable plugin.
   *
   * @param name name of the plugin
   * @throws KeypleReaderException when an issue is raised with reader
   * @since 0.9
   */
  protected AbstractThreadedObservablePlugin(String name) {
    super(name);
  }

  /**
   * Fetch the list of connected native reader (usually from third party library) and returns their
   * names (or id)
   *
   * @return connected readers' name list
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @since 0.9
   */
  protected abstract SortedSet<String> fetchNativeReadersNames();

  /**
   * Fetch connected native reader (from third party library) by its name Returns the current {@link
   * AbstractReader} if it is already listed. Creates and returns a new {@link AbstractReader} if
   * not.
   *
   * @param name the reader name
   * @return the list of AbstractReader objects.
   * @throws KeypleReaderNotFoundException if the reader was not found by its name
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @since 0.9
   */
  protected abstract Reader fetchNativeReader(String name);

  /**
   * Add a plugin observer.
   *
   * <p>Overrides the method defined in {@link AbstractObservablePlugin}, a thread is created if it
   * does not already exist (when the first observer is added).
   *
   * <p>Register the {@link PluginObservationExceptionHandler} returned by the plugin implementation
   * of getObservationExceptionHandler as an uncaught exception handler.
   *
   * @param observer the observer object
   * @throws IllegalStateException If observer is null or no {@link
   *     PluginObservationExceptionHandler} has been set.
   * @since 0.9
   */
  @Override
  public final void addObserver(final ObservablePlugin.PluginObserver observer) {

    Assert.getInstance().notNull(observer, "observer");

    super.addObserver(observer);
    if (countObservers() == 1) {
      if (getObservationExceptionHandler() == null) {
        throw new IllegalStateException("No plugin observation exception handler has been set.");
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Start monitoring the plugin {}", this.getName());
      }
      thread = new EventThread(this.getName());
      thread.setName("PluginEventMonitoringThread");
      thread.setUncaughtExceptionHandler(
          new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
              getObservationExceptionHandler().onPluginObservationError(thread.pluginName, e);
            }
          });
      thread.start();
    }
  }

  /**
   * Remove a plugin observer.
   *
   * <p>Overrides the method defined in {@link AbstractObservablePlugin}, the monitoring thread is
   * ended when the last observer is removed.
   *
   * @param observer the observer object
   * @since 0.9
   */
  @Override
  public final void removeObserver(final ObservablePlugin.PluginObserver observer) {
    super.removeObserver(observer);
    if (countObservers() == 0) {
      if (logger.isDebugEnabled()) {
        logger.debug("Stop the plugin monitoring.");
      }
      if (thread != null) {
        thread.end();
      }
    }
  }

  /**
   * Remove all observers at once
   *
   * <p>In addition to the super method in {@link AbstractObservablePlugin}, the thread is ended.
   *
   * @since 0.9
   */
  @Override
  public final void clearObservers() {
    super.clearObservers();
    if (thread != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Stop the plugin monitoring.");
      }
      thread.end();
    }
  }

  /**
   * Check whether the background job is monitoring for new readers
   *
   * @return true, if the background job is monitoring, false in all other cases.
   * @deprecated will change in a later version
   * @since 0.9
   */
  @Deprecated
  protected Boolean isMonitoring() {
    return thread != null && thread.isAlive() && thread.isMonitoring();
  }

  /* Reader insertion/removal management */
  private static final long SETTING_THREAD_TIMEOUT_DEFAULT = 1000;

  /** Local thread to monitoring readers presence */
  private EventThread thread;

  /**
   * Thread wait timeout in ms
   *
   * <p>This timeout value will determined the latency to detect changes
   */
  protected long threadWaitTimeout = SETTING_THREAD_TIMEOUT_DEFAULT;

  /** Thread in charge of reporting live events */
  private class EventThread extends Thread {
    private final String pluginName;
    private boolean running = true;

    private EventThread(String pluginName) {
      this.pluginName = pluginName;
    }

    /** Marks the thread as one that should end when the last threadWaitTimeout occurs */
    void end() {
      running = false;
      this.interrupt();
    }

    /**
     * (private)<br>
     * Indicate whether the thread is running or not
     */
    boolean isMonitoring() {
      return running;
    }

    /**
     * (private)<br>
     * Adds a reader to the list of known readers (by the plugin)
     */
    private void addReader(String readerName) {
      Reader reader = fetchNativeReader(readerName);
      ((AbstractReader) reader).register();
      readers.put(reader.getName(), reader);
      if (logger.isTraceEnabled()) {
        logger.trace(
            "[{}][{}] Plugin thread => Add plugged reader to readers list.",
            this.pluginName,
            reader.getName());
      }
    }

    /**
     * (private)<br>
     * Removes a reader from the list of known readers (by the plugin)
     */
    private void removeReader(Reader reader) {
      ((AbstractReader) reader).unregister();
      readers.remove(reader.getName());
      if (logger.isTraceEnabled()) {
        logger.trace(
            "[{}][{}] Plugin thread => Remove unplugged reader from readers list.",
            this.pluginName,
            reader.getName());
      }
    }

    /**
     * (private)<br>
     * Notifies observers of changes in the list of readers<br>
     * .
     */
    private void notifyChanges(
        PluginEvent.EventType eventType, SortedSet<String> changedReaderNames) {
      /* grouped notification */
      if (logger.isTraceEnabled()) {
        logger.trace(
            "Notifying {}(s): {}",
            eventType == PluginEvent.EventType.READER_CONNECTED ? "connection" : "disconnection",
            changedReaderNames);
      }
      notifyObservers(new PluginEvent(this.pluginName, changedReaderNames, eventType));
    }

    /**
     * (private)<br>
     * Compares the list of current readers to the list provided by the system and adds or removes
     * readers accordingly.<br>
     * Observers are notified of changes.
     *
     * @param actualNativeReadersNames the list of readers currently known by the system
     */
    private void processChanges(Set<String> actualNativeReadersNames) {
      SortedSet<String> changedReaderNames = new ConcurrentSkipListSet<String>();
      /*
       * parse the current readers list, notify for disappeared readers, update
       * readers list
       */
      final Collection<Reader> readerCollection = readers.values();
      for (Reader reader : readerCollection) {
        if (!actualNativeReadersNames.contains(reader.getName())) {
          changedReaderNames.add(reader.getName());
        }
      }
      /* notify disconnections if any and update the reader list */
      if (!changedReaderNames.isEmpty()) {
        /* list update */
        for (Reader reader : readerCollection) {
          if (!actualNativeReadersNames.contains(reader.getName())) {
            removeReader(reader);
          }
        }
        notifyChanges(PluginEvent.EventType.READER_DISCONNECTED, changedReaderNames);
        /* clean the list for a possible connection notification */
        changedReaderNames.clear();
      }
      /*
       * parse the new readers list, notify for readers appearance, update readers
       * list
       */
      for (String readerName : actualNativeReadersNames) {
        if (!getReaderNames().contains(readerName)) {
          addReader(readerName);
          /* add to the notification list */
          changedReaderNames.add(readerName);
        }
      }
      /* notify connections if any */
      if (!changedReaderNames.isEmpty()) {
        notifyChanges(PluginEvent.EventType.READER_CONNECTED, changedReaderNames);
      }
    }

    /**
     * Reader monitoring loop<br>
     * Checks reader insertions and removals<br>
     * Notifies observers of any changes
     */
    @Override
    public void run() {
      try {
        while (running) {
          /* retrieves the current readers names list */
          Set<String> actualNativeReadersNames =
              AbstractThreadedObservablePlugin.this.fetchNativeReadersNames();
          /*
           * checks if it has changed this algorithm favors cases where nothing change
           */
          Set<String> currentlyRegisteredReaderNames = getReaderNames();
          if (!currentlyRegisteredReaderNames.containsAll(actualNativeReadersNames)
              || !actualNativeReadersNames.containsAll(currentlyRegisteredReaderNames)) {
            processChanges(actualNativeReadersNames);
          }
          /* sleep for a while. */
          Thread.sleep(threadWaitTimeout);
        }
      } catch (InterruptedException e) {
        logger.info(
            "[{}] The observation of this plugin is stopped, possibly because there is no more registered observer.",
            this.pluginName);
        // Restore interrupted state...
        Thread.currentThread().interrupt();
      } catch (KeypleReaderException e) {
        logger.warn(
            "[{}] An exception occurred while monitoring plugin: {}",
            this.pluginName,
            e.getMessage(),
            e);
      }
    }
  }

  /**
   * Allows to call the defined handler when an exception condition needs to be transmitted to the
   * application level.
   *
   * <p>Must be implemented by the plugin provider.
   *
   * @return A not null reference to an object implementing the {@link
   *     PluginObservationExceptionHandler} interface.
   * @since 1.0
   */
  protected abstract PluginObservationExceptionHandler getObservationExceptionHandler();
}
