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
package org.eclipse.keyple.core.seproxy.plugin;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.*;

/** Observable plugin. These plugin can report when a reader is added or removed. */
public abstract class AbstractPlugin implements ReaderPlugin {

  /** The name of the plugin */
  private final String name;

  /** The list of readers */
  protected ConcurrentMap<String, SeReader> readers = new ConcurrentHashMap<String, SeReader>();

  /**
   * Instantiates a new ReaderPlugin. Retrieve the current readers list.
   *
   * <p>Initialize the list of readers calling the abstract method initNativeReaders
   *
   * <p>When readers initialisation failed, a KeypleReaderException is thrown
   *
   * @param name name of the plugin
   * @throws KeypleReaderException when an issue is raised with reader
   */
  protected AbstractPlugin(String name) {
    this.name = name;
    readers.putAll(initNativeReaders());
  }

  /** @return the name of the plugin */
  public final String getName() {
    return name;
  }

  /**
   * Returns the current readers name instance map.
   *
   * <p>The map is initialized in the constructor and may be updated in background in the case of a
   * threaded plugin {@link AbstractThreadedObservablePlugin}
   *
   * @return the current readers map, can be an empty
   */
  @Override
  public final ConcurrentMap<String, SeReader> getReaders() {
    return readers;
  }

  /**
   * Returns the current list of reader names.
   *
   * <p>The list of names is built from the current readers list
   *
   * @return a list of String
   */
  @Override
  public final Set<String> getReaderNames() {
    return readers.keySet();
  }

  /**
   * Init connected native readers (from third party library) and returns a map of corresponding
   * {@link SeReader} whith their name as key.
   *
   * <p>{@link SeReader} are new instances.
   *
   * <p>this method is called once in the plugin constructor.
   *
   * @return the map of AbstractReader objects.
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  protected abstract ConcurrentMap<String, SeReader> initNativeReaders()
      throws KeypleReaderIOException;

  /**
   * Gets a specific reader designated by its name in the current readers list
   *
   * @param name of the reader
   * @return the reader
   * @throws KeypleReaderNotFoundException if the wanted reader is not found
   */
  @Override
  public final SeReader getReader(String name) {
    SeReader seReader = readers.get(name);
    if (seReader == null) {
      throw new KeypleReaderNotFoundException(name);
    }
    return seReader;
  }

  /**
   * Sets at once a set of parameters for the plugin
   *
   * <p>See {@link #setParameter(String, String)} for more details
   *
   * @param parameters a Map &lt;String, String&gt; parameter set
   * @throws KeypleException if one of the parameters could not be set up
   */
  public final void setParameters(Map<String, String> parameters) {
    for (Map.Entry<String, String> en : parameters.entrySet()) {
      setParameter(en.getKey(), en.getValue());
    }
  }
}
