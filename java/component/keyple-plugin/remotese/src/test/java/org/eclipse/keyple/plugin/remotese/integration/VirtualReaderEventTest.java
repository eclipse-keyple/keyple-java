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

import static org.eclipse.keyple.plugin.stub.StubReaderTest.hoplinkSE;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReaderTest;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelectionRequest;
import org.eclipse.keyple.transaction.SelectionsResult;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test Virtual Reader Service with stub plugin and hoplink SE
 */
public class VirtualReaderEventTest extends VirtualReaderBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(VirtualReaderEventTest.class);


    /*
     * SE EVENTS
     */

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


    /**
     * Test SE_INSERTED Reader Event throwing and catching
     *
     * @throws Exception
     */
    @Test
    public void testInsert() throws Exception {


        // lock test until message is received
        final CountDownLatch lock = new CountDownLatch(1);

        // add stubPluginObserver
        virtualReader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                Assert.assertEquals(event.getReaderName(), nativeReader.getName());
                Assert.assertEquals(event.getPluginName(), StubPlugin.getInstance().getName());
                Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());
                logger.debug("Reader Event is correct, release lock");
                lock.countDown();
            }
        });

        logger.info("Insert a Hoplink SE and wait 5 seconds for a SE event to be thrown");

        // insert SE
        nativeReader.insertSe(StubReaderTest.hoplinkSE());

        // wait 5 seconds
        lock.await(5, TimeUnit.SECONDS);

        Assert.assertEquals(0, lock.getCount());
    }


    /**
     * Test SE_REMOVED Reader Event throwing and catching
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveEvent() throws Exception {

        // lock test until two messages are received
        final CountDownLatch lock = new CountDownLatch(2);

        // add stubPluginObserver
        virtualReader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                if (event.getEventType() == ReaderEvent.EventType.SE_INSERTED) {
                    // we expect the first event to be SE_INSERTED
                    Assert.assertEquals(2, lock.getCount());
                    lock.countDown();
                } else {
                    // the next event should be SE_REMOVAL
                    Assert.assertEquals(1, lock.getCount());
                    Assert.assertEquals(event.getReaderName(), nativeReader.getName());
                    Assert.assertEquals(event.getPluginName(), StubPlugin.getInstance().getName());
                    Assert.assertEquals(ReaderEvent.EventType.SE_REMOVAL, event.getEventType());
                    logger.debug("Reader Event is correct, release lock");
                    lock.countDown();

                }
            }
        });

        logger.info(
                "Insert and remove a Hoplink SE and wait 5 seconds for two SE events to be thrown");

        // insert SE
        nativeReader.insertSe(StubReaderTest.hoplinkSE());

        // wait 0,5 second
        Thread.sleep(500);

        // remove SE
        nativeReader.removeSe();

        // wait 5 seconds
        lock.await(5, TimeUnit.SECONDS);

        Assert.assertEquals(0, lock.getCount());

        // https://github.com/calypsonet/keyple-java/issues/420
        // Assert.assertEquals(0, masterAPI.getPlugin().getReaders().size());
    }


    @Test
    public void testInsertMatchingSe() throws InterruptedException {

        // CountDown lock
        final CountDownLatch lock = new CountDownLatch(1);
        final String poAid = "A000000291A000000191";

        // add observer
        virtualReader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                Assert.assertEquals(event.getReaderName(), nativeReader.getName());
                Assert.assertEquals(event.getPluginName(), StubPlugin.getInstance().getName());
                Assert.assertEquals(ReaderEvent.EventType.SE_MATCHED, event.getEventType());
                Assert.assertTrue(event.getDefaultSelectionResponse().getSelectionSeResponseSet()
                        .getSingleResponse().getSelectionStatus().hasMatched());

                Assert.assertArrayEquals(
                        event.getDefaultSelectionResponse().getSelectionSeResponseSet()
                                .getSingleResponse().getSelectionStatus().getAtr().getBytes(),
                        hoplinkSE().getATR());

                // retrieve the expected FCI from the Stub SE running the select application command
                byte[] aid = ByteArrayUtils.fromHex(poAid);
                byte[] selectApplicationCommand = new byte[6 + aid.length];
                selectApplicationCommand[0] = (byte) 0x00; // CLA
                selectApplicationCommand[1] = (byte) 0xA4; // INS
                selectApplicationCommand[2] = (byte) 0x04; // P1: select by name
                selectApplicationCommand[3] = (byte) 0x00; // P2: requests the first
                selectApplicationCommand[4] = (byte) (aid.length); // Lc
                System.arraycopy(aid, 0, selectApplicationCommand, 5, aid.length); // data

                selectApplicationCommand[5 + aid.length] = (byte) 0x00; // Le
                byte[] fci = null;
                try {
                    fci = hoplinkSE().processApdu(selectApplicationCommand);
                } catch (KeypleIOReaderException e) {
                    e.printStackTrace();
                }

                Assert.assertArrayEquals(
                        event.getDefaultSelectionResponse().getSelectionSeResponseSet()
                                .getSingleResponse().getSelectionStatus().getFci().getBytes(),
                        fci);

                logger.debug("match event is correct");
                // unlock thread
                lock.countDown();
            }
        });

        SeSelection seSelection = new SeSelection();

        SeSelectionRequest seSelectionRequest = new SeSelectionRequest(
                new SeSelector(new SeSelector.AidSelector(ByteArrayUtils.fromHex(poAid), null),
                        null, "AID: " + poAid),
                ChannelState.KEEP_OPEN, Protocol.ANY);

        seSelection.prepareSelection(seSelectionRequest);

        ((ObservableReader) virtualReader).setDefaultSelectionRequest(
                seSelection.getSelectionOperation(),
                ObservableReader.NotificationMode.MATCHED_ONLY);

        // wait 1 second
        Thread.sleep(1000);

        // test
        nativeReader.insertSe(StubReaderTest.hoplinkSE());

        // lock thread for 2 seconds max to wait for the event
        lock.await(5, TimeUnit.SECONDS);
        Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by
        // observer

    }


    @Test
    public void testInsertNotMatching_MatchedOnly() throws InterruptedException {

        // CountDown lock
        final CountDownLatch lock = new CountDownLatch(1);

        // add observer
        virtualReader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                // no event should be thrown
                Assert.fail();
                lock.countDown();// should not be called
            }
        });
        String poAid = "A000000291A000000192";// not matching poAid

        SeSelection seSelection = new SeSelection();

        SeSelectionRequest seSelectionRequest = new SeSelectionRequest(
                new SeSelector(new SeSelector.AidSelector(ByteArrayUtils.fromHex(poAid), null),
                        null, "AID: " + poAid),
                ChannelState.KEEP_OPEN, Protocol.ANY);

        seSelection.prepareSelection(seSelectionRequest);

        ((ObservableReader) virtualReader).setDefaultSelectionRequest(
                seSelection.getSelectionOperation(),
                ObservableReader.NotificationMode.MATCHED_ONLY);

        // wait 1 second
        logger.debug("Wait 1 second before inserting SE");
        Thread.sleep(500);

        // test
        nativeReader.insertSe(StubReaderTest.hoplinkSE());


        // lock thread for 2 seconds max to wait for the event
        lock.await(3, TimeUnit.SECONDS);
        Assert.assertEquals(1, lock.getCount()); // should be 1 because countDown is never called
    }

    @Test
    public void testInsertNotMatching_Always() throws InterruptedException {

        // CountDown lock
        final CountDownLatch lock = new CountDownLatch(1);

        // add observer
        virtualReader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                Assert.assertEquals(event.getReaderName(), nativeReader.getName());
                Assert.assertEquals(event.getPluginName(), StubPlugin.getInstance().getName());

                // an SE_INSERTED event is thrown
                Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());

                // card has not match
                Assert.assertFalse(event.getDefaultSelectionResponse().getSelectionSeResponseSet()
                        .getSingleResponse().getSelectionStatus().hasMatched());

                lock.countDown();// should be called
            }
        });
        String poAid = "A000000291A000000192";// not matching poAid

        SeSelection seSelection = new SeSelection();

        SeSelectionRequest seSelectionRequest = new SeSelectionRequest(
                new SeSelector(new SeSelector.AidSelector(ByteArrayUtils.fromHex(poAid), null),
                        null, "AID: " + poAid),
                ChannelState.KEEP_OPEN, Protocol.ANY);

        seSelection.prepareSelection(seSelectionRequest);

        ((ObservableReader) virtualReader).setDefaultSelectionRequest(
                seSelection.getSelectionOperation(), ObservableReader.NotificationMode.ALWAYS);

        // wait 1 second
        logger.debug("Wait 1 second before inserting SE");
        Thread.sleep(500);

        // test
        nativeReader.insertSe(StubReaderTest.hoplinkSE());

        // lock thread for 2 seconds max to wait for the event
        lock.await(5, TimeUnit.SECONDS);
        Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by
        // observer
    }

    @Test
    public void testATR() throws InterruptedException {

        // CountDown lock
        final CountDownLatch lock = new CountDownLatch(1);

        // add observer
        virtualReader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {

                Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());

                SeSelection seSelection = new SeSelection();
                SeSelectionRequest seSelectionRequest = new SeSelectionRequest(
                        new SeSelector(null, new SeSelector.AtrFilter("3B.*"), "Test ATR"),
                        ChannelState.KEEP_OPEN, Protocol.ANY);

                /* Prepare selector, ignore MatchingSe here */
                seSelection.prepareSelection(seSelectionRequest);

                try {
                    SelectionsResult selectionsResult =
                            seSelection.processExplicitSelection(virtualReader);

                    MatchingSe matchingSe = selectionsResult.getActiveSelection().getMatchingSe();

                    Assert.assertNotNull(matchingSe);

                } catch (KeypleReaderException e) {
                    Assert.fail("Unexcepted exception");
                }
                // unlock thread
                lock.countDown();
            }
        });

        // wait 1 second
        logger.debug("Wait 1 second before inserting SE");
        Thread.sleep(500);

        // test
        nativeReader.insertSe(StubReaderTest.hoplinkSE());

        // lock thread for 2 seconds max to wait for the event
        lock.await(5, TimeUnit.SECONDS);
        Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by

    }


}
