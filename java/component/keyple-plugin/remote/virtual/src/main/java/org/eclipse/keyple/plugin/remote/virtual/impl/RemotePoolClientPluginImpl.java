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
package org.eclipse.keyple.plugin.remote.virtual.impl;

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
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.virtual.RemotePoolClientPlugin;

/** Implementation of the {@link RemotePoolClientPlugin} */
final class RemotePoolClientPluginImpl extends AbstractRemotePlugin
    implements RemotePoolClientPlugin {

  /**
   * (package-private)<br>
   * Constructor.
   *
   * @param name The name of the plugin.
   * @throws KeypleReaderException when an issue is raised with reader
   */
  RemotePoolClientPluginImpl(String name) {
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
      node.openSession(sessionId);

      KeypleMessageDto request =
          new KeypleMessageDto()
              .setAction(KeypleMessageDto.Action.GET_READER_GROUP_REFERENCES.name())
              .setSessionId(sessionId)
              .setBody(null);

      KeypleMessageDto response = node.sendRequest(request);

      checkError(response);
      String readerGroupReferencesJson =
          KeypleJsonParser.getParser()
              .fromJson(response.getBody(), JsonObject.class)
              .get("readerGroupReferences")
              .toString();

      return KeypleJsonParser.getParser().fromJson(readerGroupReferencesJson, SortedSet.class);

    } finally {
      node.closeSessionSilently(sessionId);
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
      node.openSession(sessionId);

      JsonObject body = new JsonObject();
      body.addProperty("groupReference", groupReference);
      KeypleMessageDto request =
          new KeypleMessageDto()
              .setAction(KeypleMessageDto.Action.ALLOCATE_READER.name())
              .setSessionId(sessionId)
              .setBody(body.toString());

      KeypleMessageDto response = node.sendRequest(request);

      checkError(response);
      VirtualReader reader =
          new VirtualReader(
              getName(),
              response.getNativeReaderName(),
              getNode(),
              sessionId,
              response.getClientNodeId());
      readers.put(reader.getName(), reader);

      return reader;
    } catch (RuntimeException e) {
      // in case of error, session is closed
      node.closeSessionSilently(sessionId);
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
      throw new IllegalArgumentException("reader is not a virtual reader of this pool plugin");
    }

    VirtualReader virtualReader = (VirtualReader) reader;

    try {

      KeypleMessageDto request =
          new KeypleMessageDto()
              .setAction(KeypleMessageDto.Action.RELEASE_READER.name())
              .setVirtualReaderName(reader.getName())
              .setNativeReaderName(virtualReader.getNativeReaderName())
              .setSessionId(virtualReader.getSessionId())
              .setBody(null);

      // unregister reader
      readers.remove(reader.getName());

      // it is assumed a session is already open on the node, else an error is thrown
      KeypleMessageDto response = node.sendRequest(request);
      checkError(response);

    } finally {
      // close the session on the node
      node.closeSessionSilently(virtualReader.getSessionId());
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

  @Override
  public void register() {

  }

  @Override
  public void unregister() {

  }

  /**
   * (package-private)<br>
   * Not used in this plugin
   */
  @Override
  protected void onMessage(KeypleMessageDto msg) {
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
