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

/**
 * The purpose of PcscPluginFactory singleton class is to manage the creation of the PcscPlugin
 * unique instance and to provide the corresponding ReaderPlugin instance.
 * <p>
 * Thus, the internal methods of PcscPlugin are hidden from the point of view of the calling
 * application.
 */
public final class PcscPluginFactory implements PluginFactory {

    /**
     * singleton instance of PcscPluginFactory
     */
    private static final PcscPluginFactory uniqueInstance = new PcscPluginFactory();

    /**
     * unique instance of PcscPlugin
     */
    private static PcscPlugin pcscPluginUniqueInstance = null;

    /**
     * Gets the single instance of {@link PcscPluginFactory}.
     *
     * @return single instance of {@link PcscPluginFactory}
     */
    public static PcscPluginFactory getInstance() {
        return uniqueInstance;
    }


    /**
     * Get the PcscPlugin instance casted to ReaderPlugin
     * 
     * @return the ReaderPlugin
     */
    public ReaderPlugin getPluginInstance() {
        if (pcscPluginUniqueInstance == null) {
            pcscPluginUniqueInstance = new PcscPlugin();
        }
        return (ReaderPlugin) pcscPluginUniqueInstance;
    }
}
