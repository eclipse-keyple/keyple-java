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
package org.eclipse.keyple.core.reader.plugin;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.keyple.core.reader.Plugin;
import org.eclipse.keyple.core.reader.Reader;
import org.eclipse.keyple.core.reader.exception.KeypleReaderException;
import org.eclipse.keyple.core.reader.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.reader.exception.KeypleReaderNotFoundException;

/** Observable plugin. These plugin can report when a reader is added or removed. */
public abstract class AbstractPlugin implements Plugin {

  /** The name of the plugin */
  private final String name;

  /** The list of readers */
  protected Map<String, Reader> readers = new ConcurrentHashMap<String, Reader>();

  /**
   * Instantiates a new Plugin. Retrieve the current readers list.
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
  public final Map<String, Reader> getReaders() {
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
   * {@link Reader} whith their name as key.
   *
   * <p>{@link Reader} are new instances.
   *
   * <p>this method is called once in the plugin constructor.
   *
   * @return the map of AbstractReader objects.
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   */
  protected abstract Map<String, Reader> initNativeReaders();

  /**
   * Gets a specific reader designated by its name in the current readers list
   *
   * @param name of the reader
   * @return the reader
   * @throws KeypleReaderNotFoundException if the wanted reader is not found
   */
  @Override
  public final Reader getReader(String name) {
    Reader reader = readers.get(name);
    if (reader == null) {
      throw new KeypleReaderNotFoundException(name);
    }
    return reader;
  }
}
