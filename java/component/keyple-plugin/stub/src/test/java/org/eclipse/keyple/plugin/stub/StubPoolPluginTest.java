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


import java.util.TreeSet;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StubPoolPluginTest extends BaseStubTest {

    Logger logger = LoggerFactory.getLogger(StubPoolPluginTest.class);


    @Before
    public void setupStub() throws Exception {
        super.setupStub();
    }

    @After
    public void clearStub() throws InterruptedException, KeypleReaderException {
        super.clearStub();
    }

    /**
     * Allocate one reader and count if created
     */
    @Test
    public void testAllocate_success() throws InterruptedException, KeypleReaderException {
        // init stubPoolPlugin
        StubPoolPlugin stubPoolPlugin = new StubPoolPlugin(new TreeSet<String>());

        // allocate Reader
        String groupReference = "GROUP_REF1";
        SeReader seReader = stubPoolPlugin.allocateReader(groupReference);

        // check
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        Assert.assertTrue(seReader.getName().startsWith(groupReference));
    }

    /**
     * Release one reader and count if created
     */
    @Test
    public void testRelease_success() throws InterruptedException, KeypleReaderException {
        // init stubPoolPlugin
        StubPoolPlugin stubPoolPlugin = new StubPoolPlugin(new TreeSet<String>());

        // allocate Reader
        String groupReference = "GROUP_REF1";
        SeReader seReader = stubPoolPlugin.allocateReader(groupReference);

        // deallocate Reader
        stubPoolPlugin.releaseReader(seReader);

        // check
        Assert.assertEquals(0, stubPlugin.getReaders().size());

    }

}
