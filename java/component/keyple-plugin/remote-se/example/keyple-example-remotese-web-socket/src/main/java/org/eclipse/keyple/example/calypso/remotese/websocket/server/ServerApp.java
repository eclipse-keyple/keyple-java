package org.eclipse.keyple.example.calypso.remotese.websocket.server;

import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.plugin.remotese.virtualse.impl.RemoteSeServerPluginFactory;
import org.eclipse.keyple.remotese.example.app.RemoteSePluginObserver;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


/**
 * Example of a server side app
 */
@ApplicationScoped
public class ServerApp {

    RemoteSePluginObserver pluginObserver;

    @Inject
    WebsocketServerEndpoint websocketServerEndpoint;


    /**
     * Initialize the Remote SE Plugin with an async endpoint {@link WebsocketServerEndpoint} and attach an observer to the plugin {@link RemoteSePluginObserver} that contains all the business logic
     */
    public void init() {

        pluginObserver = new RemoteSePluginObserver();

        SeProxyService.getInstance()
                .registerPlugin(
                        RemoteSeServerPluginFactory.builder()
                                .withAsyncNode(websocketServerEndpoint)
                                .withPluginObserver(pluginObserver)
                                .usingDefaultEventNotificationPool()
                                .build());

    }
}