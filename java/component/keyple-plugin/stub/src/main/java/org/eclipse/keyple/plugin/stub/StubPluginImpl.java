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
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin allows to simulate card communication by creating @{@link StubReaderImpl} and @{@link
 * StubSecureElement}. Plug a new StubReader with StubPlugin#plugStubReader and insert an
 * implementation of your own of {@link StubSecureElement} to start simulation communication. This
 * class is a singleton, use StubPlugin#getInstance to access it
 */
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
    if (synchronous) {
      /* add the reader as a new reader to the readers list */
      StubReaderImpl stubReader = new StubReaderImpl(this.getName(), readerName, isContactless);
      readers.put(readerName, stubReader);
      if (this.countObservers() == 0) {
        // if no observer, no monitoring thread is started, then it needs to be registered manually
        stubReader.register();
      }
    }
    connectedStubNames.add(readerName);

    logger.info(
        "Plugged a new reader with readerName:{} synchronously:{}", readerName, synchronous);
  }

  @Override
  public void plugStubReaders(Set<String> readerNames, Boolean synchronous) {
    logger.info("Plugging {} readers ..", readerNames.size());

    /* plug stub readers that were not plugged already */
    Set<String> newNames = new HashSet<String>(readerNames);
    newNames.removeAll(connectedStubNames);

    logger.info("New readers to be created #{}", newNames.size());

    /*
     * Add new readerNames to the connectedStubNames
     */
    if (newNames.isEmpty()) {
      logger.error("All {} readers were already plugged", readerNames.size());
      return;
    }

    if (synchronous) {
      ConcurrentMap<String, StubReaderImpl> newReaders =
          new ConcurrentHashMap<String, StubReaderImpl>();
      for (String name : newNames) {
        StubReaderImpl stubReader = new StubReaderImpl(this.getName(), name, true);
        newReaders.put(name, stubReader);
        if (this.countObservers() == 0) {
          // if no observer, no monitoring thread is started, then it needs to be registered
          // manually
          stubReader.register();
        }
      }
      readers.putAll(newReaders);
    }

    connectedStubNames.addAll(readerNames);
  }

  /** {@inheritDoc} */
  @Override
  public void unplugStubReader(String readerName, Boolean synchronous) {

    if (!connectedStubNames.contains(readerName)) {
      logger.warn("No reader found with name {}", readerName);
      return;
    }

    /* remove the reader from the readers list */
    if (synchronous) {
      Reader reader = readers.get(readerName);
      readers.remove(readerName);
      // if no observer, no monitoring thread is started, then it needs to be unregistered manually
      if (this.countObservers() == 0) {
        reader.unregister();
      }
    }

    connectedStubNames.remove(readerName);

    /* remove the native reader from the native readers list */
    logger.info(
        "Unplugged reader with name {}, remaining stub readers {}",
        readerName,
        connectedStubNames.size());
  }

  @Override
  public void unplugStubReaders(Set<String> readerNames, Boolean synchronous) {
    logger.trace("Unplug stub readers.. {}", readerNames);
    List<Reader> readersToDelete = new ArrayList<Reader>();
    for (String name : readerNames) {
      try {
        readersToDelete.add(getReader(name));
      } catch (KeypleReaderNotFoundException e) {
        logger.warn("unplugStubReaders() No reader found with name {}", name);
      }
    }
    if (synchronous) {
      for (Reader reader : readersToDelete) {
        // if no observer, no monitoring thread is started, then it needs to be unregistered
        // manually
        if (this.countObservers() == 0) {
          reader.unregister();
        }
        readers.remove(reader.getName());
      }
    }
    connectedStubNames.removeAll(readerNames);
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
