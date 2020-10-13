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
package org.eclipse.keyple.core.seproxy;

import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException;

/** Plugin Factory interface. */
public interface PluginFactory {

  /**
   * Retrieve the name of the plugin that will be instantiated by this factory (can be static or
   * dynamic)
   *
   * @return pluginName
   */
  String getPluginName();

  /**
   * Retrieve an instance of a plugin (can be a singleton or not)
   *
   * @return instance of a ReaderPlugin
   * @throws KeyplePluginInstantiationException if instantiation failed, mostly when the third party
   *     library which manages the card Reader interface is not ready
   */
  ReaderPlugin getPlugin();
}
