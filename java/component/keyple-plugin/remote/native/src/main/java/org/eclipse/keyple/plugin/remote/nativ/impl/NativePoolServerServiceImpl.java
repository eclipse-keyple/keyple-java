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
package org.eclipse.keyple.plugin.remote.nativ.impl;

import com.google.gson.JsonObject;
import java.util.SortedSet;
import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.nativ.NativePoolServerService;

/**
 * Implementation of the {@link NativePoolServerService}. This object is a singleton created by the
 * {@link NativePoolServerServiceFactory}
 */
class NativePoolServerServiceImpl extends AbstractNativeService implements NativePoolServerService {

  private final ReaderPoolPlugin poolPlugin;

  private static NativePoolServerServiceImpl uniqueInstance;

  private NativePoolServerServiceImpl(ReaderPoolPlugin poolPlugin) {
    this.poolPlugin = poolPlugin;
  }

  /**
   * (package-private)<br>
   * Create an instance of this singleton service
   *
   * @param poolPlugin pool plugin
   * @return a not null instance of the singleton
   */
  static NativePoolServerServiceImpl createInstance(ReaderPoolPlugin poolPlugin) {
    uniqueInstance = new NativePoolServerServiceImpl(poolPlugin);
    return uniqueInstance;
  }

  /**
   * (package-private)<br>
   * Retrieve the instance of this singleton service
   *
   * @return a not null instance
   */
  static NativePoolServerServiceImpl getInstance() {
    return uniqueInstance;
  }

  @Override
  protected void onMessage(KeypleMessageDto msg) {
    KeypleMessageDto response;
    SeReader reader;
    try {
      switch (KeypleMessageDto.Action.valueOf(msg.getAction())) {
        case ALLOCATE_READER:
          String groupReference =
              KeypleJsonParser.getParser()
                  .fromJson(msg.getBody(), JsonObject.class)
                  .get("groupReference")
                  .getAsString();
          reader = poolPlugin.allocateReader(groupReference);
          response = new KeypleMessageDto(msg).setNativeReaderName(reader.getName()).setBody(null);
          break;
        case RELEASE_READER:
          reader = poolPlugin.getReader(msg.getNativeReaderName());
          poolPlugin.releaseReader(reader);
          response = new KeypleMessageDto(msg).setBody(null);
          break;
        case GET_READER_GROUP_REFERENCES:
          SortedSet<String> groupReferences = poolPlugin.getReaderGroupReferences();
          JsonObject body = new JsonObject();
          body.add(
              "readerGroupReferences", KeypleJsonParser.getParser().toJsonTree(groupReferences));
          response = new KeypleMessageDto(msg).setBody(body.toString());
          break;
        default:
          ProxyReader proxyReader = (ProxyReader) poolPlugin.getReader(msg.getNativeReaderName());
          response = executeLocally(proxyReader, msg);
          break;
      }
    } catch (KeypleException e) {
      response =
          new KeypleMessageDto(msg) //
              .setAction(KeypleMessageDto.Action.ERROR.name()) //
              .setBody(KeypleJsonParser.getParser().toJson(new BodyError(e)));
    }
    getNode().sendMessage(response);
  }
}
