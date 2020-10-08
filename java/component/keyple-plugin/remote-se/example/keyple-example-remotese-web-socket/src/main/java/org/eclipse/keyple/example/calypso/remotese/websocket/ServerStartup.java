package org.eclipse.keyple.example.calypso.remotese.websocket;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.eclipse.keyple.example.calypso.remotese.websocket.client.ClientApp;
import org.eclipse.keyple.example.calypso.remotese.websocket.server.ServerApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@QuarkusMain
public class ServerStartup {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStartup.class);

    public static void main(String... args) {
        Quarkus.run(RemoteSeWebsocketExample.class, args);
    }

    /*
     * Main class of the server application.
     */
    public static class RemoteSeWebsocketExample implements QuarkusApplication {

        @Inject
        ServerApp serverApp;

        @Inject
        ClientApp clientApp;

        @Override
        public int run(String... args) throws Exception {

            LOGGER.info("Server app init ...");

            serverApp.init();

            LOGGER.info("Client init...");

            clientApp.init();

            LOGGER.info("Launch client scenario...");

            Boolean isSuccessful = clientApp.launchScenario();

            LOGGER.info("Is scenario successful - {}", isSuccessful);

            //Quarkus.waitForExit(); close jvm after scenario
            return 0;
        }
    }
}
