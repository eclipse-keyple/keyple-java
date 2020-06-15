/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.pluginse;

import java.util.concurrent.ExecutorService;
import org.eclipse.keyple.core.seproxy.PluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;

/**
 * Only used by MasterAPI
 */
class RemoteSePoolPluginFactory implements PluginFactory {

    VirtualReaderSessionFactory sessionManager;
    DtoSender dtoSender;
    long rpc_timeout;
    String pluginName;
    ExecutorService executorService;

    RemoteSePoolPluginFactory(VirtualReaderSessionFactory sessionManager, DtoSender dtoSender,
            long rpc_timeout, String pluginName, ExecutorService executorService) {
        this.sessionManager = sessionManager;
        this.dtoSender = dtoSender;
        this.rpc_timeout = rpc_timeout;
        this.pluginName = pluginName;
        this.executorService = executorService;
    }

    @Override
    public String getPluginName() {
        return pluginName;
    }

    @Override
    public ReaderPlugin getPluginInstance() {
        try {
            return new RemoteSePoolPluginImpl(sessionManager, dtoSender, rpc_timeout, pluginName,
                    executorService);
        } catch (KeypleReaderException e) {
            throw new KeyplePluginInstantiationException("Can not access RemoteSePool", e);
        }
    }
}
