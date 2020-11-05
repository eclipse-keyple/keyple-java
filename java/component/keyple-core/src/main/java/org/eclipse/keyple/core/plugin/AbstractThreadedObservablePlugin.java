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
import org.eclipse.keyple.core.plugin.reader.AbstractReader;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.event.ObservablePlugin;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractThreadedObservablePlugin} class provides the means to observe a plugin
 * (insertion/removal of readers) using a monitoring thread.
 */
public abstract class AbstractThreadedObservablePlugin extends AbstractObservablePlugin {
  private static final Logger logger =
      LoggerFactory.getLogger(AbstractThreadedObservablePlugin.class);

  /**
   * Instantiates a threaded observable plugin.
   *
   * @param name name of the plugin
   * @throws KeypleReaderException when an issue is raised with reader
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
   */
  protected abstract Reader fetchNativeReader(String name);

  /**
   * Add a plugin observer.
   *
   * <p>Overrides the method defined in {@link AbstractObservablePlugin}, a thread is created if it
   * does not already exist (when the first observer is added).
   *
   * @param observer the observer object
   */
  @Override
  public final void addObserver(final ObservablePlugin.PluginObserver observer) {
    super.addObserver(observer);
    if (countObservers() == 1) {
      if (logger.isDebugEnabled()) {
        logger.debug("Start monitoring the plugin {}", this.getName());
      }
      thread = new EventThread(this.getName());
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
   * <p>Overrides the method defined in {@link AbstractObservablePlugin}, the thread is ended.
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
   * Check weither the background job is monitoring for new readers
   *
   * @return true, if the background job is monitoring, false in all other cases.
   * @deprecated will change in a later version
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

  /**
   * List of names of the physical (native) connected readers This list helps synchronizing physical
   * readers managed by third-party library such as smardcard.io and the list of keyple {@link
   * Reader} Insertion, removal, and access operations safely execute concurrently by multiple
   * threads.
   */
  private final SortedSet<String> nativeReadersNames = new ConcurrentSkipListSet<String>();

  /** Thread in charge of reporting live events */
  private class EventThread extends Thread {
    private final String pluginName;
    private boolean running = true;

    private EventThread(String pluginName) {
      this.pluginName = pluginName;
    }

    /** Marks the thread as one that should end when the last cardWaitTimeout occurs */
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
      reader.register();
      readers.put(reader.getName(), reader);
      if (logger.isTraceEnabled()) {
        logger.trace(
            "[{}][{}] Plugin thread => Add plugged reader to readers list.",
            this.pluginName,
            reader.getName());
      }
      /* add reader name to the current list */
      nativeReadersNames.add(readerName);
    }

    /**
     * (private)<br>
     * Removes a reader from the list of known readers (by the plugin)
     */
    private void removeReader(Reader reader) {
      reader.unregister();
      readers.remove(reader.getName());
      if (logger.isTraceEnabled()) {
        logger.trace(
            "[{}][{}] Plugin thread => Remove unplugged reader from readers list.",
            this.pluginName,
            reader.getName());
      }
      /* remove reader name from the current list */
      nativeReadersNames.remove(reader.getName());
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
    private void processChanges(SortedSet<String> actualNativeReadersNames) {
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
        notifyChanges(PluginEvent.EventType.READER_DISCONNECTED, changedReaderNames);
        /* list update */
        for (Reader reader : readerCollection) {
          if (!actualNativeReadersNames.contains(reader.getName())) {
            removeReader(reader);
          }
        }
        /* clean the list for a possible connection notification */
        changedReaderNames.clear();
      }
      /*
       * parse the new readers list, notify for readers appearance, update readers
       * list
       */
      for (String readerName : actualNativeReadersNames) {
        if (!nativeReadersNames.contains(readerName)) {
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
          SortedSet<String> actualNativeReadersNames =
              AbstractThreadedObservablePlugin.this.fetchNativeReadersNames();
          /*
           * checks if it has changed this algorithm favors cases where nothing change
           */
          if (!nativeReadersNames.equals(actualNativeReadersNames)) {
            processChanges(actualNativeReadersNames);
          }
          /* sleep for a while. */
          Thread.sleep(threadWaitTimeout);
        }
      } catch (InterruptedException e) {
        logger.warn(
            "[{}] An exception occurred while monitoring plugin: {}",
            this.pluginName,
            e.getMessage(),
            e);
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
}
