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
package org.eclipse.keyple.plugin.pcsc;

import org.eclipse.keyple.core.seproxy.PluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException;

/**
 * Builds a {@link PcscPlugin}
 */
public class PcscPluginFactory implements PluginFactory {

    @Override
    public String getPluginName() {
        return PcscPlugin.PLUGIN_NAME;
    }

    /**
     * Returns an instance of the {@link PcscPlugin} if the platform is ready
     * 
     * @return PcscPlugin instance
     * @throws KeyplePluginInstantiationException if Smartcard.io library is not ready
     */
    public ReaderPlugin getPlugin() {
        try {
            return PcscPluginImpl.getInstance();
        } catch (Exception e) {
            throw new KeyplePluginInstantiationException(
                    "Can not access Smartcard.io readers, check createVirtualReader trace", e);
        }
    }
}
