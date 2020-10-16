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
package org.eclipse.keyple.core.reader.exception;

import org.eclipse.keyple.core.reader.SmartCardService;

/**
 * The exception {@code KeyplePluginNotFoundException} indicates that the plugin is not found,
 * generally when it has not been previously registered to the {@link SmartCardService}.
 */
public class KeyplePluginNotFoundException extends KeyplePluginException {

  /** @param pluginName the name of the plugin that has not been found */
  public KeyplePluginNotFoundException(String pluginName) {
    super("Plugin with name " + pluginName + " was not found");
  }
}
