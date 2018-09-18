package org.eclipse.keyple.plugin.remote_se.rse;

import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;

public class VirtualSeRemoteService  {

    private RsePlugin plugin;
    private TransportNode node;

    public VirtualSeRemoteService(TransportNode node){
        this.node = node;
    }

    public void connectPlugin(RsePlugin plugin){
        this.node.setDtoReceiver(plugin);
    }


}
