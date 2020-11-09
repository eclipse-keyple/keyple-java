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
package org.eclipse.keyple.core.util.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.eclipse.keyple.core.card.message.*;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.service.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
import org.eclipse.keyple.core.service.util.ContactsCardCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;

public class SampleFactory {

  public static KeypleReaderIOException getIOExceptionWithResponse() {
    KeypleReaderIOException ioException = new KeypleReaderIOException("Keyple Reader Exception");
    ioException.setCardResponse(getACardResponse());
    return ioException;
  }

  public static KeypleReaderIOException getIOExceptionWithResponses() {
    KeypleReaderIOException ioException = new KeypleReaderIOException("Keyple Reader Exception");
    ioException.setCardSelectionResponses(getCompleteResponseSet());
    return ioException;
  }

  public static KeypleReaderIOException getAStackedKeypleException() {
    return new KeypleReaderIOException(
        "Keyple Reader Exception",
        new IOException("IO Error", new IOException("IO Error2", new RuntimeException("sdfsdf"))));
  }

  public static KeypleReaderIOException getASimpleKeypleException() {
    return new KeypleReaderIOException("Keyple Reader Exception");
  }

  public static KeypleReaderException getAReaderKeypleException() {
    return new KeypleReaderNotFoundException("Keyple Reader Not Found Exception");
  }

  public static AbstractDefaultSelectionsRequest getSelectionRequest() {
    return new DefaultSelectionsRequest(
        getCardSelectionRequests(), MultiSelectionProcessing.FIRST_MATCH, ChannelControl.KEEP_OPEN);
  }

  public static ObservableReader.NotificationMode getNotificationMode() {
    return ObservableReader.NotificationMode.ALWAYS;
  }

  public static List<CardSelectionRequest> getCardSelectionRequests() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    CardSelector.AidSelector aidSelector =
        CardSelector.AidSelector.builder() //
            .aidToSelect(poAid) //
            .build();

    CardSelector seSelector =
        CardSelector.builder() //
            .aidSelector(aidSelector) //
            .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name()) //
            .build();

    CardSelectionRequest seRequest = new CardSelectionRequest(seSelector, getACardRequest());

    List<CardSelectionRequest> seRequests = new ArrayList<CardSelectionRequest>();

    seRequests.add(seRequest);

    return seRequests;
  }

  public static List<CardRequest> getACardRequestList() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    CardRequest seRequest = new CardRequest(poApduRequests);

    List<CardRequest> seRequests = new ArrayList<CardRequest>();

    seRequests.add(seRequest);

    return seRequests;
  }

  public static CardRequest getACardRequest() {
    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    CardRequest seRequest = new CardRequest(poApduRequests);
    return seRequest;
  }

  public static List<CardSelectionRequest> getCompleteRequestList() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;

    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    CardSelector.AidSelector aidSelector =
        CardSelector.AidSelector.builder() //
            .aidToSelect(poAid) //
            .build();

    CardSelector aidCardSelector =
        CardSelector.builder() //
            .aidSelector(aidSelector) //
            .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name()) //
            .build();

    CardSelector.AtrFilter atrFilter = new CardSelector.AtrFilter("/regex/");

    CardSelector seAtrSelector =
        CardSelector.builder() //
            .atrFilter(atrFilter) //
            .cardProtocol(ContactsCardCommonProtocols.ISO_7816_3.name()) //
            .build();

    CardSelectionRequest cardSelectionRequest =
        new CardSelectionRequest(aidCardSelector, getACardRequest());

    CardSelectionRequest cardSelectionRequest2 =
        new CardSelectionRequest(seAtrSelector, getACardRequest());

    List<CardSelectionRequest> seRequests = new ArrayList<CardSelectionRequest>();
    seRequests.add(cardSelectionRequest);
    seRequests.add(cardSelectionRequest2);

    return seRequests;
  }

  public static List<CardSelectionResponse> getCompleteResponseSet() {
    List<CardSelectionResponse> cardSelectionResponses = new ArrayList<CardSelectionResponse>();

    cardSelectionResponses.add(getASelectionCardResponse());
    cardSelectionResponses.add(getASelectionCardResponse());

    return cardSelectionResponses;
  }

  public static CardResponse getACardResponse() {
    ApduResponse apdu = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());
    ApduResponse apdu2 = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());

    AnswerToReset atr = new AnswerToReset(ByteArrayUtil.fromHex("9000"));

    List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
    apduResponses.add(apdu);
    apduResponses.add(apdu2);

    return new CardResponse(true, apduResponses);
  }

  public static CardSelectionResponse getASelectionCardResponse() {
    ApduResponse apdu = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());

    AnswerToReset atr = new AnswerToReset(ByteArrayUtil.fromHex("9000"));

    return new CardSelectionResponse(new SelectionStatus(atr, apdu, true), getACardResponse());
  }

  public static class MyKeypleUserData {
    private final String field;

    MyKeypleUserData(String field) {
      this.field = field;
    }

    public String getField() {
      return field;
    }
  }
}
