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
package org.eclipse.keyple.example.generic.remote.poolclient.webservice.client;

import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.eclipse.keyple.example.generic.remote.poolclient.webservice.server.EndpointServer;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.spi.SyncEndpointClient;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Example of a {@link SyncEndpointClient} implementation using Web Services.
 *
 * <p>Sends requests to the {@link EndpointServer}.
 */
@RegisterRestClient(configKey = "pool-local-service-api")
public interface EndpointClient extends SyncEndpointClient {

  @POST
  @Path("/pool-local-service")
  @Produces("application/json")
  @Override
  List<MessageDto> sendRequest(MessageDto messageDto);
}
