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


import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseStubTest {

    StubPluginImpl stubPlugin;

    private static final Logger logger = LoggerFactory.getLogger(BaseStubTest.class);

    public static String PLUGIN_NAME = "stub1";

    @Rule
    public TestName name = new TestName();

    public void setupStub() throws Exception {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName());
        logger.info("------------------------------");

        logger.info("setupStub, assert stubplugin is empty");
        stubPlugin = (StubPluginImpl) new StubPluginFactory(PLUGIN_NAME).getPluginInstance();

        logger.info("Stubplugin readers size {}", stubPlugin.getReaders().size());
        Assert.assertEquals(0, stubPlugin.getReaders().size());

        logger.info("Stubplugin observers size {}", stubPlugin.countObservers());
        Assert.assertEquals(0, stubPlugin.countObservers());

        // add a sleep to play with thread monitor timeout
        Thread.sleep(100);

    }

    public void clearStub()
            throws InterruptedException, KeypleReaderException, KeyplePluginNotFoundException {
        logger.info("---------");
        logger.info("TearDown ");
        logger.info("---------");

        stubPlugin.unplugStubReaders(stubPlugin.getReaderNames(), true);

        stubPlugin.clearObservers();

    }

}
