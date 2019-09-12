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

import org.eclipse.keyple.core.seproxy.AbstractPluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstanciationException;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;

/**
 * Only used by MasterAPI
 */
class RemoteSePluginFactory extends AbstractPluginFactory {

    VirtualReaderSessionFactory sessionManager;
    DtoSender dtoSender;
    long rpc_timeout;
    String pluginName;

    /**
     * used only by MasterAPI
     * 
     * @param sessionManager
     * @param dtoSender
     * @param rpc_timeout
     * @param pluginName
     */
    RemoteSePluginFactory(VirtualReaderSessionFactory sessionManager, DtoSender dtoSender,
            long rpc_timeout, String pluginName) {
        this.sessionManager = sessionManager;
        this.dtoSender = dtoSender;
        this.rpc_timeout = rpc_timeout;
        this.pluginName = pluginName;
    }

    @Override
    public String getPluginName() {
        return pluginName;
    }

    @Override
    protected ReaderPlugin getPluginInstance() throws KeyplePluginInstanciationException {
        if (dtoSender == null) {
            throw new KeyplePluginInstanciationException("Dto sender must not be null");
        }
        if (sessionManager == null) {
            throw new KeyplePluginInstanciationException(
                    "VirtualReaderSessionFactory must not be null");
        }
        return new RemoteSePluginImpl(sessionManager, dtoSender, rpc_timeout, pluginName);
    }
}
