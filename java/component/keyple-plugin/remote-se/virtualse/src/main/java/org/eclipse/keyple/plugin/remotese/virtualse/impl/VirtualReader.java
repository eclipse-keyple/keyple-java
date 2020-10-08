/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.virtualse.impl;

import org.eclipse.keyple.plugin.remotese.core.impl.AbstractKeypleNode;

/**
 * (package-private)<br>
 * Virtual Reader
 */
final class VirtualReader extends AbstractVirtualReader {

  /**
   * (package-private)<br>
   * Constructor
   *
   * @param pluginName The name of the plugin (must be not null).
   * @param nativeReaderName The name of the native reader (must be not null).
   * @param node The associated node (must be not null).
   * @param sessionId Session Id (can be null)
   * @param clientNodeId Associated client node Id (can be null)
   */
  VirtualReader(
      String pluginName,
      String nativeReaderName,
      AbstractKeypleNode node,
      String sessionId,
      String clientNodeId) {
    super(pluginName, nativeReaderName, node, sessionId, clientNodeId);
  }

}
