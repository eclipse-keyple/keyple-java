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
package org.eclipse.keyple.plugin.stub;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.keyple.core.plugin.AbstractThreadedObservablePlugin;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of {@link StubPlugin} */
final class StubPluginImpl extends AbstractThreadedObservablePlugin implements StubPlugin {

  private static final Logger logger = LoggerFactory.getLogger(StubPluginImpl.class);

  // simulated list of real-time connected stubReader
  private SortedSet<String> connectedStubNames = new TreeSet<String>();

  /**
   * Constructor
   *
   * @param pluginName : custom name for the plugin
   */
  StubPluginImpl(String pluginName) {
    super(pluginName);

    /*
     * Monitoring is not handled by a lower layer (as in PC/SC), reduce the threading period to
     * 10 ms to speed up responsiveness.
     */
    threadWaitTimeout = 10;
  }

  public void plugStubReader(String readerName, Boolean synchronous) {
    plugStubReader(readerName, true, synchronous);
  }

  @Override
  public void plugStubReader(String readerName, boolean isContactless, Boolean synchronous) {

    /* add the native reader to the native readers list */
    if (connectedStubNames.contains(readerName)) {
      logger.error("Reader with readerName {} was already plugged", readerName);
      return;
    }

    connectedStubNames.add(readerName);

    if (synchronous) {
      if (this.countObservers() == 0) {
        /* add the reader as a new reader to the readers list */
        StubReaderImpl stubReader = new StubReaderImpl(this.getName(), readerName, isContactless);
        // if no observer, no monitoring thread is started, then it needs to be added and registered
        // manually
        readers.put(readerName, stubReader);
        stubReader.register();
      } else {
        // wait until readers contains readerName
        while (!Thread.currentThread().isInterrupted() && !readers.keySet().contains(readerName)) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            logger.error("Unexpected thread interruption.");
            Thread.currentThread().interrupt();
          }
        }
      }
    }

    logger.info(
        "Plugged a new reader with readerName:{} synchronously:{}", readerName, synchronous);
  }

  @Override
  public void plugStubReaders(Set<String> readerNames, Boolean synchronous) {
    logger.info("Plugging {} readers ..", readerNames.size());

    connectedStubNames.addAll(readerNames);

    if (synchronous) {
      if (this.countObservers() == 0) {
        for (String readerName : readerNames) {
          /* add the reader as a new reader to the readers list */
          StubReaderImpl stubReader = new StubReaderImpl(this.getName(), readerName, true);
          // if no observer, no monitoring thread is started, then it needs to be added and
          // registered
          // manually
          readers.put(readerName, stubReader);
          stubReader.register();
        }
      } else {
        // wait until readers contains readerName
        while (!Thread.currentThread().isInterrupted()
            && !readers.keySet().containsAll(readerNames)) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            logger.error("Unexpected thread interruption.");
            Thread.currentThread().interrupt();
          }
        }
      }
    }

    logger.info(
        "Plugged new readers with readerNames:{} synchronously:{}", readerNames, synchronous);
  }

  /** {@inheritDoc} */
  @Override
  public void unplugStubReader(String readerName, Boolean synchronous) {

    if (!connectedStubNames.contains(readerName)) {
      logger.warn("No reader found with name {}", readerName);
      return;
    }

    connectedStubNames.remove(readerName);

    /* remove the reader from the readers list */
    if (synchronous) {
      // if no observer, no monitoring thread is started, then it needs to be removed and
      // unregistered manually
      if (this.countObservers() == 0) {
        Reader reader = readers.get(readerName);
        readers.remove(readerName);
        reader.unregister();
      } else {
        // wait until readers not contain readerName
        while (!Thread.currentThread().isInterrupted() && readers.keySet().contains(readerName)) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            logger.error("Unexpected thread interruption.");
            Thread.currentThread().interrupt();
          }
        }
      }
    }

    /* remove the native reader from the native readers list */
    logger.info(
        "Unplugged reader with name {}, remaining stub readers {}",
        readerName,
        connectedStubNames.size());
  }

  @Override
  public void unplugStubReaders(Set<String> readerNames, Boolean synchronous) {
    logger.trace("Unplug stub readers.. {}", readerNames);

    connectedStubNames.removeAll(readerNames);

    /* remove the reader from the readers list */
    if (synchronous) {
      // if no observer, no monitoring thread is started, then it needs to be removed and
      // unregistered manually
      if (this.countObservers() == 0) {
        for (String readerName : readerNames) {
          Reader reader = readers.get(readerName);
          readers.remove(readerName);
          reader.unregister();
        }
      } else {
        // wait until readers not contain readerName
        while (!Thread.currentThread().isInterrupted()
            && readers.keySet().containsAll(readerNames)) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            logger.error("Unexpected thread interruption.");
            Thread.currentThread().interrupt();
          }
        }
      }
    }
    /* remove the native reader from the native readers list */
    logger.info(
        "Unplugged readers with names {}, remaining stub readers {}",
        readerNames,
        connectedStubNames.size());
  }

  /**
   * Fetch the list of connected native reader (from a simulated list) and returns their names (or
   * id)<br>
   * {@inheritDoc}
   */
  @Override
  public SortedSet<String> fetchNativeReadersNames() {
    return connectedStubNames;
  }

  /**
   * Init native Readers to empty Set
   *
   * @return the map of Reader objects.
   * @throws KeypleReaderException if a reader error occurs
   */
  @Override
  protected ConcurrentMap<String, Reader> initNativeReaders() {
    return new ConcurrentHashMap<String, Reader>();
  }

  /**
   * Fetch the reader whose name is provided as an argument. Returns the current reader if it is
   * already listed. Creates and returns a new reader if not.
   *
   * <p>Throws an exception if the wanted reader is not found.
   *
   * @param readerName name of the reader
   * @return the reader object
   */
  @Override
  protected Reader fetchNativeReader(String readerName) {
    Reader reader = readers.get(readerName);
    if (reader == null && connectedStubNames.contains(readerName)) {
      reader = new StubReaderImpl(this.getName(), readerName);
    }
    return reader;
  }
}
