/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.seproxy.plugin.reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.eclipse.keyple.core.seproxy.plugin.reader.AbsLocalReaderSelectionTest.ATR;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.eclipse.keyple.core.util.SeCommonProtocols;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Process Se Request Test for AbstractLocalReader */
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
    List<SeRequest> seRequests = getPartialRequestList(reader, 0);
    try {
      // test
      reader.processSeRequests(
          seRequests, MultiSeRequestProcessing.PROCESS_ALL, ChannelControl.CLOSE_AFTER);
      fail("A KeypleReaderIOException was expected");
    } catch (KeypleReaderIOException ex) {
      assertThat(ex.getSeResponses().size()).isEqualTo(1);
      assertThat(ex.getSeResponses().get(0).getApduResponses().size()).isEqualTo(2);
    }
  }

  @Test
  public void transmit_partial_response_set_1() throws Exception {
    AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

    List<SeRequest> seRequests = getPartialRequestList(reader, 1);
    try {
      // test
      reader.processSeRequests(
          seRequests, MultiSeRequestProcessing.PROCESS_ALL, ChannelControl.CLOSE_AFTER);
      fail("A KeypleReaderIOException was expected");
    } catch (KeypleReaderIOException ex) {
      assertThat(ex.getSeResponses().size()).isEqualTo(2);
      assertThat(ex.getSeResponses().get(0).getApduResponses().size()).isEqualTo(4);
      assertThat(ex.getSeResponses().get(1).getApduResponses().size()).isEqualTo(2);
      assertThat(ex.getSeResponses().get(1).getApduResponses().size()).isEqualTo(2);
    }
  }

  @Test
  public void transmit_partial_response_set_2() throws Exception {
    AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

    List<SeRequest> seRequests = getPartialRequestList(reader, 2);
    try {
      // test
      reader.processSeRequests(
          seRequests, MultiSeRequestProcessing.PROCESS_ALL, ChannelControl.CLOSE_AFTER);
      fail("A KeypleReaderIOException was expected");
    } catch (KeypleReaderIOException ex) {
      assertThat(ex.getSeResponses().size()).isEqualTo(3);
      assertThat(ex.getSeResponses().get(0).getApduResponses().size()).isEqualTo(4);
      assertThat(ex.getSeResponses().get(1).getApduResponses().size()).isEqualTo(4);
      assertThat(ex.getSeResponses().get(2).getApduResponses().size()).isEqualTo(2);
    }
  }

  @Test
  public void transmit_partial_response_set_3() throws Exception {
    AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

    List<SeRequest> seRequests = getPartialRequestList(reader, 3);
    try {
      // test
      List<SeResponse> responses =
          reader.processSeRequests(
              seRequests, MultiSeRequestProcessing.PROCESS_ALL, ChannelControl.CLOSE_AFTER);
      assertThat(responses.size()).isEqualTo(3);
      assertThat(responses.get(0).getApduResponses().size()).isEqualTo(4);
      assertThat(responses.get(1).getApduResponses().size()).isEqualTo(4);
      assertThat(responses.get(2).getApduResponses().size()).isEqualTo(0);

    } catch (KeypleReaderException ex) {
      fail("Should not throw exception");
    }
  }

  @Test
  public void transmit_first_match() throws Exception {
    AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

    List<SeRequest> seRequests = getPartialRequestList(reader, 3);
    try {
      // test
      List<SeResponse> responses =
          reader.processSeRequests(
              seRequests, MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER);
      assertThat(responses.size()).isEqualTo(1);
      assertThat(responses.get(0).getApduResponses().size()).isEqualTo(4);
    } catch (KeypleReaderException ex) {
      fail("Should not throw exception");
    }
  }

  @Test
  public void transmit_partial_response_0() throws Exception {
    AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

    SeRequest seRequest = getPartialRequest(reader, 0);
    try {
      // test
      reader.processSeRequest(seRequest, ChannelControl.KEEP_OPEN);
    } catch (KeypleReaderIOException ex) {
      logger.error("", ex);
      assertThat(ex.getSeResponse().getApduResponses().size()).isEqualTo(0);
    }
  }

  @Test
  public void transmit_partial_response_1() throws Exception {
    AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

    SeRequest seRequest = getPartialRequest(reader, 1);
    try {
      // test
      reader.processSeRequest(seRequest, ChannelControl.CLOSE_AFTER);
      fail("Should throw exception");

    } catch (KeypleReaderIOException ex) {
      assertThat(ex.getSeResponse().getApduResponses().size()).isEqualTo(1);
    }
  }

  @Test
  public void transmit_partial_response_2() throws Exception {
    AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

    SeRequest seRequest = getPartialRequest(reader, 2);
    try {
      // test
      reader.processSeRequest(seRequest, ChannelControl.CLOSE_AFTER);
      fail("Should throw exception");

    } catch (KeypleReaderIOException ex) {
      assertThat(ex.getSeResponse().getApduResponses().size()).isEqualTo(2);
    }
  }

  @Test
  public void transmit_partial_response_3() throws Exception {
    AbstractLocalReader reader = getSpy(PLUGIN_NAME, READER_NAME);

    SeRequest seRequest = getPartialRequest(reader, 3);
    try {
      // test
      SeResponse seResponse = reader.processSeRequest(seRequest, ChannelControl.CLOSE_AFTER);
      assertThat(seResponse.getApduResponses().size()).isEqualTo(3);
    } catch (KeypleReaderException ex) {
      fail("Should not throw exception");
    }
  }

  /*
   * Partial response set: multiple read records commands, one is not defined in the StubSE
   *
   * An Exception will be thrown.
   */
  public static List<SeRequest> getPartialRequestList(AbstractLocalReader r, int scenario) {

    SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter(ATR);
    SeSelector selector =
        SeSelector.builder()
            .seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4.getDescriptor())
            .atrFilter(atrFilter)
            .build();

    SeSelector failSelector =
        SeSelector.builder()
            .seProtocol(SeCommonProtocols.PROTOCOL_MIFARE_UL.getDescriptor())
            .atrFilter(atrFilter)
            .build();

    ApduRequest apduOK = new ApduRequest(APDU_SUCCESS, false);
    ApduRequest apduKO = new ApduRequest(APDU_IOEXC, false);

    List<ApduRequest> poApduRequests1 = new ArrayList<ApduRequest>();
    poApduRequests1.add(apduOK);
    poApduRequests1.add(apduOK);
    poApduRequests1.add(apduOK);
    poApduRequests1.add(apduOK);

    List<ApduRequest> poApduRequests2 = new ArrayList<ApduRequest>();
    poApduRequests2.add(apduOK);
    poApduRequests2.add(apduOK);
    poApduRequests2.add(apduOK);
    poApduRequests2.add(apduOK);

    List<ApduRequest> poApduRequests3 = new ArrayList<ApduRequest>();
    poApduRequests3.add(apduOK);
    poApduRequests3.add(apduOK);
    poApduRequests3.add(apduKO);
    poApduRequests3.add(apduOK);

    SeRequest seRequest1 = new SeRequest(selector, poApduRequests1);
    SeRequest seRequest2 = new SeRequest(selector, poApduRequests2);

    SeRequest seRequest4 = new SeRequest(failSelector, poApduRequests1);

    /* This SeRequest fails at step 3 */
    SeRequest seRequest3 = new SeRequest(selector, poApduRequests3);

    List<SeRequest> seRequests = new ArrayList<SeRequest>();

    switch (scenario) {
      case 0:
        /* 0 response Set */
        seRequests.add(seRequest3); // fails
        seRequests.add(seRequest1); // succeeds
        seRequests.add(seRequest2); // succeeds
        break;
      case 1:
        /* 1 response Set */
        seRequests.add(seRequest1); // succeeds
        seRequests.add(seRequest3); // fails
        seRequests.add(seRequest2); // succeeds
        break;
      case 2:
        /* 2 responses Set */
        seRequests.add(seRequest1); // succeeds
        seRequests.add(seRequest2); // succeeds
        seRequests.add(seRequest3); // fails
        break;
      case 3:
        /* 3 responses Set */
        seRequests.add(seRequest1); // succeeds
        seRequests.add(seRequest2); // succeeds
        seRequests.add(seRequest4); // selection fails
        break;
      case 4:
        /* 3 responses Set */
        seRequests.add(seRequest1); // succeeds
        break;
      default:
    }

    return seRequests;
  }

  public static SeRequest getPartialRequest(AbstractLocalReader r, int scenario) {

    /*
     * SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter(ATR); SeSelector selector = new
     * SeSelector( SeCommonProtocols.PROTOCOL_ISO14443_4, atrFilter, null, "iso");
     *
     */

    SeSelector aidSelector = AbsLocalReaderSelectionTest.getAidSelector();

    ApduRequest apduOK = new ApduRequest(APDU_SUCCESS, false);
    ApduRequest apduKO = new ApduRequest(APDU_IOEXC, false);

    List<ApduRequest> poApduRequests = new ArrayList<ApduRequest>();

    switch (scenario) {
      case 0:
        poApduRequests.add(apduKO); // fails
        poApduRequests.add(apduOK); // succeeds
        poApduRequests.add(apduOK); // succeeds
        break;
      case 1:
        poApduRequests.add(apduOK); // succeeds
        poApduRequests.add(apduKO); // fails
        poApduRequests.add(apduOK); // succeeds
        break;
      case 2:
        poApduRequests.add(apduOK); // succeeds
        poApduRequests.add(apduOK); // succeeds
        poApduRequests.add(apduKO); // fails
        break;
      case 3:
        poApduRequests.add(apduOK); // succeeds
        poApduRequests.add(apduOK); // succeeds
        poApduRequests.add(apduOK); // succeeds
        break;
      default:
        break;
    }

    return new SeRequest(aidSelector, poApduRequests);
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
  public static AbstractLocalReader getSpy(String pluginName, String readerName) {
    AbstractLocalReader r = Mockito.spy(new BlankAbstractLocalReader(pluginName, readerName));

    configure(r);

    return r;
  }

  public static void configure(AbstractLocalReader r) {

    // accept PROTOCOL_ISO14443_4
    when(r.getCurrentProtocol()).thenReturn(SeCommonProtocols.PROTOCOL_ISO14443_4.getDescriptor());

    // return art
    when(r.getATR()).thenReturn(ByteArrayUtil.fromHex(ATR));

    // success apdu
    doReturn(RESP_SUCCESS).when(r).transmitApdu(APDU_SUCCESS);

    // fail apdu
    doReturn(RESP_FAIL).when(r).transmitApdu(APDU_FAIL);

    // io exception apdu
    doThrow(new KeypleReaderIOException("io exception at transmitting " + APDU_IOEXC))
        .when(r)
        .transmitApdu(APDU_IOEXC);

    // aid selection
    doReturn(
            ByteArrayUtil.fromHex(
                "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000"))
        .when(r)
        .transmitApdu(ByteArrayUtil.fromHex("00 A4 04 00 0A A0 00 00 02 91 A0 00 00 01 91 00"));

    // physical channel is open
    doReturn(true).when(r).isPhysicalChannelOpen();
  }
}
