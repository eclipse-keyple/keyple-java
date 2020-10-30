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

/** Card readers plugin interface. */
public interface Plugin extends ProxyElement {

  /**
   * Gets the list of names of all readers
   *
   * @return a list of String
   */
  Set<String> getReaderNames();

  /**
   * Gets the readers.
   *
   * @return the map of this plugin's connected reader's name and instance, can be an empty list,
   *     can not be null;
   */
  Map<String, Reader> getReaders();

  /**
   * Gets the reader whose name is provided as an argument
   *
   * @param name of the reader
   * @return the Reader object.
   * @throws KeypleReaderNotFoundException if the wanted reader is not found
   */
  Reader getReader(String name);
}
