package org.eclipse.keyple.example.calypso.remotese.webservice.server;

import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.virtualse.impl.RemoteSeServerUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Example implementation of a RemoteSeServer with a {@link org.eclipse.keyple.plugin.remotese.core.KeypleServerSyncNode} based on Web Service.
 * Serve {@link org.eclipse.keyple.example.calypso.remotese.webservice.client.WebserviceClientEndpoint}  clients
 */
@Path("/remotese-plugin")
public class WebserviceServerEndpoint {

    /**
     * Endpoint that transfer message to the RemoteSeServer Sync Node
     *
     * @param message non nullable instance of a KeypleMessageDto
     * @return list of non nullable instances of KeypleMessageDto
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<KeypleMessageDto> add(KeypleMessageDto message) {
        return RemoteSeServerUtils.getSyncNode().onRequest(message);
    }

}