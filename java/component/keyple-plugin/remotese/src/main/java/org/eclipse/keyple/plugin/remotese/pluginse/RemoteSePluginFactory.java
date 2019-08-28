package org.eclipse.keyple.plugin.remotese.pluginse;

import org.eclipse.keyple.core.seproxy.PluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;

public class RemoteSePluginFactory extends PluginFactory {

    VirtualReaderSessionFactory sessionManager;
    DtoSender dtoSender;
    long rpc_timeout;
    String pluginName;

    public RemoteSePluginFactory(VirtualReaderSessionFactory sessionManager, DtoSender dtoSender, long rpc_timeout, String pluginName) {
        this.sessionManager = sessionManager;
        this.dtoSender = dtoSender;
        this.rpc_timeout = rpc_timeout;
        this.pluginName = pluginName;
    }

    @Override
    protected String getPluginName() {
        return pluginName;
    }

    @Override
    protected ReaderPlugin getPluginInstance() {
        return new RemoteSePluginImpl(sessionManager, dtoSender, rpc_timeout,
                RemoteSePluginImpl.DEFAULT_PLUGIN_NAME);
    }
}
