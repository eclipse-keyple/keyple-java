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
package org.eclipse.keyple.plugin.stub;

import java.util.*;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.ReaderPoolPlugin;
import org.eclipse.keyple.core.service.exception.KeypleAllocationNoReaderException;
import org.eclipse.keyple.core.service.exception.KeypleAllocationReaderException;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;

/**
 * Simulates a @{@link ReaderPoolPlugin} with {@link StubReaderImpl} and {@link StubSecureElement}
 * Manages allocation readers by group reference, Limitations : - each group can contain only one
 * StubReader thus one StubSecureElement This class uses internally @{@link StubPluginImpl} which is
 * a singleton.
 */
final class StubPoolPluginImpl implements StubPoolPlugin {

  StubPluginImpl stubPlugin;
  Map<String, StubReaderImpl> readerPool; // groupReference, reader = limitation each
  // groupReference
  // can have only one reader
  Map<String, String> allocatedReader; // readerName,groupReference

  public StubPoolPluginImpl(String pluginName) {
    // create an embedded stubplugin to manage reader
    this.stubPlugin = (StubPluginImpl) new StubPluginFactory(pluginName).getPlugin();
    this.readerPool = new HashMap<String, StubReaderImpl>();
    this.allocatedReader = new HashMap<String, String>();
  }

  @Override
  public String getName() {
    return stubPlugin.getName();
  }

  @Override
  public SortedSet<String> getReaderGroupReferences() {
    return new TreeSet<String>(readerPool.keySet());
  }

  @Override
  public Reader plugStubPoolReader(
      String groupReference, String readerName, StubSecureElement card) {
    try {
      // create new reader
      stubPlugin.plugStubReader(readerName, true);

      // get new reader
      StubReaderImpl newReader = (StubReaderImpl) stubPlugin.getReader(readerName);

      newReader.register();

      newReader.insertSe(card);

      // map reader to groupReference
      readerPool.put(groupReference, newReader);

      return newReader;
    } catch (KeypleReaderNotFoundException e) {
      throw new IllegalStateException(
          "Impossible to allocateReader, stubplugin failed to create a reader");
    }
  }

  @Override
  public void unplugStubPoolReader(String groupReference) {
    try {
      // get reader
      Reader stubReader = readerPool.get(groupReference);

      // remove reader from pool
      readerPool.remove(groupReference);

      // remove reader from plugin
      stubPlugin.unplugStubReader(stubReader.getName(), true);

    } catch (KeypleReaderException e) {
      throw new IllegalStateException(
          "Impossible to release reader, reader with groupReference was not found in stubplugin : "
              + groupReference);
    }
  }

  /**
   * Allocate a reader if available by groupReference
   *
   * @param groupReference the reference of the group to which the reader belongs (may be null
   *     depending on the implementation made)
   * @return reader if available, null otherwise
   * @throws KeypleAllocationReaderException if the allocation failed due to a technical error
   * @throws KeypleAllocationNoReaderException if the allocation failed due to lack of available
   *     reader
   */
  @Override
  public Reader allocateReader(String groupReference) {

    // find the reader in the readerPool
    StubReaderImpl reader = readerPool.get(groupReference);

    // check if reader is found
    if (reader == null) {
      throw new KeypleAllocationReaderException(
          "Impossible to allocate a reader for groupReference : "
              + groupReference
              + ". Has the reader being plugged to this referenceGroup?");
    }
    // check if reader is available
    if (allocatedReader.containsKey(reader.getName())) {
      throw new KeypleAllocationNoReaderException(
          "Impossible to allocate a reader for groupReference : "
              + groupReference
              + ". No reader Available");
    }

    // allocate reader
    allocatedReader.put(reader.getName(), groupReference);
    return reader;
  }

  /**
   * Release a reader
   *
   * @param reader the Reader to be released.
   */
  @Override
  public void releaseReader(Reader reader) {
    if (reader == null) {
      throw new IllegalArgumentException("Could not release reader, reader is null");
    }
    if (!(reader instanceof StubReaderImpl)) {
      throw new IllegalArgumentException(
          "Can not release reader, Reader should be of type StubReader");
    }

    /** Remove and Re-insert card to reset logical channel */
    StubReaderImpl stubReader = ((StubReaderImpl) reader);
    if (stubReader.checkCardPresence()) {
      StubSecureElement card = stubReader.getSe();
      stubReader.removeSe();
      stubReader.insertSe(card);
    }

    allocatedReader.remove(reader.getName());
  }

  public Map<String, String> listAllocatedReaders() {
    return allocatedReader;
  }

  /*
   * Delegate methods to embedded stub plugin
   */

  @Override
  public Set<String> getReaderNames() {
    return stubPlugin.getReaderNames();
  }

  @Override
  public Map<String, Reader> getReaders() {
    return stubPlugin.getReaders();
  }

  /** {@inheritDoc} */
  @Override
  public Reader getReader(String name) {
    return stubPlugin.getReader(name);
  }

  @Override
  public void register() {
    stubPlugin.register();
  }

  @Override
  public void unregister() {
    stubPlugin.unregister();
  }
}
