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
package org.eclipse.keyple.plugin.android.nfc;

import org.eclipse.keyple.core.seproxy.PluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;

public class AndroidNfcPluginFactory implements PluginFactory {

    /**
     * singleton instance of AndroidNfcPluginFactory
     */
    private static volatile AndroidNfcPluginFactory uniqueInstance = new AndroidNfcPluginFactory();

    /**
     * unique instance of PcscPlugin
     */
    private static AndroidNfcPlugin androidNfcPluginUniqueInstance = null;

    /**
     * Private constructor
     */
    private AndroidNfcPluginFactory() {}

    /**
     * Gets the single instance of {@link AndroidNfcPluginFactory}.
     * <p>
     * Creates the {@link AndroidNfcPlugin} unique instance if not already created.
     *
     * @return single instance of {@link AndroidNfcPluginFactory}
     */
    public static AndroidNfcPluginFactory getInstance() {
        if (androidNfcPluginUniqueInstance == null) {
            androidNfcPluginUniqueInstance = new AndroidNfcPlugin();
        }
        return uniqueInstance;
    }


    /**
     * Get the AndroidNfcPlugin instance casted to ReaderPlugin
     *
     * @return the ReaderPlugin
     */
    public ReaderPlugin getPluginInstance() {
        return (ReaderPlugin) androidNfcPluginUniqueInstance;
    }
}
