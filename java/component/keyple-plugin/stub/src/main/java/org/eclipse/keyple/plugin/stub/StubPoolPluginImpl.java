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
import org.eclipse.keyple.core.service.event.PluginObservationExceptionHandler;
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler;
import org.eclipse.keyple.core.service.exception.KeypleAllocationNoReaderException;
import org.eclipse.keyple.core.util.Assert;

/** Implementation of {@link StubPoolPlugin} This class uses internally @{@link StubPluginImpl}. */
final class StubPoolPluginImpl implements StubPoolPlugin {

  StubPluginImpl stubPlugin;
  Map<String, String> poolReaders; // map of readername and its associared groupReference
  List<String> allocatedReaders; // list of allocated readers : readerName

  public StubPoolPluginImpl(
      String pluginName,
      PluginObservationExceptionHandler pluginObservationExceptionHandler,
      ReaderObservationExceptionHandler readerObservationExceptionHandler) {
    // create an embedded stubplugin to manage reader
    this.stubPlugin =
        (StubPluginImpl)
            new StubPluginFactory(
                    pluginName,
                    pluginObservationExceptionHandler,
                    readerObservationExceptionHandler)
                .getPlugin();
    this.poolReaders = new HashMap<String, String>();
    this.allocatedReaders = new ArrayList<String>();
  }

  @Override
  public String getName() {
    return stubPlugin.getName();
  }

  @Override
  public SortedSet<String> getReaderGroupReferences() {
    return new TreeSet<String>(poolReaders.values());
  }

  @Override
  public Reader plugStubPoolReader(String groupReference, String readerName, StubSmartCard card) {
    Assert.getInstance()
        .notNull(groupReference, "group reference")
        .notNull(readerName, "reader name");

    // create new reader
    stubPlugin.plugStubReader(readerName, true);

    // get new reader
    StubReaderImpl newReader = (StubReaderImpl) stubPlugin.getReader(readerName);

    if (card != null) {
      newReader.insertCard(card);
    }

    // map reader to groupReference
    poolReaders.put(newReader.getName(), groupReference);

    return newReader;
  }

  @Override
  public void unplugStubPoolReadersByGroupReference(String aGroupReference) {
    Assert.getInstance().notNull(aGroupReference, "group reference");

    // find the reader in the readerPool
    List<String> readerNames = listReadersByGroup(aGroupReference);
    for (String readerName : readerNames) {
      unplugStubPoolReaderByName(readerName);
    }
  }

  @Override
  public void unplugStubPoolReaderByName(String readerName) {
    Assert.getInstance().notNull(readerName, "reader name");

    // remove reader from pool
    poolReaders.remove(readerName);

    // remove reader from allocate list
    allocatedReaders.remove(readerName);

    // remove reader from plugin
    stubPlugin.unplugStubReader(readerName, true);
  }

  /**
   * Allocate a reader if available by groupReference
   *
   * @param aGroupReference the reference of the group to which the reader belongs (may be null
   *     depending on the implementation made)
   * @return reader if available, null otherwise
   * @throws KeypleAllocationNoReaderException if the allocation failed due to lack of available
   *     reader
   */
  @Override
  public Reader allocateReader(String aGroupReference) {
    Assert.getInstance().notNull(aGroupReference, "group reference");

    List<String> readerNames = listReadersByGroup(aGroupReference);

    for (String readerName : readerNames) {
      if (!allocatedReaders.contains(readerName)) {
        allocatedReaders.add(readerName);
        return stubPlugin.getReader(readerName);
      }
    }

    throw new KeypleAllocationNoReaderException(
        "No reader is available in the groupReference : " + aGroupReference);
  }

  /**
   * Release a reader
   *
   * @param reader the Reader to be released.
   */
  @Override
  public void releaseReader(Reader reader) {
    Assert.getInstance().notNull(reader, "reader");

    if (!(reader instanceof StubReaderImpl)) {
      throw new IllegalArgumentException(
          "Can not release reader, Reader should be of type StubReader");
    }

    /** Remove and Re-insert card to reset logical channel */
    StubReaderImpl stubReader = ((StubReaderImpl) reader);
    if (stubReader.checkCardPresence()) {
      StubSmartCard card = stubReader.getSmartcard();
      stubReader.removeCard();
      stubReader.insertCard(card);
    }

    allocatedReaders.remove(reader.getName());
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

  /**
   * (package-private)
   *
   * @param readerName
   * @return true if reader is allocated
   */
  Boolean isAllocated(String readerName) {
    return allocatedReaders.contains(readerName);
  }

  List<String> listReadersByGroup(String aGroupReference) {
    List<String> readers = new ArrayList<String>();
    // find the reader in the readerPool
    for (Map.Entry<String, String> entry : poolReaders.entrySet()) {
      String readerName = entry.getKey();
      String groupReference = entry.getValue();
      if (groupReference.equals(aGroupReference)) {
        readers.add(readerName);
      }
    }
    return readers;
  }
}
