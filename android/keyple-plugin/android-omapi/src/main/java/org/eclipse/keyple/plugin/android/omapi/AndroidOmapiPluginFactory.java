/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.android.omapi;

import org.eclipse.keyple.core.seproxy.ReaderPlugin;

public class AndroidOmapiPluginFactory {
    /**
     * singleton instance of AndroidNfcPluginFactory
     */
    private static volatile AndroidOmapiPluginFactory uniqueInstance = new AndroidOmapiPluginFactory();

    /**
     * unique instance of PcscPlugin
     */
    private static AndroidOmapiPlugin androidNfcPluginUniqueInstance = null;

    /**
     * Private constructor
     */
    private AndroidOmapiPluginFactory() {}

    /**
     * Gets the single instance of {@link AndroidOmapiPluginFactory}.
     * <p>
     * Creates the {@link AndroidOmapiPlugin} unique instance if not already created.
     *
     * @return single instance of {@link AndroidOmapiPluginFactory}
     */
    public static AndroidOmapiPluginFactory getInstance() {
        if (androidNfcPluginUniqueInstance == null) {
            androidNfcPluginUniqueInstance = new AndroidOmapiPlugin();
        }
        return uniqueInstance;
    }


    /**
     * Get the AndroidOmapiPlugin instance casted to ReaderPlugin
     *
     * @return the ReaderPlugin
     */
    public ReaderPlugin getPluginInstance() {
        return (ReaderPlugin) androidNfcPluginUniqueInstance;
    }
}