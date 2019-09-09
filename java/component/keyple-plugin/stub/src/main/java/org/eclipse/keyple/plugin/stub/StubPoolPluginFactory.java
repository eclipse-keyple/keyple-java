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
package org.eclipse.keyple.plugin.stub;

import org.eclipse.keyple.core.seproxy.AbstractPluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginException;
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstanciationException;

/**
 * Build a StubPoolPlugin Factory.
 * StubPoolPlugin embeds a StubPlugin, then a StubPluginFactory is required.
 */
public class StubPoolPluginFactory extends AbstractPluginFactory {

    StubPluginFactory stubPluginFactory;

    /**
     * Instantiate with a StubPluginFactory
     * @param stubPluginFactory
     */
    public StubPoolPluginFactory(StubPluginFactory stubPluginFactory) {
        this.stubPluginFactory = stubPluginFactory;
    }

    @Override
    public String getPluginName() {
        return StubPoolPlugin.PLUGIN_NAME;
    }

    @Override
    protected ReaderPlugin getPluginInstance() throws KeyplePluginInstanciationException {
        if(stubPluginFactory==null){
            throw new KeyplePluginInstanciationException("stubPluginFactory must not be null");
        }
        return new StubPoolPluginImpl((StubPluginImpl) stubPluginFactory.getPluginInstance());
    }
}
