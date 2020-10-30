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
package org.eclipse.keyple.plugin.pcsc;

import org.eclipse.keyple.core.service.PluginFactory;
import org.eclipse.keyple.core.service.exception.KeyplePluginInstantiationException;

/**
 * Provides a factory to get the {@link PcscPlugin}.
 *
 * @since 0.9
 */
public class PcscPluginFactory implements PluginFactory {

  /**
   * (package-private)<br>
   * The plugin name
   */
  static final String PLUGIN_NAME = "PcscPlugin";

  /**
   * {@inheritDoc}
   *
   * @since 0.9
   */
  @Override
  public String getPluginName() {
    return PLUGIN_NAME;
  }

  /**
   * Returns an instance of the {@link PcscPlugin} if the platform is ready
   *
   * @return A not null {@link PcscPlugin} instance.
   * @throws KeyplePluginInstantiationException if smartcard.io library is not ready
   * @since 0.9
   */
  public PcscPlugin getPlugin() {
    try {
      return PcscPluginImpl.getInstance();
    } catch (Exception e) {
      throw new KeyplePluginInstantiationException("Can not access smartcard.io readers", e);
    }
  }
}
