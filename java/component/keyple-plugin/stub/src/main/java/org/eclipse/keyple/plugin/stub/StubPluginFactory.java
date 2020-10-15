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

import org.eclipse.keyple.core.seproxy.Plugin;
import org.eclipse.keyple.core.seproxy.PluginFactory;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException;

/** Instantiate a {@link StubPlugin} with a custom plugin name */
public class StubPluginFactory implements PluginFactory {

  private String pluginName;

  /**
   * Create the factory
   *
   * @param pluginName name of the plugin that will be instantiated
   */
  public StubPluginFactory(String pluginName) {
    this.pluginName = pluginName;
  }

  @Override
  public String getPluginName() {
    return pluginName;
  }

  /** {@inheritDoc} */
  @Override
  public Plugin getPlugin() {
    try {
      return new StubPluginImpl(pluginName);
    } catch (Exception e) {
      throw new KeyplePluginInstantiationException("Can not access StubPlugin", e);
    }
  }
}
