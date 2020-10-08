package org.eclipse.keyple.example.calypso.remotese.webservice.client;

import org.eclipse.keyple.plugin.remotese.core.KeypleClientAsync;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

/**
 * Example implementation of a {@link KeypleClientAsync} based on Web service. Interacts with {@link org.eclipse.keyple.example.calypso.remotese.webservice.server.WebserviceServerEndpoint}
 */
@RegisterRestClient(configKey = "remotese-plugin-api")
public interface WebserviceClientEndpoint extends KeypleClientSync {

    @POST
    @Path("/remotese-plugin")
    @Produces("application/json")
    @Override
    public List<KeypleMessageDto> sendRequest(KeypleMessageDto keypleMessageDto);
}
