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
package org.eclipse.keyple.calypso.transaction;

import org.eclipse.keyple.calypso.CalypsoBaseTest;
import org.eclipse.keyple.calypso.command.sam.SamRevision;
import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.keyple.calypso.transaction.SamResourceManager.MAX_BLOCKING_TIME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class SamResourceManagerPoolTest extends CalypsoBaseTest {


    private static final Logger logger = LoggerFactory.getLogger(SamResourceManagerPoolTest.class);

    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }

    @Test
    public void waitResources() throws KeypleReaderException {
        //init
        SamResourceManagerPool srmSpy = srmSpy();
        //doReturn(null).when(srmSpy).createSamResource(any(SeReader.class));
        long start = System.currentTimeMillis();

        //test
        SamResource out = srmSpy.allocateSamResource(
                SamResourceManager.AllocationMode.BLOCKING,
                new SamIdentifier(SamRevision.AUTO, "any","any"));

        long stop = System.currentTimeMillis();


        //assert results
        Assert.assertNull(out);
        Assert.assertTrue(stop-start > MAX_BLOCKING_TIME);
    }

    @Test
    public void getResource() throws KeypleReaderException {
        //init plugin
        ReaderPoolPlugin poolPlugin = Mockito.mock(ReaderPoolPlugin.class);
        doReturn(seReaderMock()).when(poolPlugin).allocateReader(any(String.class));

        //init SamResourceManagerPool with custom pool plugin
        SamResourceManagerPool srmSpy = srmSpy(poolPlugin);
        doReturn(samResourceMock()).when(srmSpy).createSamResource(any(SeReader.class));

        long start = System.currentTimeMillis();

        //test
        SamResource out = srmSpy.allocateSamResource(
                SamResourceManager.AllocationMode.BLOCKING,
                new SamIdentifier(SamRevision.AUTO, "any","any"));

        long stop = System.currentTimeMillis();

        //assert results
        Assert.assertNotNull(out);
        Assert.assertTrue(stop-start < MAX_BLOCKING_TIME);
    }

    /*
     * Helpers
     */
    //get a srm spy with a custom mock reader
    SamResourceManagerPool srmSpy(ReaderPoolPlugin poolPlugin) throws KeypleReaderException {
        return Mockito.spy(new SamResourceManagerPool(poolPlugin));
    }

    //get a srm spy with a default mock reader
    SamResourceManagerPool srmSpy() throws KeypleReaderException {
        ReaderPoolPlugin poolPlugin = Mockito.mock(ReaderPoolPlugin.class);
        return Mockito.spy(new SamResourceManagerPool(poolPlugin));
    }

    SamResource samResourceMock(){
        SamResource mock = Mockito.mock(SamResource.class);
        return mock;
    }

    SeReader seReaderMock(){
        SeReader mock = Mockito.mock(SeReader.class);
        return mock;
    }
}
