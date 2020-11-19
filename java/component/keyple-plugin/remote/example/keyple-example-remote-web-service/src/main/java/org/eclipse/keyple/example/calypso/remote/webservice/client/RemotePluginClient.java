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
package org.eclipse.keyple.example.calypso.remote.webservice.client;

import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.eclipse.keyple.example.calypso.remote.webservice.server.RemotePluginEndpoint;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.spi.SyncEndpointClient;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Example implementation of a {@link SyncEndpointClient} based on Web service. Interacts with
 * {@link RemotePluginEndpoint}
 */
@RegisterRestClient(configKey = "remote-plugin-api")
public interface RemotePluginClient extends SyncEndpointClient {

  @POST
  @Path("/remote-plugin")
  @Produces("application/json")
  @Override
  public List<MessageDto> sendRequest(MessageDto keypleMessageDto);
}
