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
 * Utility class of the {@link LocalServiceClient}.
 *
 * @since 1.0
 */
public final class LocalServiceClientUtils {

  /**
   * Gets the service having the default name.
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized.
   * @since 1.0
   */
  public static LocalServiceClient getLocalService() {
    return getServiceImpl();
  }

  /**
   * Gets the {@link AsyncNodeClient} node associated to the service having the default name.
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized or is not configured with a
   *     {@link AsyncNodeClient} node.
   * @since 1.0
   */
  public static AsyncNodeClient getAsyncNode() {
    LocalServiceClientImpl service = getServiceImpl();
    if (service.node instanceof AsyncNodeClient) {
      return (AsyncNodeClient) service.node;
    }
    throw new IllegalStateException(
        "The LocalServiceClient is not configured with a AsyncNodeClient");
  }

  /**
   * (private)<br>
   * Gets the service implementation.
   *
   * @return a not null reference
   * @throws IllegalStateException if the service is not initialized.
   */
  private static LocalServiceClientImpl getServiceImpl() {
    LocalServiceClientImpl service = LocalServiceClientImpl.getInstance();
    if (service == null) {
      throw new IllegalStateException("The LocalServiceClient is not initialized");
    }
    return service;
  }
}
