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
package org.eclipse.keyple.core.seproxy.plugin.local;

import static org.eclipse.keyple.core.seproxy.plugin.local.AbsLocalReaderSelectionTest.ATR;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.plugin.mock.BlankAbstractLocalReader;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process Se Request Test for AbstractLocalReader
 */
public class AbsLocalReaderTransmitTest extends CoreBaseTest {


    private static final Logger logger = LoggerFactory.getLogger(AbsLocalReaderTransmitTest.class);


    static final String PLUGIN_NAME = "AbsLocalReaderTransmitTestP";
    static final String READER_NAME = "AbsLocalReaderTransmitTest";

    static final byte[] RESP_SUCCESS = ByteArrayUtil.fromHex("90 00");
    static final byte[] RESP_FAIL = ByteArrayUtil.fromHex("00 00");

    static final byte[] APDU_SUCCESS = ByteArrayUtil.fromHex("00 01");
    static final byte[] APDU_FAIL = ByteArrayUtil.fromHex("00 02");
    static final byte[] APDU_IOEXC = ByteArrayUtil.fromHex("00 03");

    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }


    @Test
    public void transmit_partial_response_set_0() throws Exception {
        AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

        // init Request
        Set<SeRequest> seRequestSet = getPartialRequestSet(reader, 0);
        try {
            // test
            reader.processSeRequestSet(seRequestSet, MultiSeRequestProcessing.PROCESS_ALL,
                    ChannelControl.CLOSE_AFTER);
            Assert.fail();
        } catch (KeypleReaderIOException ex) {
            Assert.assertEquals(ex.getSeResponseList().size(), 1);
            Assert.assertEquals(ex.getSeResponseList().get(0).getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_set_1() throws Exception {
        AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

        Set<SeRequest> seRequestSet = getPartialRequestSet(reader, 1);
        try {
            // test
            reader.processSeRequestSet(seRequestSet, MultiSeRequestProcessing.PROCESS_ALL,
                    ChannelControl.CLOSE_AFTER);
            Assert.fail();

        } catch (KeypleReaderIOException ex) {
            Assert.assertEquals(ex.getSeResponseList().size(), 2);
            Assert.assertEquals(ex.getSeResponseList().get(0).getApduResponses().size(), 4);
            Assert.assertEquals(ex.getSeResponseList().get(1).getApduResponses().size(), 2);
            Assert.assertEquals(ex.getSeResponseList().get(1).getApduResponses().size(), 2);
        }
    }


    @Test
    public void transmit_partial_response_set_2() throws Exception {
        AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);


        Set<SeRequest> seRequestSet = getPartialRequestSet(reader, 2);
        try {
            // test
            reader.processSeRequestSet(seRequestSet, MultiSeRequestProcessing.PROCESS_ALL,
                    ChannelControl.CLOSE_AFTER);
            Assert.fail();

        } catch (KeypleReaderIOException ex) {
            Assert.assertEquals(ex.getSeResponseList().size(), 3);
            Assert.assertEquals(ex.getSeResponseList().get(0).getApduResponses().size(), 4);
            Assert.assertEquals(ex.getSeResponseList().get(1).getApduResponses().size(), 4);
            Assert.assertEquals(ex.getSeResponseList().get(2).getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_set_3() throws Exception {
        AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);


        Set<SeRequest> seRequestSet = getPartialRequestSet(reader, 3);
        try {
            // test
            List<SeResponse> responses = reader.processSeRequestSet(seRequestSet,
                    MultiSeRequestProcessing.PROCESS_ALL, ChannelControl.CLOSE_AFTER);
            Assert.assertEquals(3, responses.size());
            Assert.assertEquals(4, responses.get(0).getApduResponses().size());
            Assert.assertEquals(4, responses.get(1).getApduResponses().size());
            Assert.assertEquals(null, responses.get(2));

        } catch (KeypleReaderException ex) {
            Assert.fail("Should not throw exception");
        }
    }

    @Test
    public void transmit_first_match() throws Exception {
        AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

        Set<SeRequest> seRequestSet = getPartialRequestSet(reader, 3);
        try {
            // test
            List<SeResponse> responses = reader.processSeRequestSet(seRequestSet,
                    MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
            Assert.assertEquals(1, responses.size());
            Assert.assertEquals(4, responses.get(0).getApduResponses().size());
        } catch (KeypleReaderException ex) {
            Assert.fail("Should not throw exception");
        }
    }

    @Test
    public void transmit_partial_response_0() throws Exception {
        AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

        SeRequest seRequestSet = getPartialRequest(reader, 0);
        try {
            // test
            reader.processSeRequest(seRequestSet, ChannelControl.KEEP_OPEN);
        } catch (KeypleReaderIOException ex) {
            logger.error("", ex);
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 0);
        }
    }


    @Test
    public void transmit_partial_response_1() throws Exception {
        AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);


        SeRequest seRequestSet = getPartialRequest(reader, 1);
        try {
            // test
            reader.processSeRequest(seRequestSet, ChannelControl.CLOSE_AFTER);
            Assert.fail("Should throw exception");

        } catch (KeypleReaderIOException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 1);
        }
    }

    @Test
    public void transmit_partial_response_2() throws Exception {
        AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

        SeRequest seRequestSet = getPartialRequest(reader, 2);
        try {
            // test
            reader.processSeRequest(seRequestSet, ChannelControl.CLOSE_AFTER);
            Assert.fail("Should throw exception");

        } catch (KeypleReaderIOException ex) {
            Assert.assertEquals(ex.getSeResponse().getApduResponses().size(), 2);
        }
    }

    @Test
    public void transmit_partial_response_3() throws Exception {
        AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

        SeRequest seRequestSet = getPartialRequest(reader, 3);
        try {
            // test
            SeResponse seResponse =
                    reader.processSeRequest(seRequestSet, ChannelControl.CLOSE_AFTER);
            Assert.assertEquals(seResponse.getApduResponses().size(), 3);
        } catch (KeypleReaderException ex) {
            Assert.fail("Should not throw exception");
        }
    }



    /*
     * Partial response set: multiple read records commands, one is not defined in the StubSE
     *
     * An Exception will be thrown.
     */
    static public Set<SeRequest> getPartialRequestSet(AbstractLocalReader r, int scenario)
            throws KeypleReaderException {

        SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter(ATR);
        SeSelector selector =
                new SeSelector(SeCommonProtocols.PROTOCOL_ISO14443_4, atrFilter, null, "atr");

        SeSelector failSelector =
                new SeSelector(SeCommonProtocols.PROTOCOL_MIFARE_UL, atrFilter, null, "atr");

        ApduRequest apduOK = new ApduRequest(APDU_SUCCESS, false);
        ApduRequest apduKO = new ApduRequest(APDU_IOEXC, false);

        List<ApduRequest> poApduRequestList1 = new ArrayList<ApduRequest>();
        poApduRequestList1.add(apduOK);
        poApduRequestList1.add(apduOK);
        poApduRequestList1.add(apduOK);
        poApduRequestList1.add(apduOK);

        List<ApduRequest> poApduRequestList2 = new ArrayList<ApduRequest>();
        poApduRequestList2.add(apduOK);
        poApduRequestList2.add(apduOK);
        poApduRequestList2.add(apduOK);
        poApduRequestList2.add(apduOK);

        List<ApduRequest> poApduRequestList3 = new ArrayList<ApduRequest>();
        poApduRequestList3.add(apduOK);
        poApduRequestList3.add(apduOK);
        poApduRequestList3.add(apduKO);
        poApduRequestList3.add(apduOK);

        SeRequest seRequest1 = new SeRequest(selector, poApduRequestList1);
        SeRequest seRequest2 = new SeRequest(selector, poApduRequestList2);

        SeRequest seRequest4 = new SeRequest(failSelector, poApduRequestList1);

        /* This SeRequest fails at step 3 */
        SeRequest seRequest3 = new SeRequest(selector, poApduRequestList3);

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
                seRequestSets.add(seRequest4); // selection fails
                break;
            case 4:
                /* 3 responses Set */
                seRequestSets.add(seRequest1); // succeeds
                break;
            default:
        }

        return seRequestSets;
    }


    static public SeRequest getPartialRequest(AbstractLocalReader r, int scenario)
            throws KeypleReaderException {

        /*
         * SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter(ATR); SeSelector selector = new
         * SeSelector( SeCommonProtocols.PROTOCOL_ISO14443_4, atrFilter, null, "iso");
         * 
         */

        SeSelector aidSelector = AbsLocalReaderSelectionTest.getAidSelector();


        ApduRequest apduOK = new ApduRequest(APDU_SUCCESS, false);
        ApduRequest apduKO = new ApduRequest(APDU_IOEXC, false);

        List<ApduRequest> poApduRequestList = new ArrayList<ApduRequest>();

        switch (scenario) {
            case 0:
                poApduRequestList.add(apduKO); // fails
                poApduRequestList.add(apduOK); // succeeds
                poApduRequestList.add(apduOK); // succeeds
                break;
            case 1:
                poApduRequestList.add(apduOK); // succeeds
                poApduRequestList.add(apduKO); // fails
                poApduRequestList.add(apduOK); // succeeds
                break;
            case 2:
                poApduRequestList.add(apduOK); // succeeds
                poApduRequestList.add(apduOK); // succeeds
                poApduRequestList.add(apduKO); // fails
                break;
            case 3:
                poApduRequestList.add(apduOK); // succeeds
                poApduRequestList.add(apduOK); // succeeds
                poApduRequestList.add(apduOK); // succeeds
                break;
            default:
                break;
        }

        return new SeRequest(aidSelector, poApduRequestList);
    }
    /*
     * Partial response: multiple read records commands, one is not defined in the StubSE
     *
     * An Exception will be thrown.
     */

    /**
     * Return a basic spy reader that responds to apdu
     * 
     * @param pluginName
     * @param readerName
     * @return basic spy reader
     * @throws KeypleReaderException
     */
    static public AbstractLocalReader getSpy(String pluginName, String readerName)
            throws KeypleReaderException {
        AbstractLocalReader r = Mockito.spy(new BlankAbstractLocalReader(pluginName, readerName));

        configure(r);

        return r;
    }


    static public void configure(AbstractLocalReader r) throws KeypleReaderException {

        // accept PROTOCOL_ISO14443_4
        when(r.protocolFlagMatches(SeCommonProtocols.PROTOCOL_ISO14443_4)).thenReturn(true);

        // refuse PROTOCOL_MIFARE_UL
        when(r.protocolFlagMatches(SeCommonProtocols.PROTOCOL_MIFARE_UL)).thenReturn(false);

        // return art
        when(r.getATR()).thenReturn(ByteArrayUtil.fromHex(ATR));

        // success apdu
        doReturn(RESP_SUCCESS).when(r).transmitApdu(APDU_SUCCESS);

        // fail apdu
        doReturn(RESP_FAIL).when(r).transmitApdu(APDU_FAIL);

        // io exception apdu
        doThrow(new KeypleReaderIOException("io exception at transmitting " + APDU_IOEXC)).when(r)
                .transmitApdu(APDU_IOEXC);

        // aid selection
        doReturn(ByteArrayUtil.fromHex(
                "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000"))
                        .when(r).transmitApdu(ByteArrayUtil
                                .fromHex("00 A4 04 00 0A A0 00 00 02 91 A0 00 00 01 91 00"));

        // physical channel is open
        doReturn(true).when(r).isPhysicalChannelOpen();
    }

}
