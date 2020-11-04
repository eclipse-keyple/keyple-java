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
import java.util.*;
import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.exception.KeypleAllocationReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.nativ.NativePoolServerService;

/**
 * Implementation of the {@link NativePoolServerService}. This object is a singleton created by the
 * {@link NativePoolServerServiceFactory}
 */
final class NativePoolServerServiceImpl extends AbstractNativeService
    implements NativePoolServerService {

  private static NativePoolServerServiceImpl uniqueInstance;
  private final String[] poolPluginNames;

  private NativePoolServerServiceImpl(String[] poolPluginNames) {
    this.poolPluginNames = poolPluginNames;
  }

  /**
   * (package-private)<br>
   * Create an instance of this singleton service
   *
   * @param poolPluginNames name(s) of the pool plugin(s) associated with this service
   * @return a not null instance of the singleton
   */
  static NativePoolServerServiceImpl createInstance(String[] poolPluginNames) {
    uniqueInstance = new NativePoolServerServiceImpl(poolPluginNames);
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
    ProxyReader reader;
    ReaderPoolPlugin poolPlugin;
    try {
      switch (KeypleMessageDto.Action.valueOf(msg.getAction())) {
        case ALLOCATE_READER:
          String groupReference =
              KeypleJsonParser.getParser()
                  .fromJson(msg.getBody(), JsonObject.class)
                  .get("groupReference")
                  .getAsString();
          poolPlugin = getAPoolPlugin(groupReference);
          reader = (ProxyReader) poolPlugin.allocateReader(groupReference);
          response = new KeypleMessageDto(msg).setNativeReaderName(reader.getName()).setBody(null);
          break;
        case RELEASE_READER:
          releaseReader(msg.getNativeReaderName());
          response = new KeypleMessageDto(msg).setBody(null);
          break;
        case GET_READER_GROUP_REFERENCES:
          SortedSet<String> groupReferences = getAllGroupReferences();
          JsonObject body = new JsonObject();
          body.add(
              "readerGroupReferences", KeypleJsonParser.getParser().toJsonTree(groupReferences));
          response = new KeypleMessageDto(msg).setBody(body.toString());
          break;
        default:
          reader = findReader(msg.getNativeReaderName());
          response = executeLocally(reader, msg);
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

  /**
   * Retrieve a pool plugin that contains a specific groupReference
   *
   * @param groupReference non nullable instance of a group instance
   * @return non nullable instance of a pool plugin
   * @throws KeypleAllocationReaderException if no pool plugin containing group reference is found
   */
  private ReaderPoolPlugin getAPoolPlugin(String groupReference) {
    for (String poolPluginName : poolPluginNames) {
      ReaderPoolPlugin poolPlugin =
          (ReaderPoolPlugin) SeProxyService.getInstance().getPlugin(poolPluginName);
      if (poolPlugin.getReaderGroupReferences().contains(groupReference)) {
        return poolPlugin;
      }
    }
    throw new KeypleAllocationReaderException(
        "No reader pool plugin containing group reference '"
            + groupReference
            + "' is registered in this service");
  }

  /**
   * Concatenate group references of all registered local pool plugins.
   *
   * @return non nullable instance of a group references, can be empty
   */
  private SortedSet<String> getAllGroupReferences() {
    SortedSet<String> allGroupReferences = new TreeSet<String>();
    for (String poolPluginName : poolPluginNames) {
      ReaderPoolPlugin poolPlugin =
          (ReaderPoolPlugin) SeProxyService.getInstance().getPlugin(poolPluginName);
      allGroupReferences.addAll(poolPlugin.getReaderGroupReferences());
    }
    return allGroupReferences;
  }

  /**
   * Release reader with given reader name
   *
   * @param readerName non nullable value of a reader name
   * @throws KeypleReaderNotFoundException if no reader is found with given reader name
   */
  private void releaseReader(String readerName) {
    for (String poolPluginName : poolPluginNames) {
      ReaderPoolPlugin poolPlugin =
          (ReaderPoolPlugin) SeProxyService.getInstance().getPlugin(poolPluginName);
      if (poolPlugin.getReaderNames().contains(readerName)) {
        poolPlugin.releaseReader(poolPlugin.getReader(readerName));
        return;
      }
    }
    throw new KeypleReaderNotFoundException(readerName);
  }

  /**
   * Find a local reader among all pool plugins associated to this service
   *
   * @param nativeReaderName name of the reader to be found
   * @return a not null instance of a reader
   * @throws KeypleReaderNotFoundException if no reader is found with this name
   * @since 1.0
   */
  private ProxyReader findReader(String nativeReaderName) {
    for (String poolPluginName : poolPluginNames) {
      ReaderPoolPlugin plugin =
          (ReaderPoolPlugin) SeProxyService.getInstance().getPlugin(poolPluginName);
      try {
        return (ProxyReader) plugin.getReader(nativeReaderName);
      } catch (KeypleReaderNotFoundException e) {
        // reader has not been found in this plugin, continue
      }
    }
    throw new KeypleReaderNotFoundException(nativeReaderName);
  }
}
