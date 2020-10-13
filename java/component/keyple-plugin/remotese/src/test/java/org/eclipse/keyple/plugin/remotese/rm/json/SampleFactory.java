/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.rm.json;

import java.io.IOException;
import java.util.*;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.plugin.reader.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.seproxy.plugin.reader.util.ContactsCardCommonProtocols;
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

  public static AbstractDefaultSelectionsRequest getSelectionRequest() {
    return new DefaultSelectionsRequest(
        getASeRequestList_ISO14443_4(),
        MultiSeRequestProcessing.FIRST_MATCH,
        ChannelControl.KEEP_OPEN);
  }

  public static ObservableReader.NotificationMode getNotificationMode() {
    return ObservableReader.NotificationMode.ALWAYS;
  }

  public static List<CardRequest> getASeRequestList_ISO14443_4() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    SeSelector.AidSelector aidSelector =
        SeSelector.AidSelector.builder().aidToSelect(poAid).build();

    SeSelector seSelector =
        SeSelector.builder()
            .aidSelector(aidSelector)
            .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
            .build();

    CardRequest cardRequest = new CardRequest(seSelector, poApduRequests);

    List<CardRequest> cardRequests = new ArrayList<CardRequest>();

    cardRequests.add(cardRequest);

    return cardRequests;
  }

  public static List<CardRequest> getASeRequestList() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    CardRequest cardRequest = new CardRequest(poApduRequests);

    List<CardRequest> cardRequests = new ArrayList<CardRequest>();

    cardRequests.add(cardRequest);

    return cardRequests;
  }

  public static CardRequest getASeRequest_ISO14443_4() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    SeSelector.AidSelector aidSelector =
        SeSelector.AidSelector.builder().aidToSelect(poAid).build();

    SeSelector seSelector =
        SeSelector.builder()
            .aidSelector(aidSelector)
            .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
            .build();

    CardRequest cardRequest = new CardRequest(seSelector, poApduRequests);
    return cardRequest;
  }

  public static CardRequest getASeRequest() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    CardRequest cardRequest = new CardRequest(poApduRequests);
    return cardRequest;
  }

  public static List<CardRequest> getCompleteRequestList() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;

    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    SeSelector.AidSelector aidSelector =
        SeSelector.AidSelector.builder().aidToSelect(poAid).build();

    SeSelector aidSeSelector =
        SeSelector.builder()
            .aidSelector(aidSelector)
            .seProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
            .build();

    SeSelector.AtrFilter atrFilter = new SeSelector.AtrFilter("/regex/");

    SeSelector seAtrSelector =
        SeSelector.builder()
            .atrFilter(atrFilter)
            .seProtocol(ContactsCardCommonProtocols.ISO_7816_3.name())
            .build();

    CardRequest cardRequest = new CardRequest(aidSeSelector, poApduRequests);

    CardRequest cardRequest2 = new CardRequest(seAtrSelector, poApduRequests);

    List<CardRequest> cardRequests = new ArrayList<CardRequest>();
    cardRequests.add(cardRequest);
    cardRequests.add(cardRequest2);

    return cardRequests;
  }

  public static List<CardResponse> getCompleteResponseSet() {
    List<CardResponse> cardResponse = new ArrayList<CardResponse>();

    ApduResponse apdu = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());
    ApduResponse apdu2 = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());

    List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
    apduResponses.add(apdu);
    apduResponses.add(apdu2);

    cardResponse.add(
        new CardResponse(true, true, new SelectionStatus(null, apdu, true), apduResponses));
    cardResponse.add(
        new CardResponse(true, true, new SelectionStatus(null, apdu, true), apduResponses));

    return cardResponse;
  }
}
