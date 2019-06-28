/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.rm;

import org.eclipse.keyple.plugin.remotese.transport.DtoHandler;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Manages the transaction (request/response) for remote method invocation It holds
 * the @{@link RemoteMethodTx} untils the answer is received
 */
public class RemoteMethodTxPoolEngine implements DtoHandler {

    private static final Logger logger = LoggerFactory.getLogger(RemoteMethodTxPoolEngine.class);


    // rm id, rm
    private Map<String,RemoteMethodTx> queue;

    // Dto Sender
    private final DtoSender sender;

    // timeout to wait for the answer, in milliseconds
    private final long timeout;

    /**
     *
     * @param sender : dtosender used to send the keypleDto
     * @param timeout : timeout to wait for the answer, in milliseconds
     */
    public RemoteMethodTxPoolEngine(DtoSender sender, long timeout) {
        this.queue = new HashMap<String, RemoteMethodTx>();
        this.sender = sender;
        this.timeout = timeout;
    }


    /**
     * Set Response to a RemoteMethod Invocation
     * 
     * @param message to be processed
     * @return TransportDto : response of the processing of the transportDto, can be an empty
     *         TransportDto
     */
    @Override
    public TransportDto onDTO(TransportDto message) {
        KeypleDto keypleDto = message.getKeypleDTO();

        if (keypleDto.isRequest()) {
            throw new IllegalArgumentException(
                    "RemoteMethodTxEngine expects a KeypleDto response. " + keypleDto);
        }
        if (!queue.containsKey(keypleDto.getId())) {
            throw new IllegalStateException(
                    "RemoteMethodTxEngine receives a KeypleDto response but no remoteMethodTx are defined : "
                            + keypleDto);
        }

        //set response in rm request
        queue.get(keypleDto.getId()).setResponse(keypleDto);

        // remove rm
        queue.remove(keypleDto.getId());

        return message.nextTransportDTO(KeypleDtoHelper.NoResponse(keypleDto.getId()));
    }

    /**
     * Add RemoteMethod to executing stack
     * 
     * @param rm : RemoteMethodTx to be executed
     */
    public void add(final RemoteMethodTx rm) {
        logger.debug("Register rm to engine : {}", rm);
        rm.setRegistered(true);
        queue.put(rm.id, rm);
        rm.setDtoSender(sender);
        rm.setTimeout(timeout);
    }
}
