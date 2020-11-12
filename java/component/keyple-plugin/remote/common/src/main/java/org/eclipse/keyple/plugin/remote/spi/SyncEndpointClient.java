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
package org.eclipse.keyple.plugin.remote.spi;

import java.util.List;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.SyncNodeClient;

/**
 * <b>Client Sync Endpoint</b> endpoint API to be implemented by the user.
 *
 * <p>This interface must be implemented by a user client endpoint if you want to use a
 * Client-Server communication protocol, such as standard HTTP for example.
 *
 * <p>This endpoint must interact only with a remote controller.
 *
 * @since 1.0
 */
public interface SyncEndpointClient {

  /**
   * This method is called by {@link SyncNodeClient} to send a {@link MessageDto} to the server.<br>
   * You have to serialize and send the provided {@link MessageDto} to the server, then retry the
   * response which is a list of {@link MessageDto}.
   *
   * @param msg The message to send.
   * @return a null or empty list if there is no result.
   * @since 1.0
   */
  List<MessageDto> sendRequest(MessageDto msg);
}
