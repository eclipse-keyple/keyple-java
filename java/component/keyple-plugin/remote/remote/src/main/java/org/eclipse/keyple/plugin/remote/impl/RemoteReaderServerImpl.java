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
package org.eclipse.keyple.plugin.remote.impl;

/**
 * (package-private)<br>
 * Server Remote Reader class.<br>
 * This object is a decorator of a {@link RemoteReaderImpl}.
 *
 * @since 1.0
 */
final class RemoteReaderServerImpl extends AbstractRemoteReaderServer {

  /**
   * (package-private)<br>
   *
   * @param reader The reader to decorate (must be not null).
   * @param serviceId The service id (must be not null).
   * @param userInputDataJson The user input data as a JSON string (optional).
   * @param initialCardContentJson The initial Card content as a JSON string (optional).
   * @since 1.0
   */
  RemoteReaderServerImpl(
      RemoteReaderImpl reader,
      String serviceId,
      String userInputDataJson,
      String initialCardContentJson) {
    super(reader, serviceId, userInputDataJson, initialCardContentJson);
  }
}
