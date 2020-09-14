/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.virtualse.impl;

import java.io.IOException;
import java.util.*;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;

public class SampleFactory {

  public static KeypleReaderIOException getAStackedKeypleException() {
    return new KeypleReaderIOException(
        "Keyple Reader Exception",
        new IOException("IO Error", new IOException("IO Error2", new RuntimeException("sdfsdf"))));
  }

  public static KeypleReaderIOException getASimpleKeypleException() {
    return new KeypleReaderIOException("Keyple Reader Exception");
  }

  public static DefaultSelectionsRequest getSelectionRequest() {
    return new DefaultSelectionsRequest(
        getASeRequestList_ISO14443_4(),
        MultiSeRequestProcessing.FIRST_MATCH,
        ChannelControl.KEEP_OPEN);
  }

  public static ObservableReader.NotificationMode getNotificationMode() {
    return ObservableReader.NotificationMode.ALWAYS;
  }

  public static List<SeRequest> getASeRequestList_ISO14443_4() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    SeSelector.AidSelector aidSelector =
        SeSelector.AidSelector.builder() //
            .aidToSelect(poAid) //
            .build();

    SeSelector seSelector =
        SeSelector.builder() //
            .aidSelector(aidSelector) //
            .seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4) //
            .build();

    SeRequest seRequest = new SeRequest(seSelector, poApduRequests);

    List<SeRequest> seRequests = new ArrayList<SeRequest>();

    seRequests.add(seRequest);

    return seRequests;
  }

  public static List<SeRequest> getASeRequestList() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    SeRequest seRequest = new SeRequest(poApduRequests);

    List<SeRequest> seRequests = new ArrayList<SeRequest>();

    seRequests.add(seRequest);

    return seRequests;
  }

  public static SeRequest getASeRequest_ISO14443_4() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    SeSelector.AidSelector aidSelector =
        SeSelector.AidSelector.builder() //
            .aidToSelect(poAid) //
            .build();

    SeSelector seSelector =
        SeSelector.builder() //
            .aidSelector(aidSelector) //
            .seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4) //
            .build();

    SeRequest seRequest = new SeRequest(seSelector, poApduRequests);
    return seRequest;
  }

  public static SeRequest getASeRequest() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    SeRequest seRequest = new SeRequest(poApduRequests);
    return seRequest;
  }

  public static List<SeRequest> getCompleteRequestList() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;

    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    SeSelector.AidSelector aidSelector =
        SeSelector.AidSelector.builder() //
            .aidToSelect(poAid) //
            .build();

    SeSelector aidSeSelector =
        SeSelector.builder() //
            .aidSelector(aidSelector) //
            .seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4) //
            .build();

    SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter("/regex/");

    SeSelector seAtrSelector =
        SeSelector.builder() //
            .atrFilter(atrFilter) //
            .seProtocol(SeCommonProtocols.PROTOCOL_ISO7816_3) //
            .build();

    SeRequest seRequest = new SeRequest(aidSeSelector, poApduRequests);

    SeRequest seRequest2 = new SeRequest(seAtrSelector, poApduRequests);

    List<SeRequest> seRequests = new ArrayList<SeRequest>();
    seRequests.add(seRequest);
    seRequests.add(seRequest2);

    return seRequests;
  }

  public static List<SeResponse> getCompleteResponseList() {
    List<SeResponse> seResponses = new ArrayList<SeResponse>();

    ApduResponse apdu = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());
    ApduResponse apdu2 = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());

    List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
    apduResponses.add(apdu);
    apduResponses.add(apdu2);

    seResponses.add(
        new SeResponse(true, true, new SelectionStatus(null, apdu, true), apduResponses));
    seResponses.add(
        new SeResponse(true, true, new SelectionStatus(null, apdu, true), apduResponses));

    return seResponses;
  }

  public static SeProtocol getSeProtocol() {
    return new SeProtocol() {
      @Override
      public String getName() {
        return SeCommonProtocols.PROTOCOL_ISO14443_4.name();
      }

      @Override
      public TransmissionMode getTransmissionMode() {
        return TransmissionMode.CONTACTS;
      }
    };
  }

  public static Map<SeProtocol, String> getSeProtocolSetting() {
    Map<SeProtocol, String> seProtocolSetting = new HashMap<SeProtocol, String>();
    SeProtocol seProtocol = getSeProtocol();
    seProtocolSetting.put(seProtocol, "protocolRule");
    return seProtocolSetting;
  }

  public static DefaultSelectionsResponse getDefaultSelectionsResponse() {
    return new DefaultSelectionsResponse(getCompleteResponseList());
  }
}
