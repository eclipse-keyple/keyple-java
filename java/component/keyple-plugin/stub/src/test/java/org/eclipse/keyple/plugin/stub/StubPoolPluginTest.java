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
package org.eclipse.keyple.plugin.stub;

import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StubPoolPluginTest extends BaseStubTest {

    public static final String POOL_PLUGIN_NAME = "pool1";

    Logger logger = LoggerFactory.getLogger(StubPoolPluginTest.class);

    @Before
    public void setupStub() throws Exception {
        super.setupStub();

    }

    @After
    public void clearStub()
            throws InterruptedException, KeypleReaderException, KeyplePluginNotFoundException {
        super.clearStub();
    }


    /**
     * Plug a pool reader
     */
    @Test
    public void plugStubPoolReader_success()
            throws KeypleReaderException, KeyplePluginInstantiationException {
        StubPoolPluginImpl stubPoolPlugin =
                (StubPoolPluginImpl) new StubPoolPluginFactory(POOL_PLUGIN_NAME)
                        .getPluginInstance();

        SeReader seReader = stubPoolPlugin.plugStubPoolReader("anyGroup", "anyName", stubSe);

        Assert.assertEquals(1, stubPoolPlugin.getReaders().size());
        Assert.assertEquals(true, seReader.isSePresent());
        Assert.assertEquals(1, stubPoolPlugin.getReaderGroupReferences().size());
    }

    /**
     * Unplug a pool reader
     */
    @Test
    public void unplugStubPoolReader_success() throws KeyplePluginInstantiationException {
        StubPoolPluginImpl stubPoolPlugin =
                (StubPoolPluginImpl) new StubPoolPluginFactory(POOL_PLUGIN_NAME)
                        .getPluginInstance();

        // plug a reader
        stubPoolPlugin.plugStubPoolReader("anyGroup", "anyName", stubSe);

        // unplug the reader
        stubPoolPlugin.unplugStubPoolReader("anyGroup");

        Assert.assertEquals(0, stubPoolPlugin.getReaders().size());
        Assert.assertEquals(0, stubPoolPlugin.getReaderGroupReferences().size());


    }

    /**
     * Allocate one reader
     */
    @Test
    public void allocate_success() throws KeypleAllocationReaderException,
            KeypleAllocationNoReaderException, KeyplePluginInstantiationException {
        // init stubPoolPlugin
        StubPoolPluginImpl stubPoolPlugin =
                (StubPoolPluginImpl) new StubPoolPluginFactory(POOL_PLUGIN_NAME)
                        .getPluginInstance();

        // plug readers
        stubPoolPlugin.plugStubPoolReader("group1", "stub1", stubSe);
        stubPoolPlugin.plugStubPoolReader("group2", "stub2", stubSe);

        // allocate Reader
        SeReader seReader = stubPoolPlugin.allocateReader("group1");

        // check allocate result is correct
        Assert.assertTrue(seReader.getName().startsWith("stub1"));

        // check allocate list is correct
        Assert.assertTrue(stubPoolPlugin.listAllocatedReaders().containsKey("stub1"));
        Assert.assertEquals(1, stubPoolPlugin.listAllocatedReaders().size());

    }

    /**
     * Allocate twice the same reader
     */
    @Test(expected = KeypleAllocationNoReaderException.class)
    public void allocate_twice()
            throws InterruptedException, KeypleReaderException, KeypleAllocationReaderException,
            KeypleAllocationNoReaderException, KeyplePluginInstantiationException {
        // init stubPoolPlugin
        StubPoolPluginImpl stubPoolPlugin =
                (StubPoolPluginImpl) new StubPoolPluginFactory(POOL_PLUGIN_NAME)
                        .getPluginInstance();

        // plug readers
        stubPoolPlugin.plugStubPoolReader("group1", "stub1", stubSe);
        stubPoolPlugin.plugStubPoolReader("group2", "stub2", stubSe);

        // allocate Reader
        SeReader seReader = stubPoolPlugin.allocateReader("group1");
        SeReader seReader2 = stubPoolPlugin.allocateReader("group1");

    }

    /**
     * Release one reader
     */
    @Test
    public void release_success() throws KeypleAllocationReaderException,
            KeypleAllocationNoReaderException, KeyplePluginInstantiationException {
        // init stubPoolPlugin
        StubPoolPluginImpl stubPoolPlugin =
                (StubPoolPluginImpl) new StubPoolPluginFactory(POOL_PLUGIN_NAME)
                        .getPluginInstance();

        // plug readers
        stubPoolPlugin.plugStubPoolReader("group1", "stub1", stubSe);
        stubPoolPlugin.plugStubPoolReader("group2", "stub2", stubSe);

        // allocate Reader
        SeReader seReader = stubPoolPlugin.allocateReader("group1");

        // release reader
        stubPoolPlugin.releaseReader(seReader);

        // assert no reader is allocated
        Assert.assertEquals(0, stubPoolPlugin.listAllocatedReaders().size());

    }

    /**
     * Stub Secure Element
     */
    static final private StubSecureElement stubSe = new StubSecureElement() {
        @Override
        public byte[] getATR() {
            return new byte[0];
        }

        @Override
        public String getSeProcotol() {
            return null;
        }
    };


}
