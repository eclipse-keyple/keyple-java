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
package org.eclipse.keyple.core.seproxy.plugin.reader;

import java.util.List;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
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
   * <p>Note: the time measurement is initialized.
   *
   * @param pluginName A not empty string.
   * @param name A not empty string.
   */
  protected AbstractReader(String pluginName, String name) {
    this.name = name;
    this.pluginName = pluginName;
    this.before = System.nanoTime(); /*
                                          * provides an initial value for measuring the
                                          * inter-exchange time. The first measurement gives the
                                          * time elapsed since the plugin was loaded.
                                          */
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
   * <p>This implementation of {@link ProxyReader#transmitSeRequests(List, MultiSeRequestProcessing,
   * ChannelControl)} is based on {@link #processSeRequests(List, MultiSeRequestProcessing,
   * ChannelControl)}.<br>
   * It adds a logging of exchanges including a measure of execution time, available at the debug
   * level.
   */
  @Override
  public final List<SeResponse> transmitSeRequests(
      List<SeRequest> seRequests,
      MultiSeRequestProcessing multiSeRequestProcessing,
      ChannelControl channelControl) {

    List<SeResponse> seResponses;

    if (logger.isDebugEnabled()) {
      long timeStamp = System.nanoTime();
      long elapsed10ms = (timeStamp - before) / 100000;
      this.before = timeStamp;
      logger.debug(
          "[{}] transmit => SEREQUESTLIST = {}, elapsed {} ms.",
          this.getName(),
          seRequests,
          elapsed10ms / 10.0);
    }

    try {
      seResponses = processSeRequests(seRequests, multiSeRequestProcessing, channelControl);
    } catch (KeypleReaderIOException ex) {
      if (logger.isDebugEnabled()) {
        long timeStamp = System.nanoTime();
        long elapsed10ms = (timeStamp - before) / 100000;
        this.before = timeStamp;
        logger.debug(
            "[{}] transmit => SEREQUESTLIST IO failure. elapsed {}",
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
          "[{}] transmit => SERESPONSELIST = {}, elapsed {} ms.",
          this.getName(),
          seResponses,
          elapsed10ms / 10.0);
    }

    return seResponses;
  }

  /**
   * This method is the actual implementation of the process of transmitting a list of {@link
   * SeRequest} as defined by {@link ProxyReader#transmitSeRequests(List, MultiSeRequestProcessing,
   * ChannelControl)}.
   *
   * @param seRequests A not empty list of not null {@link SeRequest}.
   * @param multiSeRequestProcessing The multi se processing flag (must be not null).
   * @param channelControl indicates if the physical channel has to be closed at the end of the
   *     processing (must be not null).
   * @return A not null response list (can be empty).
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   * @throws IllegalArgumentException if one of the arguments is null.
   * @see ProxyReader#transmitSeRequests(List, MultiSeRequestProcessing, ChannelControl)
   * @since 0.9
   */
  protected abstract List<SeResponse> processSeRequests(
      List<SeRequest> seRequests,
      MultiSeRequestProcessing multiSeRequestProcessing,
      ChannelControl channelControl);

  /**
   * {@inheritDoc}
   *
   * <p>This implementation of {@link ProxyReader#transmitSeRequest(SeRequest, ChannelControl)} is
   * based on {@link #processSeRequest(SeRequest, ChannelControl)}.<br>
   * It adds a logging of exchanges including a measure of execution time, available at the debug
   * level.
   */
  @Override
  public final SeResponse transmitSeRequest(SeRequest seRequest, ChannelControl channelControl) {

    SeResponse seResponse;

    if (logger.isDebugEnabled()) {
      long timeStamp = System.nanoTime();
      long elapsed10ms = (timeStamp - before) / 100000;
      this.before = timeStamp;
      logger.debug(
          "[{}] transmit => SEREQUEST = {}, elapsed {} ms.",
          this.getName(),
          seRequest,
          elapsed10ms / 10.0);
    }

    try {
      seResponse = processSeRequest(seRequest, channelControl);
    } catch (KeypleReaderIOException ex) {
      if (logger.isDebugEnabled()) {
        long timeStamp = System.nanoTime();
        long elapsed10ms = (timeStamp - before) / 100000;
        this.before = timeStamp;
        logger.debug(
            "[{}] transmit => SEREQUEST IO failure. elapsed {}",
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
          "[{}] transmit => SERESPONSE = {}, elapsed {} ms.",
          this.getName(),
          seResponse,
          elapsed10ms / 10.0);
    }

    return seResponse;
  }

  /**
   * This method is the actual implementation of the process of a {@link SeRequest} as defined by
   * {@link ProxyReader#transmitSeRequest(SeRequest, ChannelControl)}
   *
   * @param seRequest The {@link SeRequest} to be processed (can be null).
   * @return seResponse A not null {@link SeResponse}.
   * @param channelControl indicates if the physical channel has to be closed at the end of the
   *     processing (must be not null).
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   * @throws IllegalArgumentException if one of the arguments is null.
   * @see ProxyReader#transmitSeRequest(SeRequest, ChannelControl)
   * @since 0.9
   */
  protected abstract SeResponse processSeRequest(
      SeRequest seRequest, ChannelControl channelControl);
}
