package org.eclipse.keyple.example.calypso.remotese.webservice.server;

import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.util.NamedThreadFactory;
import org.eclipse.keyple.plugin.remotese.virtualse.impl.RemoteSeServerPluginFactory;
import org.eclipse.keyple.remotese.example.app.RemoteSePluginObserver;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.Executors;

/**
 * Example of a server side app
 */
@ApplicationScoped
public class ServerApp {

    RemoteSePluginObserver pluginObserver;

    /**
     * Initialize the Remote SE Plugin wit a sync node and attach an observer to the plugin {@link RemoteSePluginObserver} that contains all the business logic
     */
    public void init() {

        pluginObserver = new RemoteSePluginObserver();

        SeProxyService.getInstance()
                .registerPlugin(
                        RemoteSeServerPluginFactory.builder()
                                .withSyncNode()
                                .withPluginObserver(pluginObserver)
                                .usingEventNotificationPool(Executors.newCachedThreadPool(new NamedThreadFactory("server-pool")))
                                .build());

    }
}