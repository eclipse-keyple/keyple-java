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
package org.eclipse.keyple.core.plugin.reader;

import java.util.List;
import org.eclipse.keyple.core.card.message.CardRequest;
import org.eclipse.keyple.core.card.message.CardResponse;
import org.eclipse.keyple.core.card.message.CardSelectionRequest;
import org.eclipse.keyple.core.card.message.CardSelectionResponse;
import org.eclipse.keyple.core.card.message.ChannelControl;
import org.eclipse.keyple.core.card.message.ProxyReader;
import org.eclipse.keyple.core.card.selection.MultiSelectionProcessing;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic reader. <code>AbstractReader</code> describes the high-level interface to a {@link
 * ProxyReader} made available by the underlying system.
 *
 * <p><code>AbstractReader</code> defines the minimum required functionality for a local or remote
 * reader and provides some logging facilities.
 */
public abstract class AbstractReader implements ProxyReader {

  /** The name of the reader */
  private final String name;

  /** logger */
  private static final Logger logger = LoggerFactory.getLogger(AbstractReader.class);

  /** Timestamp recorder */
  private long before;

  /** Contains the name of the plugin */
  private final String pluginName;

  /**
   * Reader constructor taking the name of the plugin that instantiated the reader and the name of
   * the reader in argument.
   *
   * <p>Initializes the time measurement log at {@link CardRequest} level. The first measurement
   * gives the time elapsed since the plugin was loaded.
   *
   * @param pluginName A not empty string.
   * @param name A not empty string.
   * @since 0.9
   */
  protected AbstractReader(String pluginName, String name) {

    this.name = name;
    this.pluginName = pluginName;
    if (logger.isDebugEnabled()) {
      this.before = System.nanoTime();
    }
  }

  /**
   * Gets the name of the reader.
   *
   * @return A not empty string.
   */
  public final String getName() {
    return name;
  }

  /**
   * Gets the name of plugin provided in the constructor.
   *
   * @return A not empty string.
   */
  public final String getPluginName() {
    return pluginName;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation of {@link ProxyReader#transmitCardSelectionRequests(List,
   * MultiSelectionProcessing, ChannelControl)} is based on {@link
   * #processCardSelectionRequests(List, MultiSelectionProcessing, ChannelControl)}.<br>
   * It adds a logging of exchanges including a measure of execution time, available at the debug
   * level.
   */
  @Override
  public final List<CardSelectionResponse> transmitCardSelectionRequests(
      List<CardSelectionRequest> cardSelectionRequests,
      MultiSelectionProcessing multiSelectionProcessing,
      ChannelControl channelControl) {

    List<CardSelectionResponse> cardSelectionResponses;

    if (logger.isDebugEnabled()) {
      long timeStamp = System.nanoTime();
      long elapsed10ms = (timeStamp - before) / 100000;
      this.before = timeStamp;
      logger.debug(
          "[{}] transmit => CARDREQUESTLIST = {}, elapsed {} ms.",
          this.getName(),
          cardSelectionRequests,
          elapsed10ms / 10.0);
    }

    try {
      cardSelectionResponses =
          processCardSelectionRequests(
              cardSelectionRequests, multiSelectionProcessing, channelControl);
    } catch (KeypleReaderIOException ex) {
      if (logger.isDebugEnabled()) {
        long timeStamp = System.nanoTime();
        long elapsed10ms = (timeStamp - before) / 100000;
        this.before = timeStamp;
        logger.debug(
            "[{}] transmit => CARDREQUESTLIST IO failure. elapsed {}",
            this.getName(),
            elapsed10ms / 10.0);
      } /* Throw an exception with the responses collected so far. */
      throw ex;
    }

    if (logger.isDebugEnabled()) {
      long timeStamp = System.nanoTime();
      long elapsed10ms = (timeStamp - before) / 100000;
      this.before = timeStamp;
      logger.debug(
          "[{}] transmit => CARDRESPONSELIST = {}, elapsed {} ms.",
          this.getName(),
          cardSelectionResponses,
          elapsed10ms / 10.0);
    }

    return cardSelectionResponses;
  }

  /**
   * This method is the actual implementation of the process of transmitting a list of {@link
   * CardRequest} as defined by {@link ProxyReader#transmitCardSelectionRequests(List,
   * MultiSelectionProcessing, ChannelControl)}.
   *
   * @param cardSelectionRequests A not empty list of not null {@link CardSelectionRequest}.
   * @param multiSelectionProcessing The multi card processing flag (must be not null).
   * @param channelControl indicates if the physical channel has to be closed at the end of the
   *     processing (must be not null).
   * @return A not null response list (can be empty).
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @throws IllegalArgumentException if one of the arguments is null.
   * @throws IllegalStateException in case of configuration inconsistency.
   * @see ProxyReader#transmitCardSelectionRequests(List, MultiSelectionProcessing, ChannelControl)
   * @since 0.9
   */
  protected abstract List<CardSelectionResponse> processCardSelectionRequests(
      List<CardSelectionRequest> cardSelectionRequests,
      MultiSelectionProcessing multiSelectionProcessing,
      ChannelControl channelControl);

  /**
   * {@inheritDoc}
   *
   * <p>This implementation of {@link ProxyReader#transmitCardRequest(CardRequest, ChannelControl)}
   * is based on {@link #processCardRequest(CardRequest, ChannelControl)}.<br>
   * It adds a logging of exchanges including a measure of execution time, available at the debug
   * level.
   */
  @Override
  public final CardResponse transmitCardRequest(
      CardRequest cardRequest, ChannelControl channelControl) {

    CardResponse cardResponse;

    if (logger.isDebugEnabled()) {
      long timeStamp = System.nanoTime();
      long elapsed10ms = (timeStamp - before) / 100000;
      this.before = timeStamp;
      logger.debug(
          "[{}] transmit => CARDREQUEST = {}, elapsed {} ms.",
          this.getName(),
          cardRequest,
          elapsed10ms / 10.0);
    }

    try {
      cardResponse = processCardRequest(cardRequest, channelControl);
    } catch (KeypleReaderIOException ex) {
      if (logger.isDebugEnabled()) {
        long timeStamp = System.nanoTime();
        long elapsed10ms = (timeStamp - before) / 100000;
        this.before = timeStamp;
        logger.debug(
            "[{}] transmit => CARDREQUEST IO failure. elapsed {}",
            this.getName(),
            elapsed10ms / 10.0);
      }
      /* Forward the exception. */
      throw ex;
    }

    if (logger.isDebugEnabled()) {
      long timeStamp = System.nanoTime();
      long elapsed10ms = (timeStamp - before) / 100000;
      this.before = timeStamp;
      logger.debug(
          "[{}] transmit => CARDRESPONSE = {}, elapsed {} ms.",
          this.getName(),
          cardResponse,
          elapsed10ms / 10.0);
    }

    return cardResponse;
  }

  /**
   * This method is the actual implementation of the process of a {@link CardRequest} as defined by
   * {@link ProxyReader#transmitCardRequest(CardRequest, ChannelControl)}
   *
   * @param cardRequest The {@link CardRequest} to be processed (can be null).
   * @return cardResponse A not null {@link CardResponse}.
   * @param channelControl indicates if the physical channel has to be closed at the end of the
   *     processing (must be not null).
   * @throws KeypleReaderIOException if the communication with the reader or the card has failed
   * @throws IllegalArgumentException if one of the arguments is null.
   * @see ProxyReader#transmitCardRequest(CardRequest, ChannelControl)
   * @since 0.9
   */
  protected abstract CardResponse processCardRequest(
      CardRequest cardRequest, ChannelControl channelControl);
}
