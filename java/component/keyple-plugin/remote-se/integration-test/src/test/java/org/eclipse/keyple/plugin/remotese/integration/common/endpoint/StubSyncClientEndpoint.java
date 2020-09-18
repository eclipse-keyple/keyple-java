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
package org.eclipse.keyple.plugin.remotese.integration.common.endpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.seproxy.exception.KeypleRuntimeException;
import org.eclipse.keyple.plugin.remotese.core.KeypleClientSync;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.integration.common.util.JacksonParser;
import org.eclipse.keyple.plugin.remotese.virtualse.impl.RemoteSeServerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stub implementation of a {@link KeypleClientSync}.
 */
public class StubSyncClientEndpoint implements KeypleClientSync {

  private static final Logger logger = LoggerFactory.getLogger(StubSyncClientEndpoint.class);
  static ExecutorService taskPool = Executors.newCachedThreadPool();
  private final String clientId;

  public StubSyncClientEndpoint() {
    clientId = UUID.randomUUID().toString();
  }

  @Override
  public List<KeypleMessageDto> sendRequest(KeypleMessageDto msg) {
    final List<String> responsesJson;
    msg.setClientNodeId(clientId);
    // serialize request
    final String request = JacksonParser.toJson(msg);

    logger.trace("Sending request data to server : {}", request);

    try {
      responsesJson =
          taskPool
              .submit(
                  new Callable<List>() {
                    @Override
                    public List call() throws Exception {
                      // Send the dto to the sync node
                      List<KeypleMessageDto> responses =
                          RemoteSeServerUtils.getSyncNode()
                              .onRequest(JacksonParser.fromJson(request));
                      List<String> responsesJson = new ArrayList<String>();
                      for (KeypleMessageDto dto : responses) {
                        dto.setClientNodeId(clientId);
                        responsesJson.add(JacksonParser.toJson(dto));
                      }
                      return responsesJson;
                    }
                  })
              .get();
    } catch (InterruptedException e) {
      throw new KeypleRuntimeException("Impossible to process incoming message", e);
    } catch (ExecutionException e) {
      throw new KeypleRuntimeException("Impossible to process incoming message", e);
    }

    // deserialize result
    final List<KeypleMessageDto> out = new ArrayList<KeypleMessageDto>();
    for (String responseJson : responsesJson) {
      out.add(JacksonParser.fromJson(responseJson));
      logger.trace("Received response data from server : {}", responseJson);
    }

    return out;
  }
}
