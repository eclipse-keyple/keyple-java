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

import org.eclipse.keyple.plugin.remote.AsyncNodeClient;
import org.eclipse.keyple.plugin.remote.LocalServiceClient;

/**
 * Utility class associated to a {@link LocalServiceClient}
 *
 * @since 1.0
 */
public class LocalServiceClientUtils {

  /**
   * Get the Local Service Client
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized.
   * @since 1.0
   */
  public static LocalServiceClient getLocalService() {
    return getClientService();
  }

  /**
   * Get the async node associated to the Local Service Client.
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized or is not bounded to an async
   *     node.
   * @since 1.0
   */
  public static AsyncNodeClient getAsyncNode() {
    LocalServiceClientImpl service = getClientService();
    if (service.getNode() instanceof AsyncNodeClient) {
      return (AsyncNodeClient) service.getNode();
    }
    throw new IllegalStateException("The Local Service is not bounded to an async node");
  }

  /**
   * (private)<br>
   * Get the Local Service Client implementation
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized.
   */
  private static LocalServiceClientImpl getClientService() {
    LocalServiceClientImpl service = LocalServiceClientImpl.getInstance();
    if (service == null) {
      throw new IllegalStateException("The Local Client Service is not initialized");
    }
    return service;
  }
}
