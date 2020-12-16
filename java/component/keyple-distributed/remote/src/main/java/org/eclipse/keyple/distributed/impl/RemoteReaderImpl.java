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
package org.eclipse.keyple.distributed.impl;

/**
 * (package-private)<br>
 * Remote Reader
 *
 * @since 1.0
 */
final class RemoteReaderImpl extends AbstractRemoteReader {

  /**
   * (package-private)<br>
   *
   * @param pluginName The name of the plugin (must be not null).
   * @param localReaderName The name of the local reader (must be not null).
   * @param node The associated node (must be not null).
   * @param sessionId Session Id (can be null)
   * @param clientNodeId Associated client node Id (can be null)
   * @since 1.0
   */
  RemoteReaderImpl(
      String pluginName,
      String localReaderName,
      AbstractNode node,
      String sessionId,
      String clientNodeId) {
    super(pluginName, localReaderName, node, sessionId, clientNodeId);
  }
}
