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
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.core.seproxy.Reader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservablePlugin;
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

  private final Map<String, String> parameters = new HashMap<String, String>();

  // simulated list of real-time connected stubReader
  private SortedSet<String> connectedStubNames =
      Collections.synchronizedSortedSet(new ConcurrentSkipListSet<String>());

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

    logger.info("Plugging a new reader with readerName {}", readerName);
    /* add the native reader to the native readers list */
    Boolean exist = connectedStubNames.contains(readerName);

    if (!exist && synchronous) {
      /* add the reader as a new reader to the readers list */
      readers.put(readerName, new StubReaderImpl(this.getName(), readerName, isContactless));
    }

    connectedStubNames.add(readerName);

    if (exist) {
      logger.error("Reader with readerName {} was already plugged", readerName);
    }
  }

  @Override
  public void plugStubReaders(Set<String> readerNames, Boolean synchronous) {
    logger.debug("Plugging {} readers ..", readerNames.size());

    /* plug stub readers that were not plugged already */
    // duplicate readerNames
    Set<String> newNames = new HashSet<String>(readerNames);
    // remove already connected stubNames
    newNames.removeAll(connectedStubNames);

    logger.debug("New readers to be created #{}", newNames.size());

    /*
     * Add new readerNames to the connectedStubNames
     */

    if (!newNames.isEmpty()) {
      if (synchronous) {
        ConcurrentMap<String, StubReaderImpl> newReaders =
            new ConcurrentHashMap<String, StubReaderImpl>();
        for (String name : newNames) {
          newReaders.put(name, new StubReaderImpl(this.getName(), name));
        }
        readers.putAll(newReaders);
      }

      connectedStubNames.addAll(readerNames);

    } else {
      logger.error("All {} readers were already plugged", readerNames.size());
    }
  }

  /** {@inheritDoc} */
  @Override
  public void unplugStubReader(String readerName, Boolean synchronous) {

    if (!connectedStubNames.contains(readerName)) {
      logger.warn("unplugStubReader() No reader found with name {}", readerName);
    } else {
      /* remove the reader from the readers list */
      if (synchronous) {
        connectedStubNames.remove(readerName);
        readers.remove(readerName);
      } else {
        connectedStubNames.remove(readerName);
      }
      /* remove the native reader from the native readers list */
      logger.info(
          "Unplugged reader with name {}, connectedStubNames size {}",
          readerName,
          connectedStubNames.size());
    }
  }

  @Override
  public void unplugStubReaders(Set<String> readerNames, Boolean synchronous) {
    logger.info("Unplug {} stub readers", readerNames.size());
    logger.debug("Unplug stub readers.. {}", readerNames);
    List<StubReaderImpl> readersToDelete = new ArrayList<StubReaderImpl>();
    for (String name : readerNames) {
      try {
        readersToDelete.add((StubReaderImpl) getReader(name));
      } catch (KeypleReaderNotFoundException e) {
        logger.warn("unplugStubReaders() No reader found with name {}", name);
      }
    }
    connectedStubNames.removeAll(readerNames);
    if (synchronous) {
      for (StubReaderImpl reader : readersToDelete) {
        readers.remove(reader.getName());
      }
    }
  }

  /**
   * Fetch the list of connected native reader (from a simulated list) and returns their names (or
   * id)<br>
   * {@inheritDoc}
   */
  @Override
  public SortedSet<String> fetchNativeReadersNames() {
    if (connectedStubNames.isEmpty()) {
      logger.trace("No reader available.");
    }
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
    /* init Stub Readers response object */
    ConcurrentMap<String, Reader> newNativeReaders = new ConcurrentHashMap<String, Reader>();
    return newNativeReaders;
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
