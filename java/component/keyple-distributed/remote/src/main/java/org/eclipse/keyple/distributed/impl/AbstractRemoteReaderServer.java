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

import java.util.List;
import org.eclipse.keyple.core.card.message.*;
import org.eclipse.keyple.core.card.selection.AbstractSmartCard;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.json.KeypleGsonParser;
import org.eclipse.keyple.distributed.RemoteReaderServer;

/**
 * (package-private)<br>
 * Abstract Remote Reader Server class.<br>
 * This class is a decorator of a {@link AbstractRemoteReader}.
 *
 * @since 1.0
 */
abstract class AbstractRemoteReaderServer implements RemoteReaderServer, ProxyReader {

  private final AbstractRemoteReader reader;
  private final String serviceId;
  private final String initialCardContentJson;
  private final String userInputDataJson;

  /**
   * (package-private)<br>
   *
   * @param reader The reader to decorate (must be not null).
   * @param serviceId The service id (must be not null).
   * @param userInputDataJson The user input data as a JSON string (optional).
   * @param initialCardContentJson The initial card content as a JSON string (optional).
   * @since 1.0
   */
  AbstractRemoteReaderServer(
      AbstractRemoteReader reader,
      String serviceId,
      String userInputDataJson,
      String initialCardContentJson) {
    this.reader = reader;
    this.serviceId = serviceId;
    this.userInputDataJson = userInputDataJson;
    this.initialCardContentJson = initialCardContentJson;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public List<CardSelectionResponse> transmitCardSelectionRequests(
      List<CardSelectionRequest> cardSelectionRequests,
      MultiSelectionProcessing multiSelectionProcessing,
      ChannelControl channelControl) {
    return reader.transmitCardSelectionRequests(
        cardSelectionRequests, multiSelectionProcessing, channelControl);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public CardResponse transmitCardRequest(CardRequest cardRequest, ChannelControl channelControl) {
    return reader.transmitCardRequest(cardRequest, channelControl);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public String getServiceId() {
    return serviceId;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public <T> T getUserInputData(Class<T> classOfT) {
    Assert.getInstance().notNull(classOfT, "classOfT");
    return userInputDataJson != null
        ? KeypleGsonParser.getParser().fromJson(userInputDataJson, classOfT)
        : null;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public <T extends AbstractSmartCard> T getInitialCardContent(Class<T> classOfT) {
    Assert.getInstance().notNull(classOfT, "classOfT");
    return initialCardContentJson != null
        ? KeypleGsonParser.getParser().fromJson(initialCardContentJson, classOfT)
        : null;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public boolean isCardPresent() {
    return reader.isCardPresent();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public String getName() {
    return reader.getName();
  }

  /**
   * (package-private)
   *
   * @return non null instance of a sessionId
   */
  String getSessionId() {
    return reader.getSessionId();
  }

  /**
   * (package-private)
   *
   * @return non null instance of a sessionId
   */
  String getClientNodeId() {
    return reader.getClientNodeId();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void releaseChannel() {
    reader.releaseChannel();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void activateProtocol(String readerProtocolName, String applicationProtocolName) {
    throw new UnsupportedOperationException(
        "activateProtocol method is not implemented in plugin remote, use it only locally");
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void deactivateProtocol(String readerProtocolName) {
    throw new UnsupportedOperationException(
        "deactivateProtocol method is not implemented in plugin remote, use it only locally");
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public boolean isContactless() {
    return reader.isContactless();
  }
}
