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
package org.eclipse.keyple.plugin.remotese.rm;

import java.util.concurrent.ExecutorService;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the transaction (request/response) for remote method invocation It holds the @{@link
 * AbstractRemoteMethodTx} untils the answer is received
 */
public class RemoteMethodTxEngine implements IRemoteMethodTxEngine {

  private static final Logger logger = LoggerFactory.getLogger(RemoteMethodTxEngine.class);

  // waiting transaction, supports only one at the time
  private AbstractRemoteMethodTx remoteMethodTx;

  // Executor to run async task required in RemoteMethodTx
  private final ExecutorService executorService;

  // Dto Sender
  private final DtoSender sender;

  // timeout to wait for the answer, in milliseconds
  private final long timeout;

  /**
   * @param sender : dtosender used to send the keypleDto
   * @param timeout : timeout to wait for the answer, in milliseconds
   * @param executorService : executorService required to execute async task in RemoteMethodTx
   */
  public RemoteMethodTxEngine(DtoSender sender, long timeout, ExecutorService executorService) {
    // this.queue = new Linkers<RemoteMethodTx>();
    this.sender = sender;
    this.timeout = timeout;
    this.executorService = executorService;
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  /**
   * Set Response to a RemoteMethod Invocation
   *
   * @param message to be processed, must be a keyple response or a
   * @return TransportDto : response of the message processing, should be a NoResponse
   */
  @Override
  public TransportDto onResponseDto(TransportDto message) {
    /*
     * Extract KeypleDto
     */
    KeypleDto keypleDto = message.getKeypleDTO();

    /*
     * Check that KeypleDto is a Response
     */
    if (message.getKeypleDTO().isRequest()) {
      throw new IllegalArgumentException(
          "RemoteMethodTxEngine expects a KeypleDto response. " + keypleDto);
    }
    /*
     * Check that a request has been made previously
     */
    if (remoteMethodTx == null) {
      /*
       * Response received does not match a request. Ignore it
       */
      logger.error(
          "RemoteMethodTxEngine receives a KeypleDto response but no remoteMethodTx is defined : "
              + keypleDto);
      throw new IllegalArgumentException(
          "RemoteMethodTxEngine receives a KeypleDto response but no remoteMethodTx is defined : "
              + keypleDto);
    }

    /*
     * Check that ids match
     */
    if (!remoteMethodTx.getId().equals(keypleDto.getId())) {
      logger.error(
          "RemoteMethodTxEngine receives a KeypleDto response but ids don't match : " + keypleDto);
      throw new IllegalArgumentException(
          "RemoteMethodTxEngine receives a KeypleDto response but ids don't match : " + keypleDto);
    }

    /*
     * All checks are successful Set keypleDto as a response to the remote method Tx (request)
     */
    remoteMethodTx.setResponse(keypleDto);

    /*
     * init remote engine to receive a new request
     */
    // re init remoteMethod
    remoteMethodTx = null;

    // no dto should be sent back
    return message.nextTransportDTO(KeypleDtoHelper.NoResponse(keypleDto.getId()));
  }

  /**
   * Add RemoteMethod to executing stack
   *
   * @param rm : RemoteMethodTx to be executed
   */
  @Override
  public void register(final AbstractRemoteMethodTx rm) {
    if (logger.isTraceEnabled()) {
      logger.trace("Register RemoteMethod to engine : {} ", rm.id);
    }
    rm.setExecutorService(executorService);
    rm.setRegistered(true);
    remoteMethodTx = rm;
    rm.setDtoSender(sender);
    rm.setTimeout(timeout);
  }
}
