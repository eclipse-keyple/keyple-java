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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.*;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Se Selection Test for AbstractLocalReader */
public class AbsLocalReaderSelectionTest extends CoreBaseTest {

  private static final Logger logger = LoggerFactory.getLogger(AbsLocalReaderSelectionTest.class);

  final String PLUGIN_NAME = "AbstractLocalReaderTestP";
  final String READER_NAME = "AbstractLocalReaderTest";

  static final String AID = "A000000291A000000191";
  static final int STATUS_CODE_1 = 1;
  static final int STATUS_CODE_2 = 2;
  static final Set<Integer> STATUS_CODE_LIST =
      new HashSet(Arrays.asList(STATUS_CODE_1, STATUS_CODE_2));

  static final String ATR = "0000";

  final byte[] RESP_SUCCESS = ByteArrayUtil.fromHex("90 00");
  final byte[] RESP_FAIL = ByteArrayUtil.fromHex("00 00");

  @Before
  public void setUp() {
    logger.info("------------------------------");
    logger.info("Test {}", name.getMethodName() + "");
    logger.info("------------------------------");
  }

  /** ==== Card presence management ====================================== */
  @Test
  public void isSePresent_false() throws Exception {
    AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
    when(r.checkSePresence()).thenReturn(false);
    // test
    assertThat(r.isSePresent()).isFalse();
  }

  @Test
  public void isSePresent_true() throws Exception {
    AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
    when(r.checkSePresence()).thenReturn(true);
    // test
    assertThat(r.isSePresent()).isTrue();
  }

  /*
   * Select by ATR
   */

  @Test
  public void select_byAtr_success() throws Exception {
    AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
    // mock ATR
    when(r.getATR()).thenReturn(ByteArrayUtil.fromHex(ATR));

    SeSelector seSelector = getAtrSelector();

    SeRequest seRequest = new SeRequest(seSelector, new ArrayList<ApduRequest>());

    SeResponse seResponse = r.processSeRequest(seRequest, ChannelControl.KEEP_OPEN);

    assertThat(seResponse.getSelectionStatus().hasMatched()).isTrue();

    assertThat(r.isLogicalChannelOpen()).isTrue();
  }

  @Test
  public void select_byAtr_fail() throws Exception {
    AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
    // mock ATR to fail
    when(r.getATR()).thenReturn(ByteArrayUtil.fromHex("1000"));

    SeSelector seSelector = getAtrSelector();

    SeRequest seRequest = new SeRequest(seSelector, new ArrayList<ApduRequest>());

    SeResponse seResponse = r.processSeRequest(seRequest, ChannelControl.KEEP_OPEN);

    assertThat(seResponse.getSelectionStatus().hasMatched()).isFalse();
  }

  /*
   * Select by AID
   */

  @Test
  public void select_byAid_success() throws Exception {
    AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
    when(r.transmitApdu(any(byte[].class))).thenReturn(RESP_SUCCESS);

    SeSelector seSelector = getAidSelector();

    SeRequest seRequest = new SeRequest(seSelector, new ArrayList<ApduRequest>());

    SeResponse seResponse = r.processSeRequest(seRequest, ChannelControl.KEEP_OPEN);

    assertThat(seResponse.getSelectionStatus().hasMatched()).isTrue();
  }

  @Test
  public void select_byAid_fail() throws Exception {
    AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
    when(r.transmitApdu(any(byte[].class))).thenReturn(RESP_FAIL);

    SeSelector seSelector = getAidSelector();

    SeRequest seRequest = new SeRequest(seSelector, new ArrayList<ApduRequest>());

    SeResponse seResponse = r.processSeRequest(seRequest, ChannelControl.KEEP_OPEN);

    assertThat(seResponse.getSelectionStatus().hasMatched()).isFalse();
  }

  /*
   * Select by AID -- Smart Selection interface
   */
  @Test
  public void select_bySmartAid_success() throws Exception {
    // use a SmartSelectionReader object
    BlankSmartSelectionReader r = getSmartSpy(PLUGIN_NAME, READER_NAME);

    when(r.openChannelForAid(any(SeSelector.AidSelector.class)))
        .thenReturn(new ApduResponse(RESP_SUCCESS, STATUS_CODE_LIST));
    when(r.getATR()).thenReturn(ByteArrayUtil.fromHex(ATR));
    when(r.transmitApdu(any(byte[].class))).thenReturn(RESP_SUCCESS);

    SeSelector seSelector = getAidSelector();

    SeRequest seRequest = new SeRequest(seSelector, new ArrayList<ApduRequest>());

    SeResponse seResponse = r.processSeRequest(seRequest, ChannelControl.KEEP_OPEN);

    assertThat(seResponse.getSelectionStatus().hasMatched()).isTrue();
  }

  /*
   * Select by Atr and Aid
   */

  // atr fail, aid success
  // TODO OD : check this, it seems that this case is no treated
  // @Test
  public void select_byAtrAndAid_success() throws Exception {
    AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);
    // mock ATR to fail
    when(r.getATR()).thenReturn(ByteArrayUtil.fromHex("1000"));
    // mock aid to success
    when(r.transmitApdu(any(byte[].class))).thenReturn(RESP_SUCCESS);

    SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter(ATR);
    SeSelector.AidSelector aidSelector = SeSelector.AidSelector.builder().aidToSelect(AID).build();
    aidSelector.addSuccessfulStatusCode(STATUS_CODE_1);
    aidSelector.addSuccessfulStatusCode(STATUS_CODE_2);

    // select both
    SeSelector seSelector =
        SeSelector.builder().atrFilter(atrFilter).aidSelector(aidSelector).build();

    SeRequest seRequest = new SeRequest(seSelector, new ArrayList<ApduRequest>());

    SeResponse seResponse = r.processSeRequest(seRequest, ChannelControl.KEEP_OPEN);

    assertThat(seResponse.getSelectionStatus().hasMatched()).isTrue();
  }

  /*
   * Select by null null
   */

  // TODO OD:is this normal ? check this
  @Test
  public void select_no_param() throws Exception {
    AbstractLocalReader r = getSpy(PLUGIN_NAME, READER_NAME);

    SeSelector seSelector = SeSelector.builder().build();

    SeRequest seRequest = new SeRequest(seSelector, new ArrayList<ApduRequest>());

    SeResponse seResponse = r.processSeRequest(seRequest, ChannelControl.KEEP_OPEN);

    assertThat(seResponse.getSelectionStatus().hasMatched()).isTrue();
  }

  /*
   * HELPERS
   */

  /**
   * Return a basic spy reader
   *
   * @param pluginName
   * @param readerName
   * @return basic spy reader
   * @throws KeypleReaderException
   */
  public static AbstractLocalReader getSpy(String pluginName, String readerName) {
    AbstractLocalReader r = Mockito.spy(new BlankAbstractLocalReader(pluginName, readerName));
    return r;
  }

  public static BlankSmartSelectionReader getSmartSpy(String pluginName, String readerName) {
    BlankSmartSelectionReader r =
        Mockito.spy(new BlankSmartSelectionReader(pluginName, readerName));
    return r;
  }

  public static SeSelector getAidSelector() {
    SeSelector.AidSelector aidSelector = SeSelector.AidSelector.builder().aidToSelect(AID).build();
    aidSelector.addSuccessfulStatusCode(STATUS_CODE_1);
    aidSelector.addSuccessfulStatusCode(STATUS_CODE_2);

    return SeSelector.builder().aidSelector(aidSelector).build();
  }

  public static SeSelector getAtrSelector() {

    SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter(ATR);

    return SeSelector.builder().atrFilter(atrFilter).build();
  }
}
