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



import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.builder.IncreaseCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.selection.AbstractSeSelectionRequest;
import org.eclipse.keyple.core.selection.SeSelection;
import org.eclipse.keyple.core.selection.SelectionsResult;
import org.eclipse.keyple.core.seproxy.ChannelState;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.*;
import org.eclipse.keyple.core.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.rmi.CORBA.Stub;


@SuppressWarnings("PMD.SignatureDeclareThrowsException")
@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StubReaderTest extends BaseStubTest {


    Logger logger = LoggerFactory.getLogger(StubReaderTest.class);

    // init before each test
    @Before
    public void SetUp() throws Exception {
        // clear observers from others tests as StubPlugin is a singleton

        this.setupStub();

    }

    @After
    public void tearDown() throws KeypleReaderException {

        final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        stubPlugin.clearObservers();
        reader.clearObservers();
        stubPlugin.unplugStubReader("StubReaderTest", true);
    }



    /*
     * TEST
     *
     * EVENT
     *
     */


    /**
     * Insert SE check : event and se presence
     * 
     * @throws InterruptedException
     */
    @Test
    public void testInsert() throws Exception, NoStackTraceThrowable {

        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());

        final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        Assert.assertEquals(false, reader.isSePresent());

        // CountDown lock
        final CountDownLatch lock = new CountDownLatch(1);

        // add observer
        reader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                Assert.assertEquals(event.getReaderName(), reader.getName());
                Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
                Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());

                logger.debug("testInsert event is correct");
                // unlock thread
                lock.countDown();
            }
        });
        // test
        reader.insertSe(hoplinkSE());

        // lock thread for 2 seconds max to wait for the event
        lock.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by
        Assert.assertTrue(reader.isSePresent());


    }

    /**
     * Remove SE check : event and se presence
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRemove() throws Exception, NoStackTraceThrowable {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

        // CountDown lock
        final CountDownLatch insertLock = new CountDownLatch(1);
        final CountDownLatch removeLock = new CountDownLatch(1);

        // add observer
        reader.addObserver(new ObservableReader.ReaderObserver() {
            int event_i = 1;

            @Override
            public void update(ReaderEvent event) {
                logger.info("event {}", event.getEventType());
                // first event
                if (event_i == 1) {
                    Assert.assertEquals(event.getReaderName(), reader.getName());
                    Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
                    Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());
                    insertLock.countDown();
                }

                // analyze the second event, should be a SE_REMOVED
                if (event_i == 2) {
                    Assert.assertEquals(event.getReaderName(), reader.getName());
                    Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
                    Assert.assertEquals(ReaderEvent.EventType.SE_REMOVAL, event.getEventType());
                    removeLock.countDown();
                }
                event_i++;
            }
        });
        // test
        reader.insertSe(hoplinkSE());

        // lock thread for 2 seconds max to wait for the event SE_INSERTED
        insertLock.await(2, TimeUnit.SECONDS);

        Assert.assertEquals(0, insertLock.getCount()); // should be 0 because insertLock is
                                                       // countDown by obs

        reader.removeSe();

        // lock thread for 2 seconds max to wait for the event SE_REMOVAL
        removeLock.await(2, TimeUnit.SECONDS);

        Assert.assertEquals(0, removeLock.getCount()); // should be 0 because removeLock is
                                                       // countDown by obs

        Assert.assertFalse(reader.isSePresent());

    }

    /**
     * Remove SE check : event and se presence
     * 
     * @throws InterruptedException
     */
    @Test
    public void A_testInsertRemoveTwice() throws Exception, NoStackTraceThrowable {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

        // CountDown lock
        final CountDownLatch firstInsertLock = new CountDownLatch(1);
        final CountDownLatch firstRemoveLock = new CountDownLatch(1);
        final CountDownLatch secondInsertLock = new CountDownLatch(1);
        final CountDownLatch secondRemoveLock = new CountDownLatch(1);

        // add observer
        reader.addObserver(new ObservableReader.ReaderObserver() {
            int event_i = 1;

            @Override
            public void update(ReaderEvent event) {
                logger.info("event {}", event.getEventType());
                // first event
                if (event_i == 1) {
                    Assert.assertEquals(event.getReaderName(), reader.getName());
                    Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
                    Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());
                    firstInsertLock.countDown();
                }

                // analyze the second event, should be a SE_REMOVED
                if (event_i == 2) {
                    Assert.assertEquals(event.getReaderName(), reader.getName());
                    Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
                    Assert.assertEquals(ReaderEvent.EventType.SE_REMOVAL, event.getEventType());
                    firstRemoveLock.countDown();
                }
                if (event_i == 3) {
                    Assert.assertEquals(event.getReaderName(), reader.getName());
                    Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
                    Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());
                    secondInsertLock.countDown();
                }
                if (event_i == 4) {
                    Assert.assertEquals(event.getReaderName(), reader.getName());
                    Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
                    Assert.assertEquals(ReaderEvent.EventType.SE_REMOVAL, event.getEventType());
                    secondRemoveLock.countDown();
                }
                event_i++;
            }
        });
        // test first sequence
        reader.insertSe(hoplinkSE());

        Thread.sleep(1000);

        // lock thread for 2 seconds max to wait for the event SE_INSERTED
        firstInsertLock.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, firstInsertLock.getCount()); // should be 0 because insertLock is
                                                            // countDown by obs
        Thread.sleep(1000);

        reader.removeSe();

        // lock thread for 2 seconds max to wait for the event SE_REMOVAL
        firstRemoveLock.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, firstRemoveLock.getCount()); // should be 0 because removeLock is
                                                            // countDown by obs

        // BUG, insert event is not throw without (1)
        // BUG (1) make thread sleep
        // BUG, solved by setting a lower threadWaitTimeout (100ms)
        Thread.sleep(1000);

        // test second sequence
        reader.insertSe(hoplinkSE());

        // lock thread for 2 seconds max to wait for the event SE_INSERTED
        secondInsertLock.await(2, TimeUnit.SECONDS);

        Assert.assertEquals(0, secondInsertLock.getCount()); // should be 0 because insertLock is
                                                             // countDown by obs
        Thread.sleep(1000);
        reader.removeSe();

        // lock thread for 2 seconds max to wait for the event SE_REMOVAL
        secondRemoveLock.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, secondRemoveLock.getCount()); // should be 0 because removeLock is
                                                             // countDown by obs

        Assert.assertFalse(reader.isSePresent());

    }

    // @Test
    // Is unstable
    public void A_testInsertRemoveTwiceFast() throws Exception, NoStackTraceThrowable {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

        // CountDown lock
        final CountDownLatch firstInsertLock = new CountDownLatch(1);
        final CountDownLatch firstRemoveLock = new CountDownLatch(1);
        final CountDownLatch secondInsertLock = new CountDownLatch(1);
        final CountDownLatch secondRemoveLock = new CountDownLatch(1);

        // add observer
        reader.addObserver(new ObservableReader.ReaderObserver() {
            int event_i = 1;

            @Override
            public void update(ReaderEvent event) {
                logger.info("event {}", event.getEventType());
                // first event
                if (event_i == 1) {
                    Assert.assertEquals(event.getReaderName(), reader.getName());
                    Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
                    Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());
                    firstInsertLock.countDown();
                }

                // analyze the second event, should be a SE_REMOVED
                if (event_i == 2) {
                    Assert.assertEquals(event.getReaderName(), reader.getName());
                    Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
                    Assert.assertEquals(ReaderEvent.EventType.SE_REMOVAL, event.getEventType());
                    firstRemoveLock.countDown();
                }
                if (event_i == 3) {
                    Assert.assertEquals(event.getReaderName(), reader.getName());
                    Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
                    Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());
                    secondInsertLock.countDown();
                }
                if (event_i == 4) {
                    Assert.assertEquals(event.getReaderName(), reader.getName());
                    Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
                    Assert.assertEquals(ReaderEvent.EventType.SE_REMOVAL, event.getEventType());
                    secondRemoveLock.countDown();
                }
                event_i++;
            }
        });
        // test first sequence
        reader.insertSe(hoplinkSE());

        // lock thread for 2 seconds max to wait for the event SE_INSERTED
        firstInsertLock.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, firstInsertLock.getCount()); // should be 0 because insertLock is
        // countDown by obs


        reader.removeSe();

        // lock thread for 2 seconds max to wait for the event SE_REMOVAL
        firstRemoveLock.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, firstRemoveLock.getCount()); // should be 0 because removeLock is
        // countDown by obs

        // BUG, insert event is not throw without (1)
        // BUG (1) make thread sleep
        // BUG, solved by setting a lower threadWaitTimeout (100ms)

        // test second sequence
        reader.insertSe(hoplinkSE());

        // lock thread for 2 seconds max to wait for the event SE_INSERTED
        secondInsertLock.await(2, TimeUnit.SECONDS);

        Assert.assertEquals(0, secondInsertLock.getCount()); // should be 0 because insertLock is
        // countDown by obs
        reader.removeSe();

        // lock thread for 2 seconds max to wait for the event SE_REMOVAL
        secondRemoveLock.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, secondRemoveLock.getCount()); // should be 0 because removeLock is
        // countDown by obs

        Assert.assertFalse(reader.isSePresent());

    }

    @Test
    public void testInsertMatchingSe() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

        // CountDown lock
        final CountDownLatch lock = new CountDownLatch(1);
        final String poAid = "A000000291A000000191";

        // add Protocol flag
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        // add observer
        reader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                Assert.assertEquals(event.getReaderName(), reader.getName());
                Assert.assertEquals(event.getPluginName(), stubPlugin.getName());
                Assert.assertEquals(ReaderEvent.EventType.SE_MATCHED, event.getEventType());
                Assert.assertTrue(((DefaultSelectionsResponse) event.getDefaultSelectionsResponse())
                        .getSelectionSeResponseSet().getSingleResponse().getSelectionStatus()
                        .hasMatched());
                Assert.assertArrayEquals(
                        ((DefaultSelectionsResponse) event.getDefaultSelectionsResponse())
                                .getSelectionSeResponseSet().getSingleResponse()
                                .getSelectionStatus().getAtr().getBytes(),
                        hoplinkSE().getATR());

                // retrieve the expected FCI from the Stub SE running the select application command
                byte[] aid = ByteArrayUtil.fromHex(poAid);
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
                        ((DefaultSelectionsResponse) event.getDefaultSelectionsResponse())
                                .getSelectionSeResponseSet().getSingleResponse()
                                .getSelectionStatus().getFci().getBytes(),
                        fci);

                logger.debug("match event is correct");
                // unlock thread
                lock.countDown();
            }
        });

        SeSelection seSelection = new SeSelection();

        PoSelectionRequest poSelectionRequest = new PoSelectionRequest(new PoSelector(
                SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                new PoSelector.PoAidSelector(new SeSelector.AidSelector.IsoAid(poAid), null),
                "AID: " + poAid), ChannelState.KEEP_OPEN);

        seSelection.prepareSelection(poSelectionRequest);

        ((ObservableReader) reader).setDefaultSelectionRequest(seSelection.getSelectionOperation(),
                ObservableReader.NotificationMode.MATCHED_ONLY);

        // test
        reader.insertSe(hoplinkSE());

        // lock thread for 2 seconds max to wait for the event
        lock.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by
        // observer

    }


    @Test
    public void testInsertNotMatching_MatchedOnly() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

        // CountDown lock
        final CountDownLatch lock = new CountDownLatch(1);

        // add observer
        reader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                // no event is thrown
                lock.countDown();// should not be called
            }
        });
        String poAid = "A000000291A000000192";// not matching poAid

        SeSelection seSelection = new SeSelection();

        PoSelectionRequest poSelectionRequest = new PoSelectionRequest(new PoSelector(
                SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                new PoSelector.PoAidSelector(new SeSelector.AidSelector.IsoAid(poAid), null),
                "AID: " + poAid), ChannelState.KEEP_OPEN);

        seSelection.prepareSelection(poSelectionRequest);

        ((ObservableReader) reader).setDefaultSelectionRequest(seSelection.getSelectionOperation(),
                ObservableReader.NotificationMode.MATCHED_ONLY);

        // test
        reader.insertSe(hoplinkSE());


        // lock thread for 2 seconds max to wait for the event
        lock.await(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, lock.getCount()); // should be 1 because countDown is never called
    }

    @Test
    public void testInsertNotMatching_Always() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

        // CountDown lock
        final CountDownLatch lock = new CountDownLatch(1);

        // add Protocol flag
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        // add observer
        reader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                Assert.assertEquals(event.getReaderName(), reader.getName());
                Assert.assertEquals(event.getPluginName(), stubPlugin.getName());

                // an SE_INSERTED event is thrown
                Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());

                // card has not match
                Assert.assertFalse(
                        ((DefaultSelectionsResponse) event.getDefaultSelectionsResponse())
                                .getSelectionSeResponseSet().getSingleResponse()
                                .getSelectionStatus().hasMatched());

                lock.countDown();// should be called
            }
        });
        String poAid = "A000000291A000000192";// not matching poAid

        SeSelection seSelection = new SeSelection();

        PoSelectionRequest poSelectionRequest = new PoSelectionRequest(new PoSelector(
                SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                new PoSelector.PoAidSelector(new SeSelector.AidSelector.IsoAid(poAid), null),
                "AID: " + poAid), ChannelState.KEEP_OPEN);

        seSelection.prepareSelection(poSelectionRequest);

        ((ObservableReader) reader).setDefaultSelectionRequest(seSelection.getSelectionOperation(),
                ObservableReader.NotificationMode.ALWAYS);

        // test
        reader.insertSe(hoplinkSE());

        // lock thread for 2 seconds max to wait for the event
        lock.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(0, lock.getCount()); // should be 0 because countDown is called by
        // observer
    }

    @Test
    public void testATR() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        final StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

        // CountDown lock
        final CountDownLatch lock = new CountDownLatch(1);

        // add observer
        reader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {

                Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());

                SeSelection seSelection = new SeSelection();
                PoSelectionRequest poSelectionRequest = new PoSelectionRequest(
                        new PoSelector(SeCommonProtocols.PROTOCOL_ISO14443_4,
                                new PoSelector.PoAtrFilter("3B.*"), null, "Test" + " ATR"),
                        ChannelState.KEEP_OPEN);

                /* Prepare selector, ignore AbstractMatchingSe here */
                seSelection.prepareSelection(poSelectionRequest);

                try {
                    SelectionsResult selectionsResult =
                            seSelection.processExplicitSelection(reader);

                    AbstractMatchingSe matchingSe =
                            selectionsResult.getActiveSelection().getMatchingSe();

                    Assert.assertNotNull(matchingSe);

                } catch (KeypleReaderException e) {
                    Assert.fail("Unexcepted exception");
                }
                // unlock thread
                lock.countDown();
            }
        });

        // test
        reader.insertSe(hoplinkSE());

        // lock thread for 2 seconds max to wait for the event
        lock.await(2, TimeUnit.SECONDS);

    }


    /*
     * TEST
     *
     * TRANSMIT
     *
     */

    @Test(expected = IllegalArgumentException.class)
    public void transmit_Hoplink_null() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        reader.insertSe(hoplinkSE());
        ((ProxyReader)reader).transmitSet((SeRequestSet) null);

        // throws exception
    }

    @Test
    public void transmit_Hoplink_Successful() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        // init Request
        SeRequestSet requests = getRequestIsoDepSetSample();

        // init SE
        reader.insertSe(hoplinkSE());

        // add Protocol flag
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        SeResponseSet seResponse = ((ProxyReader)reader).transmitSet(requests);

        // assert
        Assert.assertTrue(seResponse.getSingleResponse().getApduResponses().get(0).isSuccessful());
    }


    // @Test
    // public void transmit_null_Selection() throws KeypleReaderException {
    // // init SE
    // // no SE
    //
    // // init request
    // SeRequestSet seRequest = getRequestIsoDepSetSample();
    //
    // // add Protocol flag
    // reader.addSeProtocolSetting(
    // new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));
    //
    // // test
    // SeResponseSet resp = reader.transmit(seRequest);
    //
    // Assert.assertNull(resp.getSingleResponse());
    // }


    @Test(expected = KeypleReaderException.class)
    public void transmit_no_response() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        // init Request
        SeRequestSet requests = getNoResponseRequest();

        // init SE
        reader.insertSe(noApduResponseSE());

        // add Protocol flag
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        SeResponseSet seResponse = ((ProxyReader)reader).transmitSet(requests);
    }

    @Test
    public void transmit_partial_response_set_0() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        // init Request
        SeRequestSet seRequestSet = getPartialRequestSet(0);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponseSet seResponseSet = ((ProxyReader)reader).transmitSet(seRequestSet);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponseSet().getResponses().size(), 1);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(0).getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_set_1() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        // init Request
        SeRequestSet seRequestSet = getPartialRequestSet(1);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponseSet seResponseSet = ((ProxyReader)reader).transmitSet(seRequestSet);
        } catch (KeypleReaderException ex) {
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
    public void transmit_partial_response_set_2() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        // init Request
        SeRequestSet seRequestSet = getPartialRequestSet(2);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponseSet seResponseSet = ((ProxyReader)reader).transmitSet(seRequestSet);
        } catch (KeypleReaderException ex) {
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
    public void transmit_partial_response_set_3() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        // init Request
        SeRequestSet seRequestSet = getPartialRequestSet(3);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponseSet seResponseSet = ((ProxyReader)reader).transmitSet(seRequestSet);
        } catch (KeypleReaderException ex) {
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
    public void transmit_partial_response_0() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        // init Request
        SeRequest seRequest = getPartialRequest(0);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponse seResponse = ((ProxyReader)reader).transmit(seRequest);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 0);
        }
    }


    @Test
    public void transmit_partial_response_1() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        // init Request
        SeRequest seRequest = getPartialRequest(1);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponse seResponse = ((ProxyReader)reader).transmit(seRequest);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 1);
        }
    }

    @Test
    public void transmit_partial_response_2() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        // init Request
        SeRequest seRequest = getPartialRequest(2);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponse seResponse = ((ProxyReader)reader).transmit(seRequest);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_3() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        // init Request
        SeRequest seRequest = getPartialRequest(3);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                StubProtocolSetting.STUB_PROTOCOL_SETTING
                        .get(SeCommonProtocols.PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponse seResponse = ((ProxyReader)reader).transmit(seRequest);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 3);
        }
    }


    /*
     * NAME and PARAMETERS
     */

    @Test
    public void testGetName() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        Assert.assertNotNull(reader.getName());
    }

    // Set wrong parameter
    @Test(expected = KeypleReaderException.class)
    public void testSetWrongParameter() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        reader.setParameter("WRONG_PARAMETER", "a");
    }

    // Set wrong parameters
    @Test(expected = KeypleReaderException.class)
    public void testSetWrongParameters() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("WRONG_PARAMETER", "d");
        parameters.put(StubReader.ALLOWED_PARAMETER_1, "a");
        reader.setParameters(parameters);
    }

    // Set correct parameters
    @Test
    public void testSetAllowedParameters() throws Exception {
        stubPlugin.plugStubReader("StubReaderTest", true);
        Assert.assertEquals(1, stubPlugin.getReaders().size());
        StubReader reader = (StubReader) stubPlugin.getReader("StubReaderTest");

        Map<String, String> p1 = new HashMap<String, String>();
        p1.put(StubReader.ALLOWED_PARAMETER_1, "a");
        p1.put(StubReader.ALLOWED_PARAMETER_2, "a");

        reader.setParameters(p1);
        Map<String, String> p2 = reader.getParameters();
        assert (p1.equals(p2));
    }


    /*
     * HELPER METHODS
     */


    static public SeRequestSet getRequestIsoDepSetSample() {
        String poAid = "A000000291A000000191";

        ReadRecordsCmdBuild poReadRecordCmd_T2Env =
                new ReadRecordsCmdBuild(PoClass.ISO, (byte) 0x14,
                        ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, true, (byte) 0x20, "");

        List<ApduRequest> poApduRequestList;

        poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest());

        SeRequest seRequest = new SeRequest(poApduRequestList, ChannelState.CLOSE_AFTER);

        return new SeRequestSet(seRequest);

    }

    /*
     * No Response: increase command is not defined in the StubSE
     *
     * An Exception will be thrown.
     */
    static public SeRequestSet getNoResponseRequest() {

        IncreaseCmdBuild poIncreaseCmdBuild =
                new IncreaseCmdBuild(PoClass.ISO, (byte) 0x14, (byte) 0x01, 0, "");

        List<ApduRequest> poApduRequestList;

        poApduRequestList = Arrays.asList(poIncreaseCmdBuild.getApduRequest());

        SeRequest seRequest = new SeRequest(poApduRequestList, ChannelState.CLOSE_AFTER);

        return new SeRequestSet(seRequest);

    }

    /*
     * Partial response set: multiple read records commands, one is not defined in the StubSE
     *
     * An Exception will be thrown.
     */
    static public SeRequestSet getPartialRequestSet(int scenario) {
        String poAid = "A000000291A000000191";

        ReadRecordsCmdBuild poReadRecord1CmdBuild = new ReadRecordsCmdBuild(PoClass.ISO,
                (byte) 0x14, ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, true, "");

        /* this command doesn't in the PartialSE */
        ReadRecordsCmdBuild poReadRecord2CmdBuild = new ReadRecordsCmdBuild(PoClass.ISO,
                (byte) 0x1E, ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, true, "");

        List<ApduRequest> poApduRequestList1 = new ArrayList<ApduRequest>();
        poApduRequestList1.add(poReadRecord1CmdBuild.getApduRequest());
        poApduRequestList1.add(poReadRecord1CmdBuild.getApduRequest());
        poApduRequestList1.add(poReadRecord1CmdBuild.getApduRequest());
        poApduRequestList1.add(poReadRecord1CmdBuild.getApduRequest());

        List<ApduRequest> poApduRequestList2 = new ArrayList<ApduRequest>();
        poApduRequestList2.add(poReadRecord1CmdBuild.getApduRequest());
        poApduRequestList2.add(poReadRecord1CmdBuild.getApduRequest());
        poApduRequestList2.add(poReadRecord1CmdBuild.getApduRequest());
        poApduRequestList2.add(poReadRecord1CmdBuild.getApduRequest());

        List<ApduRequest> poApduRequestList3 = new ArrayList<ApduRequest>();
        poApduRequestList3.add(poReadRecord1CmdBuild.getApduRequest());
        poApduRequestList3.add(poReadRecord1CmdBuild.getApduRequest());
        poApduRequestList3.add(poReadRecord2CmdBuild.getApduRequest());
        poApduRequestList3.add(poReadRecord1CmdBuild.getApduRequest());

        SeRequest seRequest1 = new SeRequest(poApduRequestList1, ChannelState.KEEP_OPEN);

        SeRequest seRequest2 = new SeRequest(poApduRequestList2, ChannelState.KEEP_OPEN);

        /* This SeRequest fails at step 3 */
        SeRequest seRequest3 = new SeRequest(poApduRequestList3, ChannelState.KEEP_OPEN);

        SeRequest seRequest4 = new SeRequest(poApduRequestList1, ChannelState.KEEP_OPEN);

        Set<SeRequest> seRequestSets = new LinkedHashSet<SeRequest>();

        switch (scenario) {
            case 0:
                /* 0 response Set */
                seRequestSets.add(seRequest3); // fails
                seRequestSets.add(seRequest1); // succeeds
                seRequestSets.add(seRequest2); // succeeds
                break;
            case 1:
                /* 1 response Set */
                seRequestSets.add(seRequest1); // succeeds
                seRequestSets.add(seRequest3); // fails
                seRequestSets.add(seRequest2); // succeeds
                break;
            case 2:
                /* 2 responses Set */
                seRequestSets.add(seRequest1); // succeeds
                seRequestSets.add(seRequest2); // succeeds
                seRequestSets.add(seRequest3); // fails
                break;
            case 3:
                /* 3 responses Set */
                seRequestSets.add(seRequest1); // succeeds
                seRequestSets.add(seRequest2); // succeeds
                seRequestSets.add(seRequest4); // succeeds
                break;
            default:
        }

        return new SeRequestSet(seRequestSets);
    }

    /*
     * Partial response: multiple read records commands, one is not defined in the StubSE
     *
     * An Exception will be thrown.
     */
    static public SeRequest getPartialRequest(int scenario) {
        String poAid = "A000000291A000000191";

        ReadRecordsCmdBuild poReadRecord1CmdBuild = new ReadRecordsCmdBuild(PoClass.ISO,
                (byte) 0x14, ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, true, "");

        /* this command doesn't in the PartialSE */
        ReadRecordsCmdBuild poReadRecord2CmdBuild = new ReadRecordsCmdBuild(PoClass.ISO,
                (byte) 0x1E, ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, true, "");

        List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();

        switch (scenario) {
            case 0:
                poApduRequestList.add(poReadRecord2CmdBuild.getApduRequest()); // fails
                poApduRequestList.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
                poApduRequestList.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
                break;
            case 1:
                poApduRequestList.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
                poApduRequestList.add(poReadRecord2CmdBuild.getApduRequest()); // fails
                poApduRequestList.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
                break;
            case 2:
                poApduRequestList.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
                poApduRequestList.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
                poApduRequestList.add(poReadRecord2CmdBuild.getApduRequest()); // fails
                break;
            case 3:
                poApduRequestList.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
                poApduRequestList.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
                poApduRequestList.add(poReadRecord1CmdBuild.getApduRequest()); // succeeds
                break;
            default:
                break;
        }

        SeSelector selector = new SeSelector(SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                new SeSelector.AidSelector(
                        new SeSelector.AidSelector.IsoAid(ByteArrayUtil.fromHex(poAid)), null),
                null);

        return new SeRequest(poApduRequestList, ChannelState.CLOSE_AFTER);
    }

    static public StubSecureElement hoplinkSE() {


        return new StubSecureElement() {

            @Override
            public byte[] processApdu(byte[] apduIn) throws KeypleIOReaderException {
                addHexCommand("00 A4 04 00 0A A0 00 00 02 91 A0 00 00 01 91 00",
                        "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000");

                addHexCommand("00 A4 04 00 0A A0 00 00 02 91 A0 00 00 01 92 00", "6A82");


                addHexCommand("00 B2 01 A4 20",
                        "00000000000000000000000000000000000000000000000000000000000000009000");

                return super.processApdu(apduIn);
            }

            @Override
            public byte[] getATR() {
                return ByteArrayUtil
                        .fromHex("3B 8E 80 01 80 31 80 66 40 90 89 12 08 02 83 01 90 00 0B");
            }

            @Override
            public String getSeProcotol() {
                return "PROTOCOL_ISO14443_4";
            }
        };


    }

    static public StubSecureElement noApduResponseSE() {
        return new StubSecureElement() {

            @Override
            public byte[] processApdu(byte[] apduIn) throws KeypleIOReaderException {

                addHexCommand("00 A4 04 00 0A A0 00 00 02 91 A0 00 00 01 91 00",
                        "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000");

                return super.processApdu(apduIn);
            }

            @Override
            public byte[] getATR() {
                return ByteArrayUtil
                        .fromHex("3B 8E 80 01 80 31 80 66 40 90 89 12 08 02 83 01 90 00 0B");
            }

            @Override
            public String getSeProcotol() {
                return "PROTOCOL_ISO14443_4";
            }
        };
    }

    static public StubSecureElement partialSE() {
        return new StubSecureElement() {
            @Override
            public byte[] processApdu(byte[] apduIn) throws KeypleIOReaderException {

                addHexCommand("00 A4 04 00 0A A0 00 00 02 91 A0 00 00 01 91 00",
                        "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000");
                addHexCommand("00 B2 01 A4 00",
                        "00000000000000000000000000000000000000000000000000000000009000");

                return super.processApdu(apduIn);
            }

            @Override
            public byte[] getATR() {
                return ByteArrayUtil
                        .fromHex("3B 8E 80 01 80 31 80 66 40 90 89 12 08 02 83 01 90 00 0B");
            }

            @Override
            public String getSeProcotol() {
                return "PROTOCOL_ISO14443_4";
            }
        };



    }

    static public StubSecureElement getSENoconnection() {
        return new StubSecureElement() {
            @Override
            public byte[] getATR() {
                return new byte[0];
            }

            @Override
            public boolean isPhysicalChannelOpen() {
                return false;
            }

            // override methods to fail open connection
            @Override
            public void openPhysicalChannel() throws KeypleChannelStateException {
                throw new KeypleChannelStateException("Impossible to estasblish connection");
            }

            @Override
            public void closePhysicalChannel() throws KeypleChannelStateException {
                throw new KeypleChannelStateException("Channel is not open");
            }

            @Override
            public byte[] processApdu(byte[] apduIn) throws KeypleIOReaderException {
                throw new KeypleIOReaderException("Error while transmitting apdu");
            }

            @Override
            public String getSeProcotol() {
                return null;
            }
        };

    }

    static public ApduRequest getApduSample() {
        return new ApduRequest(ByteArrayUtil.fromHex("FEDCBA98 9005h"), false);
    }

    static public void selectSe(SeReader reader) throws KeypleReaderException {
        /**
         * Create a new local class extending AbstractSeSelectionRequest
         */
        class GenericSeSelectionRequest extends AbstractSeSelectionRequest {
            TransmissionMode transmissionMode;

            public GenericSeSelectionRequest(SeSelector seSelector, ChannelState channelState) {
                super(seSelector, channelState);
                transmissionMode = seSelector.getSeProtocol().getTransmissionMode();
            }

            @Override
            protected AbstractMatchingSe parse(SeResponse seResponse) {
                class GenericMatchingSe extends AbstractMatchingSe {
                    public GenericMatchingSe(SeResponse selectionResponse,
                            TransmissionMode transmissionMode, String extraInfo) {
                        super(selectionResponse, transmissionMode, extraInfo);
                    }
                }
                return new GenericMatchingSe(seResponse, transmissionMode, "Generic Matching SE");
            }
        }

        SeSelection seSelection = new SeSelection();
        GenericSeSelectionRequest genericSeSelectionRequest = new GenericSeSelectionRequest(
                new SeSelector(SeCommonProtocols.PROTOCOL_ISO14443_4,
                        new SeSelector.AtrFilter("3B.*"), null, "ATR selection"),
                ChannelState.KEEP_OPEN);

        /* Prepare selector, ignore AbstractMatchingSe here */
        seSelection.prepareSelection(genericSeSelectionRequest);

        seSelection.processExplicitSelection(reader);
    }
}
