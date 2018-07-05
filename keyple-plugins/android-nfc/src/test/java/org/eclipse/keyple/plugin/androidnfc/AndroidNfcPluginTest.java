/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.androidnfc;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
public class AndroidNfcPluginTest {

    private static final ILogger logger = SLoggerFactory.getLogger(AndroidNfcPluginTest.class);

    AndroidNfcPlugin plugin;


    @Before
    public void SetUp() throws IOException {
        plugin = AndroidNfcPlugin.getInstance();
        plugin.setParameters(new HashMap<String, String>());// re-init parameters
    }



    /*
     * TEST PUBLIC METHODS
     */



    @Test
    public void getInstance() throws IOException {
        Assert.assertTrue(plugin != null);
    }


    @Test
    public void getParameters() throws IOException {
        Assert.assertTrue(plugin.getParameters() != null);
        Assert.assertTrue(plugin.getParameters().size() == 0);
    }


    @Test
    public void setParameter() throws IOException {
        plugin.setParameter("key", "value");
        Assert.assertTrue(plugin.getParameters().size() == 1);
        Assert.assertTrue(plugin.getParameters().get("key").equals("value"));
    }

    @Test
    public void getReaders() throws IOException {
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
        assertThat(plugin.getNativeReader(AndroidNfcReader.TAG),
                instanceOf(AndroidNfcReader.class));
    }

    @Test
    public void getNativeReaders() throws IOException {
        Assert.assertTrue(plugin.getNativeReaders().size() == 1);
        assertThat(plugin.getNativeReaders().first(), instanceOf(AndroidNfcReader.class));
    }



}
