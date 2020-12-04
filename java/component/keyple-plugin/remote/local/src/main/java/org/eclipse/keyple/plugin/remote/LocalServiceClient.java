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
package org.eclipse.keyple.plugin.remote;

import org.eclipse.keyple.plugin.remote.impl.LocalServiceClientFactory;
import org.eclipse.keyple.plugin.remote.impl.LocalServiceClientUtils;

/**
 * API of the <b>Local Service Client</b> associated to the <b>Remote Plugin Server</b>.
 *
 * <p>This service must be started by the application installed on a <b>Client</b> having local
 * access to the smart card reader but wishes to delegate all or part of the ticketing processing to
 * a remote application :
 *
 * <ul>
 *   <li>To <b>start</b> the service, use the factory {@link LocalServiceClientFactory}.
 *   <li>To <b>access</b> the service, use the utility method {@link
 *       LocalServiceClientUtils#getLocalService()}.
 *   <li>To <b>stop</b> the service, there is nothing special to do because the service is a
 *       standard java singleton instance.
 * </ul>
 *
 * @since 1.0
 */
public interface LocalServiceClient {

  /**
   * Allows you to connect a local card reader to a remote server and execute a specific ticketing
   * service from the server.
   *
   * <p>The service is identify by the <b>serviceId</b> parameter.
   *
   * @param parameters The service parameters (serviceId, ...) (see {@link RemoteServiceParameters}
   *     documentation for all possible parameters).
   * @param classOfT The class of the expected user output data.
   * @param <T> The generic type of the expected user output data.
   * @return a new instance of <b>T</b>.
   * @since 1.0
   */
  <T> T executeRemoteService(RemoteServiceParameters parameters, Class<T> classOfT);
}
