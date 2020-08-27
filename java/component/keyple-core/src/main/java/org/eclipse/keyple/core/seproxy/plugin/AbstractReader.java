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
package org.eclipse.keyple.core.seproxy.plugin;

import java.util.List;
import java.util.Map;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.ChannelControl;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract definition of an observable reader.
 *
 * <ul>
 *   <li>High level logging and benchmarking of Set of SeRequest and SeRequest transmission
 *   <li>Name-based comparison of ProxyReader (required for SortedSet&lt;ProxyReader&gt;)
 *   <li>Plugin naming management
 * </ul>
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
   * Reader constructor
   *
   * <p>Initialize the time measurement
   *
   * @param pluginName the name of the plugin that instantiated the reader
   * @param name the name of the reader
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

  /** @return the name of the reader */
  public final String getName() {
    return name;
  }

  /**
   * Gets the name of plugin provided in the constructor.
   *
   * <p>The method will be used particularly for logging purposes. The plugin name is also part of
   * the ReaderEvent and PluginEvent objects.
   *
   * @return the plugin name String
   */
  public final String getPluginName() {
    return pluginName;
  }

  /**
   * Execute the transmission of a list of {@link SeRequest} and returns a list of {@link
   * SeResponse}
   *
   * <p>The {@link MultiSeRequestProcessing} parameter indicates whether all requests are to be sent
   * regardless of their result (PROCESS_ALL) or whether the process should stop at the first
   * request whose result is a success (FIRST_MATCH).
   *
   * <p>The {@link ChannelControl} parameter specifies whether the physical channel should be closed
   * (CLOSE_AFTER) or not (KEEP_OPEN) after all requests have been transmitted.
   *
   * <p>The global execution time (inter-exchange and communication) and the Set of SeRequest
   * content is logged (DEBUG level).
   *
   * <p>As the method is final, it cannot be extended.
   *
   * @param seRequests the request set
   * @param multiSeRequestProcessing the multi SE request processing mode
   * @param channelControl the channel control indicator
   * @return the response set
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  @Override
  public final List<SeResponse> transmitSeRequests(
      List<SeRequest> seRequests,
      MultiSeRequestProcessing multiSeRequestProcessing,
      ChannelControl channelControl) {
    if (seRequests == null && channelControl == ChannelControl.KEEP_OPEN) {
      throw new IllegalArgumentException(
          "The request list must not be null when the channel is to remain open.");
    }

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
   * Abstract method implemented by the AbstractLocalReader and VirtualReader classes.
   *
   * <p>This method is handled by transmitSet.
   *
   * @param seRequests a {@link List} of {@link SeRequest} to be processed
   * @param multiSeRequestProcessing the multi se processing mode
   * @param channelControl indicates if the channel has to be closed at the end of the processing
   * @return the List of {@link SeResponse} (responses to the Set of {@link SeRequest})
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  protected abstract List<SeResponse> processSeRequests(
      List<SeRequest> seRequests,
      MultiSeRequestProcessing multiSeRequestProcessing,
      ChannelControl channelControl);

  /**
   * Execute the transmission of a {@link SeRequest} and returns a {@link SeResponse}
   *
   * <p>The individual execution time (inter-exchange and communication) and the {@link SeRequest}
   * content is logged (DEBUG level).
   *
   * <p>As the method is final, it cannot be extended.
   *
   * @param seRequest the request to be transmitted
   * @param channelControl indicates if the channel has to be closed at the end of the processing
   * @return the received response
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  @Override
  public final SeResponse transmitSeRequest(SeRequest seRequest, ChannelControl channelControl) {
    if (seRequest == null && channelControl == ChannelControl.KEEP_OPEN) {
      throw new IllegalArgumentException(
          "The request must not be null when the channel is to remain open.");
    }

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
      /* Throw an exception with the responses collected so far (ex.getSeResponse()). */
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
   * Abstract method implemented by the AbstractLocalReader and VirtualReader classes.
   *
   * <p>This method is handled by transmit.
   *
   * @param seRequest the {@link SeRequest} to be processed
   * @param channelControl a flag indicating if the channel has to be closed after the processing of
   *     the {@link SeRequest}
   * @return the {@link SeResponse} (responses to the {@link SeRequest})
   * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
   */
  protected abstract SeResponse processSeRequest(
      SeRequest seRequest, ChannelControl channelControl);

  /**
   * Sets at once a set of parameters for the reader
   *
   * <p>See {@link #setParameter(String, String)} for more details
   *
   * @param parameters a Map &lt;String, String&gt; parameter set
   * @throws KeypleException if one of the parameters could not be set up
   */
  public final void setParameters(Map<String, String> parameters) {
    for (Map.Entry<String, String> en : parameters.entrySet()) {
      setParameter(en.getKey(), en.getValue());
    }
  }
}
