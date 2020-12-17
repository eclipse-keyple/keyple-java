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
package org.eclipse.keyple.example.generic.distributed.server.webservice.client;

import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.eclipse.keyple.distributed.MessageDto;
import org.eclipse.keyple.distributed.spi.SyncEndpointClient;
import org.eclipse.keyple.example.generic.distributed.server.webservice.server.EndpointServer;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Example of a {@link SyncEndpointClient} implementation using Web Services.
 *
 * <p>Sends requests to the {@link EndpointServer}.
 */
@RegisterRestClient(configKey = "remote-plugin-api")
public interface EndpointClient extends SyncEndpointClient {

  @POST
  @Path("/remote-plugin")
  @Produces("application/json")
  @Override
  List<MessageDto> sendRequest(MessageDto messageDto);
}
