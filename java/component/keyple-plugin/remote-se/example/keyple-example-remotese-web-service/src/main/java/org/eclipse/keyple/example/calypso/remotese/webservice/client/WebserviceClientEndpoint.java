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
package org.eclipse.keyple.example.calypso.remotese.webservice.client;

import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Example implementation of a {@link KeypleClientAsync} based on Web service. Interacts with {@link
 * org.eclipse.keyple.example.calypso.remotese.webservice.server.WebserviceServerEndpoint}
 */
@RegisterRestClient(configKey = "remotese-plugin-api")
public interface WebserviceClientEndpoint extends KeypleClientSync {

  @POST
  @Path("/remotese-plugin")
  @Produces("application/json")
  @Override
  public List<KeypleMessageDto> sendRequest(KeypleMessageDto keypleMessageDto);
}
