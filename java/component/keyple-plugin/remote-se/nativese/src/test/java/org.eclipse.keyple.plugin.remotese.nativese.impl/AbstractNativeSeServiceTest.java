/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.nativese.impl;

import static org.mockito.Mockito.doReturn;
import org.eclipse.keyple.core.seproxy.PluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class AbstractNativeSeServiceTest {


    private static final Logger logger = LoggerFactory.getLogger(AbstractNativeSeServiceTest.class);


    /**
     * Find local reader among plugin no plugin
     * 
     * @throws Exception
     */
    @Test(expected = KeypleReaderNotFoundException.class)
    public void findLocalReader_notFound() throws Exception {
        // init
        AbstractNativeSeService abstractNativeSeService =
                Mockito.spy(AbstractNativeSeService.class);
        // execute
        abstractNativeSeService.findLocalReader("test");
        // should throw exception
    }

    /**
     * Find local reader among plugin : reader found
     * 
     * @throws Exception
     */
    @Test
    public void findLocalReader_Found() throws Exception {
        // init
        AbstractNativeSeService abstractNativeSeService =
                Mockito.spy(AbstractNativeSeService.class);
        SeProxyService.getInstance().registerPlugin(mockPluginFactory());
        // execute
        SeReader seReader = abstractNativeSeService.findLocalReader("test");
        // results
        Assert.assertNotNull(seReader);
        SeProxyService.getInstance().unregisterPlugin("mockPlugin");
    }


    /*
     * helpers
     */

    public PluginFactory mockPluginFactory() {
        PluginFactory mockFactory = Mockito.mock(PluginFactory.class);
        ReaderPlugin mockPlugin = Mockito.mock(ReaderPlugin.class);
        SeReader mockReader = Mockito.mock(SeReader.class);
        doReturn(mockPlugin).when(mockFactory).getPlugin();
        doReturn("mockPlugin").when(mockFactory).getPluginName();
        doReturn("mockPlugin").when(mockPlugin).getName();
        doReturn(mockReader).when(mockPlugin).getReader("test");
        return mockFactory;
    }
}
