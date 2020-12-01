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
package org.eclipse.keyple.plugin.remote.integration.common.endpoint.pool;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.service.exception.KeypleRuntimeException;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.eclipse.keyple.plugin.remote.PoolRemotePluginClient;
import org.eclipse.keyple.plugin.remote.impl.PoolLocalServiceServerUtils;
import org.eclipse.keyple.plugin.remote.integration.common.util.JacksonParser;
import org.eclipse.keyple.plugin.remote.integration.common.util.NamedThreadFactory;
import org.eclipse.keyple.plugin.remote.spi.SyncEndpointClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stub implementation of a {@link SyncEndpointClient} for a {@link PoolRemotePluginClient}. It
 * simulates synchronous invocation to a remote server.
 */
public class StubSyncEndpointClient implements SyncEndpointClient {

  private static final Logger logger = LoggerFactory.getLogger(StubSyncEndpointClient.class);
  private static final ExecutorService taskPool =
      Executors.newCachedThreadPool(new NamedThreadFactory("syncPool"));;

  public StubSyncEndpointClient() {}

  @Override
  public List<MessageDto> sendRequest(MessageDto msg) {
    final String responsesJson;
    // serialize request
    final String request = JacksonParser.toJson(msg);
    logger.trace("Sending request data to server : {}", request);

    try {
      responsesJson = taskPool.submit(sendData(request)).get();

      List<MessageDto> responses = JacksonParser.fromJsonList(responsesJson);

      return responses;
    } catch (InterruptedException e) {
      throw new KeypleRuntimeException("Impossible to process incoming message", e);
    } catch (ExecutionException e) {
      throw new KeypleRuntimeException("Impossible to process incoming message", e);
    }
  }

  /**
   * @param data json serialized (keyple message dto)
   * @return json serialized data (list of keyple dto)
   */
  private Callable<String> sendData(final String data) {
    return new Callable<String>() {
      @Override
      public String call() throws Exception {
        // Send the dto to the sync node
        List<MessageDto> responses =
            PoolLocalServiceServerUtils.getSyncNode().onRequest(JacksonParser.fromJson(data));

        return JacksonParser.toJson(responses);
      }
    };
  }
}
