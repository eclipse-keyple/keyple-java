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
package org.eclipse.keyple.plugin.remote.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.core.card.message.ProxyReader;
import org.eclipse.keyple.core.card.selection.AbstractSmartCard;
import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.KeypleClientReaderEventFilter;
import org.eclipse.keyple.plugin.remote.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.exception.KeypleDoNotPropagateEventException;
import org.eclipse.keyple.plugin.remote.NativeClientService;
import org.eclipse.keyple.plugin.remote.RemoteServiceParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Singleton instance of the {@link NativeClientService} implementation
 */
final class NativeClientServiceImpl extends AbstractNativeService
    implements ObservableReader.ReaderObserver, NativeClientService {

  private static final Logger logger = LoggerFactory.getLogger(NativeClientServiceImpl.class);

  private static NativeClientServiceImpl uniqueInstance;

  private final boolean withReaderObservation;
  private final KeypleClientReaderEventFilter eventFilter;
  private final Map<String, String> virtualReaders;

  /**
   * (private)<br>
   * Constructor
   *
   * @param withReaderObservation Indicates if reader observation should be activated.
   * @param eventFilter The event filter to use if reader observation should is activated.
   */
  private NativeClientServiceImpl(
      boolean withReaderObservation, KeypleClientReaderEventFilter eventFilter) {
    super();
    this.withReaderObservation = withReaderObservation;
    this.eventFilter = eventFilter;
    this.virtualReaders = new HashMap<String, String>();
  }

  /**
   * (package-private)<br>
   * Create an instance of this singleton service
   *
   * @param withReaderObservation true if reader observation should be activated
   * @return a not null instance of the singleton
   */
  static NativeClientServiceImpl createInstance(
      boolean withReaderObservation, KeypleClientReaderEventFilter eventFilter) {
    uniqueInstance = new NativeClientServiceImpl(withReaderObservation, eventFilter);
    return uniqueInstance;
  }

  /**
   * (package-private)<br>
   * Retrieve the instance of this singleton service
   *
   * @return a not null instance
   */
  static NativeClientServiceImpl getInstance() {
    return uniqueInstance;
  }

  /** {@inheritDoc} */
  @Override
  public <T> T executeRemoteService(RemoteServiceParameters parameters, Class<T> classOfT) {

    // check params
    Assert.getInstance() //
        .notNull(parameters, "parameters") //
        .notNull(classOfT, "classOfT");

    // get nativeReader
    ProxyReader nativeReader = (ProxyReader) parameters.getNativeReader();

    if (logger.isTraceEnabled()) {
      logger.trace(
          "Execute remoteService {} for native reader {}",
          parameters.getServiceId(),
          nativeReader.getName());
    }

    // Generate a new session id
    String sessionId = generateSessionId();

    // build keypleMessageDto EXECUTE_REMOTE_SERVICE with user params
    KeypleMessageDto remoteServiceDto = buildRemoteServiceMessage(parameters, sessionId);

    try {
      // Open a new session on the node
      node.openSession(sessionId);

      // send keypleMessageDto through the node
      KeypleMessageDto receivedDto = node.sendRequest(remoteServiceDto);

      T userOutputData;

      // start observation if needed
      if (withReaderObservation) {
        if (nativeReader instanceof ObservableReader) {

          // Register the virtual reader associated to the native reader.
          virtualReaders.put(nativeReader.getName(), receivedDto.getVirtualReaderName());

          try {
            // Start the observation.
            if (logger.isTraceEnabled()) {
              logger.trace(
                  "Add NativeClientService as an observer for reader {}", nativeReader.getName());
            }
            ((ObservableReader) nativeReader).addObserver(this);

            // Process the entire transaction
            receivedDto = processTransaction(nativeReader, receivedDto);

            // Extract user output data
            userOutputData = extractUserOutputData(receivedDto, classOfT);

            // Verify if the virtual reader can be unregistered.
            if (canUnregisterVirtualReader(receivedDto)) {
              virtualReaders.remove(nativeReader.getName());
            }
          } catch (RuntimeException e) {
            // Unregister the associated virtual reader.
            virtualReaders.remove(nativeReader.getName());
            throw e;
          }
        } else {
          throw new IllegalArgumentException(
              "Observation can not be activated because native reader is not observable");
        }
      } else {
        // Process the entire transaction
        receivedDto = processTransaction(nativeReader, receivedDto);

        // Extract user output data
        userOutputData = extractUserOutputData(receivedDto, classOfT);
      }

      // return userOutputData
      return userOutputData;

    } finally {
      // Close the session
      node.closeSessionSilently(sessionId);
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void onMessage(KeypleMessageDto msg) {
    throw new UnsupportedOperationException("onMessage");
  }

  /**
   * Propagate Reader Events to RemotePlugin (internal use only)
   *
   * @param event The event to be propagated (should be not null)
   * @since 1.0
   */
  @Override
  public void update(ReaderEvent event) {

    Object userData;
    try {
      // Get the user input data from the event filter
      userData = eventFilter.beforePropagation(event);
    } catch (KeypleDoNotPropagateEventException e) {
      // do not throw event
      logger.info(
          "The propagation of the reader event ["
              + event.getEventType().name()
              + "] is cancelled by the user's event filter");
      return;
    }

    try {
      // Get the native reader instance
      ProxyReader nativeReader = findLocalReader(event.getReaderName());

      // Generate a new session id
      String sessionId = generateSessionId();

      // Build an event message with the user data
      KeypleMessageDto eventMessageDto = buildEventMessage(event, userData, sessionId);

      try {
        // Open a new session on the node
        node.openSession(sessionId);

        // send keypleMessageDto through the node
        KeypleMessageDto receivedDto = node.sendRequest(eventMessageDto);

        // Process all the transaction
        receivedDto = processTransaction(nativeReader, receivedDto);

        // extract userOutputData
        Object userOutputData =
            extractUserOutputData(receivedDto, eventFilter.getUserOutputDataClass());

        // Verify if the virtual reader can be unregistered.
        if (canUnregisterVirtualReader(receivedDto)) {
          virtualReaders.remove(nativeReader.getName());
        }

        // invoke callback
        eventFilter.afterPropagation(userOutputData);

      } finally {
        // Close the session
        node.closeSessionSilently(sessionId);
      }

    } catch (RuntimeException e) {
      // Unregister the associated virtual reader.
      virtualReaders.remove(event.getReaderName());
      throw e;
    }
  }

  /**
   * (private)<br>
   * Process the entire transaction.
   *
   * @param nativeReader The native reader.
   * @param receivedDto The first received dto from the server.
   * @return a not null reference.
   * @throws RuntimeException if an error occurs.
   */
  private KeypleMessageDto processTransaction(
      ProxyReader nativeReader, KeypleMessageDto receivedDto) {

    // check server response : while dto is not a terminate service, execute dto locally and send
    // back response.
    while (!receivedDto.getAction().equals(KeypleMessageDto.Action.TERMINATE_SERVICE.name())
        && !receivedDto.getAction().equals(KeypleMessageDto.Action.ERROR.name())) {

      // execute dto request locally
      KeypleMessageDto responseDto = executeLocally(nativeReader, receivedDto);

      // get response dto - send dto response to server
      receivedDto = node.sendRequest(responseDto);
    }

    // Check if the received dto contains an error
    checkError(receivedDto);

    return receivedDto;
  }

  /**
   * (private)<br>
   * Extract the user output data from the provided message.
   *
   * @param msg The message.
   * @param classOfT The class of T.
   * @param <T> The type of the output data.
   * @return null if there is no user data to extract.
   */
  private <T> T extractUserOutputData(KeypleMessageDto msg, Class<T> classOfT) {
    if (classOfT == null) {
      return null;
    }
    Gson parser = KeypleJsonParser.getParser();
    JsonObject body = parser.fromJson(msg.getBody(), JsonObject.class);
    return parser.fromJson(body.get("userOutputData").getAsString(), classOfT);
  }

  /**
   * (private)<br>
   * Verify if the virtual reader associated to the provided message can be unregistered.
   *
   * @param msg The message to analyse.
   * @return true if the virtual reader can be unregistered.
   */
  private boolean canUnregisterVirtualReader(KeypleMessageDto msg) {
    Gson parser = KeypleJsonParser.getParser();
    JsonObject body = parser.fromJson(msg.getBody(), JsonObject.class);
    return parser.fromJson(body.get("unregisterVirtualReader"), Boolean.class);
  }

  /**
   * (private)<br>
   * Build the message associated to the EXECUTE_SERVICE action.
   *
   * @param parameters The main parameters.
   * @param sessionId The session id to use.
   * @return a not null reference.
   */
  private KeypleMessageDto buildRemoteServiceMessage(
      RemoteServiceParameters parameters, String sessionId) {

    JsonObject body = new JsonObject();

    body.addProperty("serviceId", parameters.getServiceId());

    Object userInputData = parameters.getUserInputData();
    if (userInputData != null) {
      body.add("userInputData", KeypleJsonParser.getParser().toJsonTree(userInputData));
    }

    AbstractSmartCard initialCardContent = parameters.getInitialCardContent();
    if (initialCardContent != null) {
      body.add("initialCardContent", KeypleJsonParser.getParser().toJsonTree(initialCardContent));
    }

    body.addProperty(
        "isObservable",
        withReaderObservation && (parameters.getNativeReader() instanceof ObservableReader));

    return new KeypleMessageDto()
        .setSessionId(sessionId)
        .setAction(KeypleMessageDto.Action.EXECUTE_REMOTE_SERVICE.name())
        .setNativeReaderName(parameters.getNativeReader().getName())
        .setBody(body.toString());
  }

  /**
   * (private)<br>
   * Build the message associated to the READER_EVENT action.
   *
   * @param readerEvent The reader event.
   * @param userInputData The user input data.
   * @param sessionId The session id to use.
   * @return a not null reference.
   */
  private KeypleMessageDto buildEventMessage(
      ReaderEvent readerEvent, Object userInputData, String sessionId) {

    JsonObject body = new JsonObject();

    body.add("readerEvent", KeypleJsonParser.getParser().toJsonTree(readerEvent));
    body.add("userInputData", KeypleJsonParser.getParser().toJsonTree(userInputData));

    return new KeypleMessageDto()
        .setSessionId(sessionId)
        .setAction(KeypleMessageDto.Action.READER_EVENT.name())
        .setNativeReaderName(readerEvent.getReaderName())
        .setVirtualReaderName(virtualReaders.get(readerEvent.getReaderName()))
        .setBody(body.toString());
  }
}
