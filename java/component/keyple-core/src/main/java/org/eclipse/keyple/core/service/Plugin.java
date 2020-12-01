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
package org.eclipse.keyple.core.service;

import java.util.Map;
import java.util.Set;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;

/**
 * Defines the high level plugin API.
 *
 * <p>Provides methods to get the plugin name and to retrieve the readers.
 *
 * @since 0.9
 */
public interface Plugin {

  /**
   * Gets the name of the plugin
   *
   * @return A not empty string.
   * @since 0.9
   */
  String getName();

  /**
   * Gets the list of names of all readers
   *
   * @return a list of String
   * @throws IllegalStateException is called when plugin is no longer registered
   * @since 0.9
   */
  Set<String> getReaderNames();

  /**
   * Gets the readers.
   *
   * @return the map of this plugin's connected reader's name and instance, can be an empty list,
   *     can not be null;
   * @throws IllegalStateException is called when plugin is no longer registered
   * @since 0.9
   */
  Map<String, Reader> getReaders();

  /**
   * Gets the reader whose name is provided as an argument
   *
   * @param name of the reader
   * @return the Reader object.
   * @throws KeypleReaderNotFoundException if the wanted reader is not found
   * @throws IllegalStateException is called when plugin is no longer registered
   * @since 0.9
   */
  Reader getReader(String name);
}
