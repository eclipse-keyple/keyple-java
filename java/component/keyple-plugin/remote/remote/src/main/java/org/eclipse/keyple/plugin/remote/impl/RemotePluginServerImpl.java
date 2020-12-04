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

import com.google.gson.JsonObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.event.PluginEvent;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.json.KeypleGsonParser;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.ObservableRemoteReaderServer;
import org.eclipse.keyple.plugin.remote.RemotePluginServer;
import org.eclipse.keyple.plugin.remote.RemoteReaderServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (package-private)<br>
 * Implementation of the {@link RemotePluginServer}.
 *
 * @since 1.0
 */
final class RemotePluginServerImpl extends AbstractRemotePlugin implements RemotePluginServer {

  private static final Logger logger = LoggerFactory.getLogger(RemotePluginServerImpl.class);

  private final ExecutorService eventNotificationPool;

  /** The observers of this object */
  private final List<PluginObserver> observers;

  /**
   * (package-private)<br>
   *
   * <ul>
   *   <li>Instantiates a new RemotePluginServer.
   *   <li>Retrieve the current readers list.
   *   <li>Initialize the list of readers invoking the abstract method initNativeReaders.
   *   <li>When readers initialisation failed, a KeypleReaderException is thrown.
   * </ul>
   *
   * @param name The name of the plugin.
   * @throws KeypleReaderException when an issue is raised with reader
   * @since 1.0
   */
  RemotePluginServerImpl(String name, ExecutorService eventNotificationPool) {
    super(name);
    this.eventNotificationPool = eventNotificationPool;
    this.observers = new ArrayList<PluginObserver>();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  ConcurrentMap<String, Reader> initNativeReaders() throws KeypleReaderIOException {
    return new ConcurrentHashMap<String, Reader>();
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  void onMessage(MessageDto message) {
    switch (MessageDto.Action.valueOf(message.getAction())) {
      case EXECUTE_REMOTE_SERVICE:

        // create a remote reader from message parameters
        final AbstractRemoteReaderServer remoteReader = createMasterReader(message);
        readers.put(remoteReader.getName(), remoteReader);
        notifyObservers(
            new PluginEvent(
                getName(), remoteReader.getName(), PluginEvent.EventType.READER_CONNECTED));
        break;
      case READER_EVENT:
        Assert.getInstance().notNull(message.getRemoteReaderName(), "remoteReaderName");

        ObservableRemoteReaderServerImpl delegateRemoteReader = createSlaveReader(message);
        readers.put(delegateRemoteReader.getName(), delegateRemoteReader);

        // notify observers of this event
        ReaderEvent readerEvent =
            KeypleGsonParser.getParser()
                .fromJson(
                    KeypleGsonParser.getParser()
                        .fromJson(message.getBody(), JsonObject.class)
                        .get("readerEvent"),
                    ReaderEvent.class);

        delegateRemoteReader.notifyObservers(
            new ReaderEvent(
                getName(), // plugin name is overwritten
                delegateRemoteReader.getName(), // reader name is overwritten
                readerEvent.getEventType(),
                readerEvent.getDefaultSelectionsResponse()));

        break;
      default:
        throw new IllegalStateException("Message is not supported by Remote Plugin " + message);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void terminateService(String remoteReaderName, Object userOutputData) {

    AbstractRemoteReaderServer remoteReader =
        (AbstractRemoteReaderServer) getReader(remoteReaderName);

    // keep remote reader if observable and has observers
    Boolean unregisterRemoteReader = false;

    if (!(remoteReader instanceof ObservableRemoteReaderServer)) {
      // not a observable, remove it and unregister
      unregisterRemoteReader = true;
      readers.remove(remoteReader.getName());
    } else {
      ObservableRemoteReaderServerImpl observableReader =
          (ObservableRemoteReaderServerImpl) remoteReader;
      if (observableReader.getMasterReader() != null) {
        // is observer and slave, remove it
        readers.remove(remoteReader.getName());
        if (observableReader.countObservers() == 0) {
          // and master has no observer, remove and unregister master
          readers.remove(observableReader.getMasterReader().getName());
          unregisterRemoteReader = true;
        }
      } else {
        // is master
        if (observableReader.countObservers() == 0) {
          // has no observer, remove it, unregister
          readers.remove(remoteReader.getName());
          unregisterRemoteReader = true;
        }
      }
    }

    JsonObject body = new JsonObject();
    body.addProperty("userOutputData", KeypleGsonParser.getParser().toJson(userOutputData));
    body.addProperty("unregisterRemoteReader", unregisterRemoteReader);

    // Build the message
    MessageDto message =
        new MessageDto() //
            .setAction(MessageDto.Action.TERMINATE_SERVICE.name()) //
            .setRemoteReaderName(remoteReaderName) //
            .setSessionId(remoteReader.getSessionId()) //
            .setClientNodeId(remoteReader.getClientNodeId()) //
            .setBody(body.toString());

    // Send the message
    node.sendMessage(message);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public RemoteReaderServer getReader(String name) throws KeypleReaderNotFoundException {
    Assert.getInstance().notNull(name, "reader name");
    RemoteReaderServer seReader = (RemoteReaderServer) readers.get(name);
    if (seReader == null) {
      throw new KeypleReaderNotFoundException(name);
    }
    return seReader;
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void addObserver(PluginObserver observer) {
    Assert.getInstance().notNull(observer, "Plugin Observer");
    if (observers.add(observer) && logger.isTraceEnabled()) {
      logger.trace(
          "[{}] Added plugin observer '{}'", getName(), observer.getClass().getSimpleName());
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void removeObserver(PluginObserver observer) {
    Assert.getInstance().notNull(observer, "Plugin Observer");
    if (observers.remove(observer) && logger.isTraceEnabled()) {
      logger.trace(
          "[{}] Removed plugin observer '{}'", getName(), observer.getClass().getSimpleName());
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void clearObservers() {
    observers.clear();
    if (logger.isTraceEnabled()) {
      logger.trace("[{}] Clear reader observers", this.getName());
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public int countObservers() {
    return observers.size();
  }

  /**
   * (private)<br>
   * Notify observers of an event. Each observer is notified in a separate thread.
   *
   * @param event non nullable instance of event
   */
  private void notifyObservers(final PluginEvent event) {
    for (final PluginObserver observer : observers) {
      eventNotificationPool.execute(
          new Runnable() {
            @Override
            public void run() {
              observer.update(event);
            }
          });
    }
  }

  /**
   * (private)<br>
   * Create a server remote reader based on incoming message. Can be an observable or not.
   *
   * @param message incoming message
   * @return non null instance of AbstractRemoteReaderServer
   */
  private AbstractRemoteReaderServer createMasterReader(MessageDto message) {

    final JsonObject body =
        KeypleGsonParser.getParser().fromJson(message.getBody(), JsonObject.class);
    final String serviceId = body.get("serviceId").getAsString();
    final String userInputData =
        body.has("userInputData") ? body.get("userInputData").toString() : null;
    final String initialCardContent =
        body.has("initialCardContent") ? body.get("initialCardContent").toString() : null;
    boolean isObservable = body.has("isObservable") && body.get("isObservable").getAsBoolean();
    final String remoteReaderName = UUID.randomUUID().toString();
    final String sessionId = message.getSessionId();
    final String clientNodeId = message.getClientNodeId();

    if (logger.isTraceEnabled()) {
      logger.trace(
          "[{}] Create a remote reader {} with serviceId:{} and isObservable:{} for sessionId:{} for clientNodeId:{}",
          this.getName(),
          remoteReaderName,
          serviceId,
          isObservable,
          sessionId,
          clientNodeId);
    }
    if (isObservable) {
      ObservableRemoteReaderImpl observableRemoteReaderImpl =
          new ObservableRemoteReaderImpl(
              getName(), remoteReaderName, node, sessionId, clientNodeId, eventNotificationPool);
      return new ObservableRemoteReaderServerImpl(
          observableRemoteReaderImpl, serviceId, userInputData, initialCardContent, null);
    } else {
      RemoteReaderImpl remoteReaderImpl =
          new RemoteReaderImpl(getName(), remoteReaderName, node, sessionId, clientNodeId);
      return new RemoteReaderServerImpl(
          remoteReaderImpl, serviceId, userInputData, initialCardContent);
    }
  }

  /**
   * (private)<br>
   * Create a reader to handle the communication in the session of the event notification
   *
   * @param message incoming reader event message
   * @return non null instance of a ObservableRemoteReaderServerImpl
   */
  private ObservableRemoteReaderServerImpl createSlaveReader(MessageDto message) {

    final ObservableRemoteReaderServerImpl observableRemoteReaderServer =
        (ObservableRemoteReaderServerImpl) getReader(message.getRemoteReaderName());
    final JsonObject body =
        KeypleGsonParser.getParser().fromJson(message.getBody(), JsonObject.class);

    String userInputData = body.has("userInputData") ? body.get("userInputData").toString() : null;

    ObservableRemoteReaderImpl observableRemoteReader =
        new ObservableRemoteReaderImpl(
            getName(),
            UUID.randomUUID().toString(),
            node,
            message.getSessionId(),
            message.getClientNodeId(),
            eventNotificationPool);
    // create a temporary remote reader for this event
    return new ObservableRemoteReaderServerImpl(
        observableRemoteReader, null, userInputData, null, observableRemoteReaderServer);
  }
}
