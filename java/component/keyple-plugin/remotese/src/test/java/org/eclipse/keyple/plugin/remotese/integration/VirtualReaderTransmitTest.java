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
package org.eclipse.keyple.plugin.remotese.integration;

import java.util.Arrays;
import java.util.List;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.plugin.remotese.rm.json.SampleFactory;
import org.eclipse.keyple.plugin.stub.StubReaderTest;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test transmit scenarii extends configuration from VirtualReaderTest
 */
public class VirtualReaderTransmitTest extends VirtualReaderBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(VirtualReaderTransmitTest.class);


    @Before
    public void setUp() throws Exception {
        // restore plugin state
        clearStubpluginReaders();

        initKeypleServices();

        // configure and connect a Stub Native reader
        nativeReader = this.connectStubReader(NATIVE_READER_NAME, CLIENT_NODE_ID);

        // test virtual reader
        virtualReader = getVirtualReader();

    }

    @After
    public void tearDown() throws Exception {
        clearStubpluginReaders();
    }

    /*
     * TRANSMITS
     */

    @Test
    public void testKOTransmitSet_NoSE() {

        try {
            StubReaderTest.selectSe(virtualReader);

            virtualReader.transmitSet(SampleFactory.getASeRequestSet());
            // should throw KeypleReaderException
            Assert.assertTrue(false);

        } catch (KeypleReaderException e) {
            logger.info("KeypleReaderException was thrown as expected");
            // assert exception is thrown
            Assert.assertNotNull(e);
            Assert.assertNotNull(e.getSeResponseSet());
            Assert.assertNull(e.getSeResponse());
        }
    }

    @Test
    public void testKOTransmit_NoSE() {

        try {
            StubReaderTest.selectSe(virtualReader);

            virtualReader.transmit(SampleFactory.getASeRequest());
            // should throw KeypleReaderException
            Assert.assertTrue(false);

        } catch (KeypleReaderException e) {
            logger.info("KeypleReaderException was thrown as expected");
            // assert exception is thrown
            Assert.assertNotNull(e);
            // Assert.assertNotNull(e.getSeResponseSet());
            // Assert.assertNull(e.getSeResponse());
            // should not be null but transmit is using transmitSet, this is the reason I guess
            // todo : VirtualReader transmit should not be using transmitSet
        }
    }

    /**
     * Successful Transmit with a Calypso command to a Calypso SE
     *
     * @throws Exception
     */
    @Test
    public void rse_transmit_Hoplink_Sucessfull() throws Exception {
        int N_TIMES = 10;

        // insert SE
        nativeReader.insertSe(StubReaderTest.hoplinkSE());

        Thread.sleep(1000);

        StubReaderTest.selectSe(virtualReader);

        // test N_TIMES transmit with KEEP_OPEN
        for (int i = 0; i < N_TIMES; i++) {

            // test
            ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(PoClass.ISO,
                    (byte) 0x14, ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, true,
                    (byte) 0x20, "");
            List<ApduRequest> poApduRequestList =
                    Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());
            SeRequest seRequest = new SeRequest(poApduRequestList, ChannelState.KEEP_OPEN);
            SeResponseSet seResponse = virtualReader.transmitSet(new SeRequestSet(seRequest));
            // assert
            Assert.assertTrue(
                    seResponse.getSingleResponse().getApduResponses().get(0).isSuccessful());

            logger.info("SeResponseSet returned as expected {}", seResponse.getSingleResponse());
        }


    }

    @Test(expected = KeypleReaderException.class)
    public void rse_transmit_no_response() throws Exception {

        // insert SE
        nativeReader.insertSe(StubReaderTest.noApduResponseSE());

        // wait for card to be detected
        Thread.sleep(500);

        // init Request
        SeRequestSet requests = StubReaderTest.getNoResponseRequest();

        StubReaderTest.selectSe(virtualReader);

        // test
        virtualReader.transmitSet(requests);
    }


    @Test
    public void transmit_partial_response_set_0() throws InterruptedException {

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // wait for card to be detected
        Thread.sleep(500);

        // init Request
        SeRequestSet seRequestSet = StubReaderTest.getPartialRequestSet(0);

        try {
            StubReaderTest.selectSe(virtualReader);

            virtualReader.transmitSet(seRequestSet);

        } catch (KeypleReaderException ex) {
            logger.info("KeypleReaderException was thrown as expected : {} {}",
                    ex.getSeResponseSet(), ex.getSeResponse());

            Assert.assertEquals(ex.getSeResponseSet().getResponses().size(), 1);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(0).getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_set_1() throws InterruptedException {

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // wait for card to be detected
        Thread.sleep(500);

        // init Request
        SeRequestSet seRequestSet = StubReaderTest.getPartialRequestSet(1);

        try {
            StubReaderTest.selectSe(virtualReader);

            virtualReader.transmitSet(seRequestSet);

        } catch (KeypleReaderException ex) {
            logger.info("KeypleReaderException was thrown as expected : {} {}",
                    ex.getSeResponseSet(), ex.getSeResponse());
            Assert.assertEquals(ex.getSeResponseSet().getResponses().size(), 2);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(0).getApduResponses().size(), 4);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(1).getApduResponses().size(), 2);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(1).getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_set_2() throws InterruptedException {

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // wait for card to be detected
        Thread.sleep(500);

        // init Request
        SeRequestSet seRequestSet = StubReaderTest.getPartialRequestSet(2);

        // test
        try {
            StubReaderTest.selectSe(virtualReader);

            virtualReader.transmitSet(seRequestSet);

        } catch (KeypleReaderException ex) {
            logger.info("KeypleReaderException was thrown as expected : {} {}",
                    ex.getSeResponseSet(), ex.getSeResponse());
            Assert.assertEquals(ex.getSeResponseSet().getResponses().size(), 3);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(0).getApduResponses().size(), 4);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(1).getApduResponses().size(), 4);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(2).getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_set_3() throws InterruptedException {

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // wait for card to be detected
        Thread.sleep(500);

        // init Request
        SeRequestSet seRequestSet = StubReaderTest.getPartialRequestSet(3);

        // test
        try {
            StubReaderTest.selectSe(virtualReader);

            virtualReader.transmitSet(seRequestSet);

        } catch (KeypleReaderException ex) {
            logger.info("KeypleReaderException was thrown as expected : {} {}",
                    ex.getSeResponseSet(), ex.getSeResponse());
            Assert.assertEquals(ex.getSeResponseSet().getResponses().size(), 3);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(0).getApduResponses().size(), 4);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(1).getApduResponses().size(), 4);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(2).getApduResponses().size(), 4);
        }
    }

    @Test
    public void transmit_partial_response_0() throws InterruptedException {

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // wait for card to be detected
        Thread.sleep(500);

        // init Request
        SeRequest seRequest = StubReaderTest.getPartialRequest(0);

        // test
        try {
            StubReaderTest.selectSe(virtualReader);

            virtualReader.transmit(seRequest);

        } catch (KeypleReaderException ex) {
            logger.info("KeypleReaderException was thrown as expected : {} {}",
                    ex.getSeResponseSet(), ex.getSeResponse());
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 0);
        }
    }

    @Test
    public void transmit_partial_response_1() throws InterruptedException {

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // wait for card to be detected
        Thread.sleep(500);

        // init Request
        SeRequest seRequest = StubReaderTest.getPartialRequest(1);

        // test
        try {
            StubReaderTest.selectSe(virtualReader);

            virtualReader.transmit(seRequest);

        } catch (KeypleReaderException ex) {
            logger.info("KeypleReaderException was thrown as expected : {} {}",
                    ex.getSeResponseSet(), ex.getSeResponse());
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 1);
        }
    }

    @Test
    public void transmit_partial_response_2() throws InterruptedException {

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // wait for card to be detected
        Thread.sleep(500);

        // init Request
        SeRequest seRequest = StubReaderTest.getPartialRequest(2);

        // test
        try {
            StubReaderTest.selectSe(virtualReader);

            virtualReader.transmit(seRequest);

        } catch (KeypleReaderException ex) {
            logger.info("KeypleReaderException was thrown as expected : set : {}, seResponse : {}",
                    ex.getSeResponseSet(), ex.getSeResponse());
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_3() throws InterruptedException {

        // insert SE
        nativeReader.insertSe(StubReaderTest.partialSE());

        // wait for card to be detected
        Thread.sleep(500);

        // init Request
        SeRequest seRequest = StubReaderTest.getPartialRequest(3);

        try {
            // test
            StubReaderTest.selectSe(virtualReader);

            virtualReader.transmit(seRequest);

        } catch (KeypleReaderException ex) {
            logger.info("KeypleReaderException was thrown as expected : {} {}",
                    ex.getSeResponseSet(), ex.getSeResponse());
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 3);
        }
    }
}
