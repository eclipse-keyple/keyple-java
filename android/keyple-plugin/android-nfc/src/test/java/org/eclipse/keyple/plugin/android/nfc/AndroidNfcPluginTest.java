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
package org.eclipse.keyple.plugin.android.nfc;


import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
        // get unique instance
        plugin = AndroidNfcPlugin.getInstance();
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
    public void getName() throws Exception {
        Assert.assertTrue(plugin.getName().equals(AndroidNfcPlugin.PLUGIN_NAME));
    }

    /*
     * TEST INTERNAL METHODS
     */

    @Test
    public void getNativeReader() throws Exception {
        assertThat(plugin.getReader(AndroidNfcReader.READER_NAME),
                instanceOf(AndroidNfcReader.class));
    }

    @Test
    public void getNativeReaders() throws Exception {
        Assert.assertTrue(plugin.getReaders().size() == 1);
        assertThat(plugin.getReaders().first(), instanceOf(AndroidNfcReader.class));
    }



}
