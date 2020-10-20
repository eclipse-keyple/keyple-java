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
package org.eclipse.keyple.example.calypso.remote.webservice.server;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.keyple.example.calypso.remote.webservice.client.WebserviceClientEndpoint;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.virtual.impl.RemoteServerUtils;

/**
 * Example implementation of a RemoteServer with a {@link
 * org.eclipse.keyple.plugin.remote.core.KeypleServerSyncNode} based on Web Service. Serve {@link
 * WebserviceClientEndpoint} clients
 */
@Path("/remote-plugin")
public class WebserviceServerEndpoint {

  /**
   * Endpoint that transfer message to the RemoteServer Sync Node
   *
   * @param message non nullable instance of a KeypleMessageDto
   * @return list of non nullable instances of KeypleMessageDto
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<KeypleMessageDto> add(KeypleMessageDto message) {
    return RemoteServerUtils.getSyncNode().onRequest(message);
  }
}
