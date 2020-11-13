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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.service.exception.KeypleReaderException;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.PoolRemotePluginClient;

/** Implementation of the {@link PoolRemotePluginClient} */
final class PoolRemotePluginClientImpl extends AbstractRemotePlugin
    implements PoolRemotePluginClient {

  /**
   * (package-private)<br>
   * Constructor.
   *
   * @param name The name of the plugin.
   * @throws KeypleReaderException when an issue is raised with reader
   */
  PoolRemotePluginClientImpl(String name) {
    super(name);
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public SortedSet<String> getReaderGroupReferences() {
    String sessionId = generateSessionId();
    try {
      // Open a new session on the node, session will be closed at the end of this operation
      getNode().openSession(sessionId);

      MessageDto request =
          new MessageDto()
              .setAction(MessageDto.Action.GET_READER_GROUP_REFERENCES.name())
              .setSessionId(sessionId)
              .setBody(null);

      MessageDto response = getNode().sendRequest(request);

      checkError(response);
      String readerGroupReferencesJson =
          KeypleJsonParser.getParser()
              .fromJson(response.getBody(), JsonObject.class)
              .get("readerGroupReferences")
              .toString();

      return KeypleJsonParser.getParser().fromJson(readerGroupReferencesJson, SortedSet.class);

    } finally {
      getNode().closeSessionSilently(sessionId);
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public Reader allocateReader(String groupReference) {
    String sessionId = generateSessionId();
    try {
      // Open a new session on the node, session will be closed with the release reader method
      getNode().openSession(sessionId);

      JsonObject body = new JsonObject();
      body.addProperty("groupReference", groupReference);
      MessageDto request =
          new MessageDto()
              .setAction(MessageDto.Action.ALLOCATE_READER.name())
              .setSessionId(sessionId)
              .setBody(body.toString());

      MessageDto response = getNode().sendRequest(request);

      checkError(response);
      RemoteReaderImpl reader =
          new RemoteReaderImpl(
              getName(),
              response.getLocalReaderName(),
              getNode(),
              sessionId,
              response.getClientNodeId());
      reader.register();
      readers.put(reader.getName(), reader);

      return reader;
    } catch (RuntimeException e) {
      // in case of error, session is closed
      getNode().closeSessionSilently(sessionId);
      throw e;
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public void releaseReader(Reader reader) {
    Assert.getInstance().notNull(reader, "reader");
    if (!readers.containsKey(reader.getName())) {
      throw new IllegalArgumentException("reader is not a remote reader of this pool plugin");
    }

    RemoteReaderImpl remoteReaderImpl = (RemoteReaderImpl) reader;

    try {

      MessageDto request =
          new MessageDto()
              .setAction(MessageDto.Action.RELEASE_READER.name())
              .setRemoteReaderName(reader.getName())
              .setLocalReaderName(remoteReaderImpl.getLocalReaderName())
              .setSessionId(remoteReaderImpl.getSessionId())
              .setBody(null);

      // unregister reader
      readers.remove(reader.getName());

      // it is assumed a session is already open on the node, else an error is thrown
      MessageDto response = getNode().sendRequest(request);
      checkError(response);

    } finally {
      // close the session on the node
      getNode().closeSessionSilently(remoteReaderImpl.getSessionId());
    }
  }

  /**
   * {@inheritDoc}
   *
   * @since 1.0
   */
  @Override
  public Reader getReader(String name) {
    Assert.getInstance().notNull(name, "reader name");
    Reader seReader = readers.get(name);
    if (seReader == null) {
      throw new KeypleReaderNotFoundException(name);
    }
    return seReader;
  }

  /**
   * (package-private)<br>
   * Not used in this plugin
   */
  @Override
  void onMessage(MessageDto msg) {
    // not used
    throw new UnsupportedOperationException("onMessage method is not supported by this plugin");
  }

  /**
   * (package-private)<br>
   * Initialize the readers map
   */
  @Override
  protected Map<String, Reader> initNativeReaders() throws KeypleReaderIOException {
    return new HashMap<String, Reader>();
  }
}
