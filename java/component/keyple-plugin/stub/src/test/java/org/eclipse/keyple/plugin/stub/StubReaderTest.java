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
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.event.*;
import org.eclipse.keyple.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.*;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.transaction.MatchingSe;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelectionRequest;
import org.eclipse.keyple.transaction.SelectionsResult;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings("PMD.SignatureDeclareThrowsException")
@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StubReaderTest {

    StubReader reader;

    StubReader spyReader;

    Logger logger = LoggerFactory.getLogger(StubReaderTest.class);

    // init before each test
    @Before
    public void SetUp() throws InterruptedException, KeypleReaderException {
        // clear observers from others tests as StubPlugin is a singleton

        StubPlugin stubPlugin = StubPlugin.getInstance();

        // add an observer to start the plugin monitoring thread
        stubPlugin.addObserver(new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {}
        });

        logger.info("Stubplugin readers size {}", stubPlugin.getReaders().size());
        Assert.assertEquals(0, stubPlugin.getReaders().size());

        logger.info("Stubplugin observers size {}", stubPlugin.countObservers());
        Assert.assertEquals(1, stubPlugin.countObservers());

        stubPlugin.plugStubReader("StubReaderTest", true);

        reader = (StubReader) stubPlugin.getReader("StubReaderTest");
    }

    @After
    public void tearDown() throws InterruptedException, KeypleReaderException {
        StubPlugin stubPlugin = StubPlugin.getInstance();
        stubPlugin.clearObservers();
        reader.clearObservers();
        stubPlugin.getInstance().unplugStubReader("StubReaderTest", true);
    }


    static public void selectSe(SeReader reader) throws KeypleReaderException {
        SeSelection seSelection = new SeSelection();
        SeSelectionRequest seSelectionRequest = new SeSelectionRequest(
                new SeSelector(null, new SeSelector.AtrFilter("3B.*"), "ATR selection"),
                ChannelState.KEEP_OPEN, Protocol.ANY);

        /* Prepare selector, ignore MatchingSe here */
        seSelection.prepareSelection(seSelectionRequest);

        seSelection.processExplicitSelection(reader);
    }

    /*
     * TRANSMIT
     */


    @Test
    public void testInsert() throws InterruptedException {

        // CountDown lock
        final CountDownLatch lock = new CountDownLatch(1);

        // add observer
        reader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                Assert.assertEquals(event.getReaderName(), reader.getName());
                Assert.assertEquals(event.getPluginName(), StubPlugin.getInstance().getName());
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
        // observer

    }

    @Test
    public void testInsertMatchingSe() throws InterruptedException {

        // CountDown lock
        final CountDownLatch lock = new CountDownLatch(1);
        final String poAid = "A000000291A000000191";

        // add observer
        reader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                Assert.assertEquals(event.getReaderName(), reader.getName());
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
    public void testInsertNotMatching_MatchedOnly() throws InterruptedException {

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

        SeSelectionRequest seSelectionRequest = new SeSelectionRequest(
                new SeSelector(new SeSelector.AidSelector(ByteArrayUtils.fromHex(poAid), null),
                        null, "AID: " + poAid),
                ChannelState.KEEP_OPEN, Protocol.ANY);

        seSelection.prepareSelection(seSelectionRequest);

        ((ObservableReader) reader).setDefaultSelectionRequest(seSelection.getSelectionOperation(),
                ObservableReader.NotificationMode.MATCHED_ONLY);

        // test
        reader.insertSe(hoplinkSE());


        // lock thread for 2 seconds max to wait for the event
        lock.await(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, lock.getCount()); // should be 1 because countDown is never called
    }

    @Test
    public void testInsertNotMatching_Always() throws InterruptedException {

        // CountDown lock
        final CountDownLatch lock = new CountDownLatch(1);

        // add observer
        reader.addObserver(new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent event) {
                Assert.assertEquals(event.getReaderName(), reader.getName());
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
    public void testATR() throws InterruptedException {

        // CountDown lock
        final CountDownLatch lock = new CountDownLatch(1);

        // add observer
        reader.addObserver(new ObservableReader.ReaderObserver() {
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
                            seSelection.processExplicitSelection(reader);

                    MatchingSe matchingSe = selectionsResult.getActiveSelection().getMatchingSe();

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


    @Test(expected = IllegalArgumentException.class)
    public void transmit_Hoplink_null() throws Exception {
        reader.insertSe(hoplinkSE());
        reader.transmitSet((SeRequestSet) null);

        // throws exception
    }

    @Test
    public void transmit_Hoplink_Successful() throws KeypleReaderException, InterruptedException {
        // init Request
        SeRequestSet requests = getRequestIsoDepSetSample();

        // init SE
        reader.insertSe(hoplinkSE());

        // send the selection request
        selectSe(reader);

        // add Protocol flag
        reader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));
        // test
        SeResponseSet seResponse = reader.transmitSet(requests);

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
    public void transmit_no_response() throws KeypleReaderException, InterruptedException {
        // init Request
        SeRequestSet requests = getNoResponseRequest();

        // init SE
        reader.insertSe(noApduResponseSE());

        // add Protocol flag
        reader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        SeResponseSet seResponse = reader.transmitSet(requests);
    }

    @Test
    public void transmit_partial_response_set_0()
            throws KeypleReaderException, InterruptedException {
        // init Request
        SeRequestSet seRequestSet = getPartialRequestSet(0);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponseSet seResponseSet = reader.transmitSet(seRequestSet);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponseSet().getResponses().size(), 1);
            Assert.assertEquals(
                    ex.getSeResponseSet().getResponses().get(0).getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_set_1()
            throws KeypleReaderException, InterruptedException {
        // init Request
        SeRequestSet seRequestSet = getPartialRequestSet(1);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponseSet seResponseSet = reader.transmitSet(seRequestSet);
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
    public void transmit_partial_response_set_2()
            throws KeypleReaderException, InterruptedException {
        // init Request
        SeRequestSet seRequestSet = getPartialRequestSet(2);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponseSet seResponseSet = reader.transmitSet(seRequestSet);
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
    public void transmit_partial_response_set_3()
            throws KeypleReaderException, InterruptedException {
        // init Request
        SeRequestSet seRequestSet = getPartialRequestSet(3);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponseSet seResponseSet = reader.transmitSet(seRequestSet);
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
    public void transmit_partial_response_0() throws KeypleReaderException, InterruptedException {
        // init Request
        SeRequest seRequest = getPartialRequest(0);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponse seResponse = reader.transmit(seRequest);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 0);
        }
    }


    @Test
    public void transmit_partial_response_1() throws KeypleReaderException, InterruptedException {
        // init Request
        SeRequest seRequest = getPartialRequest(1);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponse seResponse = reader.transmit(seRequest);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 1);
        }
    }

    @Test
    public void transmit_partial_response_2() throws KeypleReaderException, InterruptedException {
        // init Request
        SeRequest seRequest = getPartialRequest(2);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponse seResponse = reader.transmit(seRequest);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_3() throws KeypleReaderException, InterruptedException {
        // init Request
        SeRequest seRequest = getPartialRequest(3);

        // init SE
        reader.insertSe(partialSE());

        // add Protocol flag
        reader.addSeProtocolSetting(
                new SeProtocolSetting(StubProtocolSetting.SETTING_PROTOCOL_ISO14443_4));

        // send the selection request
        selectSe(reader);

        // test
        try {
            SeResponse seResponse = reader.transmit(seRequest);
        } catch (KeypleReaderException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 3);
        }
    }


    /*
     * NAME and PARAMETERS
     */

    @Test
    public void testGetName() {
        Assert.assertNotNull(reader.getName());
    }

    // Set wrong parameter
    @Test(expected = KeypleReaderException.class)
    public void testSetWrongParameter() throws Exception {
        reader.setParameter("WRONG_PARAMETER", "a");
    }

    // Set wrong parameters
    @Test(expected = KeypleReaderException.class)
    public void testSetWrongParameters() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("WRONG_PARAMETER", "d");
        parameters.put(StubReader.ALLOWED_PARAMETER_1, "a");
        reader.setParameters(parameters);
    }

    // Set correct parameters
    @Test
    public void testSetParameters() throws Exception {
        Map<String, String> p1 = new HashMap<String, String>();
        p1.put(StubReader.ALLOWED_PARAMETER_1, "a");
        p1.put(StubReader.ALLOWED_PARAMETER_2, "a");

        reader.setParameters(p1);
        Map<String, String> p2 = reader.getParameters();
        assert (p1.equals(p2));


    }


    /**
     * AbstractObservableReader methods test
     *
     * @throws Exception
     */


    ObservableReader.ReaderObserver obs1 = new ObservableReader.ReaderObserver() {
        @Override
        public void update(ReaderEvent readerEvent) {}
    };

    ObservableReader.ReaderObserver obs2 = new ObservableReader.ReaderObserver() {
        @Override
        public void update(ReaderEvent readerEvent) {}
    };



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

        SeSelector selector = new SeSelector(
                new SeSelector.AidSelector(ByteArrayUtils.fromHex(poAid), null), null, null);

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
                return ByteArrayUtils
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
                return ByteArrayUtils
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
                return ByteArrayUtils
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
        return new ApduRequest(ByteArrayUtils.fromHex("FEDCBA98 9005h"), false);
    }
}
