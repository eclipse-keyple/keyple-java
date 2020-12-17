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
package org.eclipse.keyple.distributed.impl;

import java.io.IOException;
import java.util.*;
import org.eclipse.keyple.core.card.message.*;
import org.eclipse.keyple.core.card.selection.CardSelector;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols;
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
        getACardRequestList_ISO14443_4(),
        MultiSelectionProcessing.FIRST_MATCH,
        ChannelControl.KEEP_OPEN);
  }

  public static ObservableReader.NotificationMode getNotificationMode() {
    return ObservableReader.NotificationMode.ALWAYS;
  }

  public static List<CardSelectionRequest> getACardRequestList_ISO14443_4() {
    String poAid = "A000000291A000000191";

    CardSelector.AidSelector aidSelector =
        CardSelector.AidSelector.builder() //
            .aidToSelect(poAid) //
            .build();

    CardSelector seSelector =
        CardSelector.builder() //
            .aidSelector(aidSelector) //
            .cardProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name()) //
            .build();

    CardSelectionRequest cardSelectionRequest =
        new CardSelectionRequest(seSelector, getACardRequest());

    return Arrays.asList(cardSelectionRequest);
  }

  public static CardRequest getACardRequest() {
    String poAid = "A000000291A000000191";

    List<ApduRequest> poApduRequests;
    poApduRequests = Arrays.asList(new ApduRequest(ByteArrayUtil.fromHex("9000"), true));

    CardRequest cardRequest = new CardRequest(poApduRequests);
    return cardRequest;
  }

  public static CardResponse getACardResponse() {

    ApduResponse apdu = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());
    List<ApduResponse> apduResponses = Arrays.asList(apdu, apdu);
    return new CardResponse(true, apduResponses);
  }

  public static List<CardSelectionResponse> getCompleteResponseList() {
    ApduResponse apdu = new ApduResponse(ByteArrayUtil.fromHex("9000"), new HashSet<Integer>());
    return Arrays.asList(
        new CardSelectionResponse(new SelectionStatus(null, apdu, true), getACardResponse()),
        new CardSelectionResponse(new SelectionStatus(null, apdu, true), getACardResponse()));
  }

  public static DefaultSelectionsResponse getDefaultSelectionsResponse() {
    return new DefaultSelectionsResponse(getCompleteResponseList());
  }
}
