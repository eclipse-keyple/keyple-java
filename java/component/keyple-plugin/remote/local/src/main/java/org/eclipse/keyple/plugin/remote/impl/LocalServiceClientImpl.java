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
import org.eclipse.keyple.core.util.json.KeypleGsonParser;
import org.eclipse.keyple.plugin.remote.LocalServiceClient;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.RemoteServiceParameters;
import org.eclipse.keyple.plugin.remote.spi.DoNotPropagateEventException;
import org.eclipse.keyple.plugin.remote.spi.ObservableReaderEventFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Implementation of the {@link LocalServiceClient}.
 *
 * @since 1.0
 */
final class LocalServiceClientImpl extends AbstractLocalService
    implements ObservableReader.ReaderObserver, LocalServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(LocalServiceClientImpl.class);

  private static Map<String, LocalServiceClientImpl> serviceByName;

  private final boolean withReaderObservation;
  private final ObservableReaderEventFilter eventFilter;
  private final Map<String, String> remoteReaderByLocalName;

  /**
   * (private)<br>
   *
   * @param withReaderObservation Indicates if reader observation should be activated.
   * @param eventFilter The event filter to use if reader observation should is activated.
   * @since 1.0
   */
  private LocalServiceClientImpl(
      boolean withReaderObservation, ObservableReaderEventFilter eventFilter) {
    super();
    this.withReaderObservation = withReaderObservation;
    this.eventFilter = eventFilter;
    this.remoteReaderByLocalName = new HashMap<String, String>();
  }

  /**
   * (package-private)<br>
   * Creates an instance of the service.
   *
   * @param serviceName The identifier of the local service.
   * @param withReaderObservation Indicates if the reader observation should be activated.
   * @return A not null instance.
   * @throws IllegalArgumentException If a service already exists with the provided serviceName.
   * @since 1.0
   */
  static LocalServiceClientImpl createInstance(
      String serviceName, boolean withReaderObservation, ObservableReaderEventFilter eventFilter) {

    if (serviceByName == null) {
      serviceByName = new HashMap<String, LocalServiceClientImpl>();
    }
    if (serviceByName.containsKey(serviceName)) {
      throw new IllegalArgumentException(
          "A LocalServiceClient already exists with the same name : " + serviceName);
    }
    LocalServiceClientImpl localService =
        new LocalServiceClientImpl(withReaderObservation, eventFilter);
    serviceByName.put(serviceName, localService);
    return localService;
  }

  /**
   * (package-private)<br>
   * Retrieves the service having the provided service name.
   *
   * @param serviceName The identifier of the local service.
   * @return A not null reference.
   * @throws IllegalStateException If there's no service having the provided name.
   * @since 1.0
   */
  static LocalServiceClientImpl getInstance(String serviceName) {
    if (!serviceByName.containsKey(serviceName)) {
      throw new IllegalStateException(
          "No LocalServiceClient could be found with the provided name : " + serviceName);
    }
    return serviceByName.get(serviceName);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public <T> T executeRemoteService(RemoteServiceParameters parameters, Class<T> classOfT) {

    // check params
    Assert.getInstance() //
        .notNull(parameters, "parameters") //
        .notNull(classOfT, "classOfT");

    // get localReader
    ProxyReader localReader = (ProxyReader) parameters.getLocalReader();

    if (logger.isTraceEnabled()) {
      logger.trace(
          "Execute remoteService {} for local reader {}",
          parameters.getServiceId(),
          localReader.getName());
    }

    // Generate a new session id
    String sessionId = generateSessionId();

    // build messageDto EXECUTE_REMOTE_SERVICE with user params
    MessageDto remoteServiceDto = buildRemoteServiceMessage(parameters, sessionId);

    try {
      // Open a new session on the node
      node.openSession(sessionId);

      // send messageDto through the node
      MessageDto receivedDto = node.sendRequest(remoteServiceDto);

      T userOutputData;

      // start observation if needed
      if (withReaderObservation) {
        if (localReader instanceof ObservableReader) {

          // Register the remote reader associated to the local reader.
          remoteReaderByLocalName.put(localReader.getName(), receivedDto.getRemoteReaderName());

          try {
            // Start the observation.
            if (logger.isTraceEnabled()) {
              logger.trace(
                  "Add LocalServiceClient as an observer for reader {}", localReader.getName());
            }
            ((ObservableReader) localReader).addObserver(this);

            // Process the entire transaction
            receivedDto = processTransaction(localReader, receivedDto);

            // Extract user output data
            userOutputData = extractUserOutputData(receivedDto, classOfT);

            // Verify if the remote reader can be unregistered.
            if (canUnregisterRemoteReader(receivedDto)) {
              remoteReaderByLocalName.remove(localReader.getName());
            }
          } catch (RuntimeException e) {
            // Unregister the associated remote reader.
            remoteReaderByLocalName.remove(localReader.getName());
            throw e;
          }
        } else {
          throw new IllegalArgumentException(
              "Observation can not be activated because local reader is not observable");
        }
      } else {
        // Process the entire transaction
        receivedDto = processTransaction(localReader, receivedDto);

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

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  void onMessage(MessageDto msg) {
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
    } catch (DoNotPropagateEventException e) {
      // do not throw event
      logger.info(
          "The propagation of the reader event ["
              + event.getEventType().name()
              + "] is cancelled by the user's event filter");
      return;
    }

    try {
      // Get the local reader instance
      ProxyReader localReader = findLocalReader(event.getReaderName());

      // Generate a new session id
      String sessionId = generateSessionId();

      // Build an event message with the user data
      MessageDto eventMessageDto = buildEventMessage(event, userData, sessionId);

      try {
        // Open a new session on the node
        node.openSession(sessionId);

        // send messageDto through the node
        MessageDto receivedDto = node.sendRequest(eventMessageDto);

        // Process all the transaction
        receivedDto = processTransaction(localReader, receivedDto);

        // extract userOutputData
        Object userOutputData =
            extractUserOutputData(receivedDto, eventFilter.getUserOutputDataClass());

        // Verify if the remote reader can be unregistered.
        if (canUnregisterRemoteReader(receivedDto)) {
          remoteReaderByLocalName.remove(localReader.getName());
        }

        // invoke callback
        eventFilter.afterPropagation(userOutputData);

      } finally {
        // Close the session
        node.closeSessionSilently(sessionId);
      }

    } catch (RuntimeException e) {
      // Unregister the associated remote reader.
      remoteReaderByLocalName.remove(event.getReaderName());
      throw e;
    }
  }

  /**
   * (private)<br>
   * Process the entire transaction.
   *
   * @param localReader The local reader.
   * @param receivedDto The first received dto from the server.
   * @return a not null reference.
   * @throws RuntimeException if an error occurs.
   */
  private MessageDto processTransaction(ProxyReader localReader, MessageDto receivedDto) {

    // check server response : while dto is not a terminate service, execute dto locally and send
    // back response.
    while (!receivedDto.getAction().equals(MessageDto.Action.TERMINATE_SERVICE.name())
        && !receivedDto.getAction().equals(MessageDto.Action.ERROR.name())) {

      // execute dto request locally
      MessageDto responseDto = executeLocally(localReader, receivedDto);

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
  private <T> T extractUserOutputData(MessageDto msg, Class<T> classOfT) {
    if (classOfT == null) {
      return null;
    }
    Gson parser = KeypleGsonParser.getParser();
    JsonObject body = parser.fromJson(msg.getBody(), JsonObject.class);
    return parser.fromJson(body.get("userOutputData").getAsString(), classOfT);
  }

  /**
   * (private)<br>
   * Verify if the remote reader associated to the provided message can be unregistered.
   *
   * @param msg The message to analyse.
   * @return true if the remote reader can be unregistered.
   */
  private boolean canUnregisterRemoteReader(MessageDto msg) {
    Gson parser = KeypleGsonParser.getParser();
    JsonObject body = parser.fromJson(msg.getBody(), JsonObject.class);
    return parser.fromJson(body.get("unregisterRemoteReader"), Boolean.class);
  }

  /**
   * (private)<br>
   * Build the message associated to the EXECUTE_SERVICE action.
   *
   * @param parameters The main parameters.
   * @param sessionId The session id to use.
   * @return a not null reference.
   */
  private MessageDto buildRemoteServiceMessage(
      RemoteServiceParameters parameters, String sessionId) {

    JsonObject body = new JsonObject();

    body.addProperty("serviceId", parameters.getServiceId());

    Object userInputData = parameters.getUserInputData();
    if (userInputData != null) {
      body.add("userInputData", KeypleGsonParser.getParser().toJsonTree(userInputData));
    }

    AbstractSmartCard initialCardContent = parameters.getInitialCardContent();
    if (initialCardContent != null) {
      body.add("initialCardContent", KeypleGsonParser.getParser().toJsonTree(initialCardContent));
    }

    body.addProperty(
        "isObservable",
        withReaderObservation && (parameters.getLocalReader() instanceof ObservableReader));

    return new MessageDto()
        .setSessionId(sessionId)
        .setAction(MessageDto.Action.EXECUTE_REMOTE_SERVICE.name())
        .setLocalReaderName(parameters.getLocalReader().getName())
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
  private MessageDto buildEventMessage(
      ReaderEvent readerEvent, Object userInputData, String sessionId) {

    JsonObject body = new JsonObject();

    body.add("readerEvent", KeypleGsonParser.getParser().toJsonTree(readerEvent));
    body.add("userInputData", KeypleGsonParser.getParser().toJsonTree(userInputData));

    return new MessageDto()
        .setSessionId(sessionId)
        .setAction(MessageDto.Action.READER_EVENT.name())
        .setLocalReaderName(readerEvent.getReaderName())
        .setRemoteReaderName(remoteReaderByLocalName.get(readerEvent.getReaderName()))
        .setBody(body.toString());
  }
}
