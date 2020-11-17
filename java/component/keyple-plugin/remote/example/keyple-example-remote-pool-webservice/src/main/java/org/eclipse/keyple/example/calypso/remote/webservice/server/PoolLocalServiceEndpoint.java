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
import org.eclipse.keyple.example.calypso.remote.webservice.client.PoolLocalServiceClient;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.impl.PoolLocalServiceServerUtils;

/**
 * Implementation of a Pool Local Service based on Web Service. Serve {@link PoolLocalServiceClient}
 * clients
 */
@Path("/pool-local-service")
public class PoolLocalServiceEndpoint {

  /**
   * Endpoint that transfer message to the RemoteServer Sync Node
   *
   * @param message non nullable instance of a KeypleMessageDto
   * @return list of non nullable instances of KeypleMessageDto
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public List<MessageDto> add(MessageDto message) {
    return PoolLocalServiceServerUtils.getSyncNode().onRequest(message);
  }
}
