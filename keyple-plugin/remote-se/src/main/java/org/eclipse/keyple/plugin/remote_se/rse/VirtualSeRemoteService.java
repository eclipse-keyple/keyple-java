/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.rse;

import java.util.SortedSet;
import org.eclipse.keyple.plugin.remote_se.transport.DtoSender;
import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;

/**
 * Service to setDtoSender a RSE Plugin to a Transport Node
 */
public class VirtualSeRemoteService {

    private DtoSender dtoSender;
    private final SeProxyService seProxyService;
    private RsePlugin plugin;

    public VirtualSeRemoteService(SeProxyService seProxyService, DtoSender dtoSender) {
        this.seProxyService = seProxyService;
        this.dtoSender = dtoSender;

        // instanciate plugin
        this.plugin = startPlugin();
    }

    public void bindDtoEndpoint(TransportNode node) {
        node.setDtoDispatcher(plugin);
    }

    public RsePlugin getPlugin() {
        return plugin;
    }

    private RsePlugin startPlugin() {
        SortedSet<ReaderPlugin> plugins = seProxyService.getPlugins();
        RsePlugin rsePlugin = new RsePlugin();
        plugins.add(rsePlugin);
        seProxyService.setPlugins(plugins);
        return rsePlugin;
    }

}
