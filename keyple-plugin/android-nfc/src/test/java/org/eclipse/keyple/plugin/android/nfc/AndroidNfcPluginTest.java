/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.plugin.android.nfc;


import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AndroidNfcReader.class})
public class AndroidNfcPluginTest {

    AndroidNfcPlugin plugin;

    // init before each test
    @Before
    public void SetUp() throws IOException {
        PowerMockito.mockStatic(AndroidNfcReader.class);
        when(AndroidNfcReader.getInstance()).thenReturn(PowerMockito.mock(AndroidNfcReader.class));

        // get unique instance
        plugin = AndroidNfcPlugin.getInstance();

        // reset parameters

    }



    /*
     * TEST PUBLIC METHODS
     */


    @Test
    public void getInstance() throws IOException {
        Assert.assertTrue(plugin != null);
    }

    @Test
    public void setParameters() throws KeypleBaseException {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key1", "value1");
        plugin.setParameters(parameters);
        Assert.assertTrue(plugin.getParameters().size() > 0);
        Assert.assertTrue(plugin.getParameters().get("key1").equals("value1"));

    }

    @Test
    public void getParameters() throws IOException {
        Assert.assertTrue(plugin.getParameters() != null);
    }


    @Test
    public void setParameter() throws IOException {
        plugin.setParameter("key2", "value2");
        Assert.assertTrue(plugin.getParameters().size() > 0);
        Assert.assertTrue(plugin.getParameters().get("key2").equals("value2"));
    }

    @Test
    public void getReaders() throws KeypleReaderException {
        Assert.assertTrue(plugin.getReaders().size() == 1);
        assertThat(plugin.getReaders().first(), instanceOf(AndroidNfcReader.class));
    }

    @Test
    public void getName() throws IOException {
        Assert.assertTrue(plugin.getName().equals(AndroidNfcPlugin.PLUGIN_NAME));
    }

    /*
     * TEST INTERNAL METHODS
     */

    @Test
    public void getNativeReader() throws IOException {
        assertThat(plugin.getNativeReader(AndroidNfcReader.READER_NAME),
                instanceOf(AndroidNfcReader.class));
    }

    @Test
    public void getNativeReaders() throws IOException {
        Assert.assertTrue(plugin.getNativeReaders().size() == 1);
        assertThat(plugin.getNativeReaders().first(), instanceOf(AndroidNfcReader.class));
    }



}
