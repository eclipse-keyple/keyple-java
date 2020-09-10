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
package org.eclipse.keyple.plugin.android.omapi;

import org.eclipse.keyple.core.seproxy.AbstractPluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;

/**
 * Build asynchronously the Android OMAPI plugin.
 * Platform incompabilities are not managed
 */
public final class AndroidOmapiPluginFactory extends AbstractPluginFactory {

    @Override
    public String getPluginName() {
        return AndroidOmapiPlugin.PLUGIN_NAME;
    }

    //TODO throw error is Android is not compatible with OMAPI
    @Override
    protected ReaderPlugin getPluginInstance() {
            return AndroidOmapiPluginImpl.getInstance();
    }
}
