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
import org.eclipse.keyple.core.card.message.ApduRequest;
import org.eclipse.keyple.core.card.message.ApduResponse;
import org.eclipse.keyple.core.card.message.CardRequest;
import org.eclipse.keyple.core.card.message.CardResponse;
import org.eclipse.keyple.core.card.message.CardSelectionRequest;
import org.eclipse.keyple.core.card.message.CardSelectionResponse;
import org.eclipse.keyple.core.card.message.ChannelControl;
import org.eclipse.keyple.core.card.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.card.message.SelectionStatus;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.service.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.service.util.ContactsCardCommonProtocols;
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
        getACardRequestList_ISO14443_4(),
        MultiSelectionProcessing.FIRST_MATCH,
        ChannelControl.KEEP_OPEN);
  }

  public static ObservableReader.NotificationMode getNotificationMode() {
    return ObservableReader.NotificationMode.ALWAYS;
  }

  public static List<CardSelectionRequest> getACardRequestList_ISO14443_4() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    CardSelector.AidSelector aidSelector =
        CardSelector.AidSelector.builder().aidToSelect(poAid).build();

    CardSelector cardSelector =
        CardSelector.builder()
            .aidSelector(aidSelector)
            .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
            .build();

    CardRequest cardRequest = new CardRequest(poApduRequests);

    List<CardSelectionRequest> cardRequests = new ArrayList<CardSelectionRequest>();

    cardRequests.add(new CardSelectionRequest(cardSelector, cardRequest));

    return cardRequests;
  }

  public static List<CardSelectionRequest> getACardRequestList() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    CardRequest cardRequest = new CardRequest(poApduRequests);

    List<CardSelectionRequest> cardSelectionRequests = new ArrayList<CardSelectionRequest>();

    CardSelector cardSelector =
        CardSelector.builder()
            .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
            .aidSelector(CardSelector.AidSelector.builder().aidToSelect(poAid).build())
            .build();

    cardSelectionRequests.add(new CardSelectionRequest(cardSelector, cardRequest));

    return cardSelectionRequests;
  }

  public static CardRequest getACardRequest_ISO14443_4() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    CardRequest cardRequest = new CardRequest(poApduRequests);
    return cardRequest;
  }

  public static CardRequest getACardRequest() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    CardRequest cardRequest = new CardRequest(poApduRequests);
    return cardRequest;
  }

  public static List<CardSelectionRequest> getCompleteRequestList() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;

    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    CardSelector.AidSelector aidSelector =
        CardSelector.AidSelector.builder().aidToSelect(poAid).build();

    CardSelector aidCardSelector =
        CardSelector.builder()
            .aidSelector(aidSelector)
            .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name())
            .build();

    CardSelector.AtrFilter atrFilter = new CardSelector.AtrFilter("/regex/");

    CardSelector cardAtrSelector =
        CardSelector.builder()
            .atrFilter(atrFilter)
            .cardProtocol(ContactsCardCommonProtocols.ISO_7816_3.name())
            .build();

    CardRequest cardRequest = new CardRequest(poApduRequests);

    CardRequest cardRequest2 = new CardRequest(poApduRequests);

    List<CardSelectionRequest> cardSelectionRequests = new ArrayList<CardSelectionRequest>();
    cardSelectionRequests.add(new CardSelectionRequest(aidCardSelector, cardRequest));
    cardSelectionRequests.add(new CardSelectionRequest(cardAtrSelector, cardRequest2));

    return cardSelectionRequests;
  }

  public static List<CardSelectionResponse> getCompleteResponseList() {
    List<CardSelectionResponse> cardSelectionResponses = new ArrayList<CardSelectionResponse>();

    ApduResponse apdu = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());
    ApduResponse apdu2 = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());

    List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
    apduResponses.add(apdu);
    apduResponses.add(apdu2);

    cardSelectionResponses.add(
        new CardSelectionResponse(
            new SelectionStatus(null, apdu, true), new CardResponse(true, apduResponses)));
    cardSelectionResponses.add(
        new CardSelectionResponse(
            new SelectionStatus(null, apdu, true), new CardResponse(true, apduResponses)));

    return cardSelectionResponses;
  }
}
