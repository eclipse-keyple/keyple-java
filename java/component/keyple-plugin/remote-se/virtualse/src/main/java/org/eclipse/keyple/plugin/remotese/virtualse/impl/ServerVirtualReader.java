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

/**
 * (package-private)<br>
 * Server Virtual Reader class.<br>
 * This object is a decorator of a {@link VirtualReader}.
 */
final class ServerVirtualReader extends AbstractServerVirtualReader {

  /**
   * (package-private)<br>
   * Constructor
   *
   * @param reader The reader to decorate (must be not null).
   * @param serviceId The service id (must be not null).
   * @param userInputDataJson The user input data as a JSON string (optional).
   * @param initialSeContentJson The initial SE content as a JSON string (optional).
   */
  ServerVirtualReader(
      VirtualReader reader,
      String serviceId,
      String userInputDataJson,
      String initialSeContentJson) {
    super(reader,  serviceId, userInputDataJson, initialSeContentJson);
  }
}
