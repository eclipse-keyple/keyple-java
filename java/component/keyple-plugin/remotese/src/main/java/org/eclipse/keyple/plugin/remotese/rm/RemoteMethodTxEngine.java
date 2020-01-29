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

import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages the transaction (request/response) for remote method invocation It holds
 * the @{@link AbstractRemoteMethodTx} untils the answer is received
 */
public class RemoteMethodTxEngine implements DtoHandler, IRemoteMethodTxEngine {

    private static final Logger logger = LoggerFactory.getLogger(RemoteMethodTxEngine.class);


    // waiting transaction, supports only one at the time
    private AbstractRemoteMethodTx remoteMethodTx;

    // Dto Sender
    private final DtoSender sender;

    // timeout to wait for the answer, in milliseconds
    private final long timeout;

    /**
     *
     * @param sender : dtosender used to send the keypleDto
     * @param timeout : timeout to wait for the answer, in milliseconds
     */
    public RemoteMethodTxEngine(DtoSender sender, long timeout) {
        // this.queue = new LinkedList<RemoteMethodTx>();
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
             * Should not happen, response received does not match a request. Ignore it
             */
            logger.error(
                    "RemoteMethodTxEngine receives a KeypleDto response but no remoteMethodTx are defined : "
                            + keypleDto);
        }

        // only one operation is allowed at the time
        remoteMethodTx.setResponse(keypleDto);

        // re init remoteMethod
        remoteMethodTx = null;

        return message.nextTransportDTO(KeypleDtoHelper.NoResponse(keypleDto.getId()));
    }

    /**
     * Add RemoteMethod to executing stack
     * 
     * @param rm : RemoteMethodTx to be executed
     */
    @Override
    public void register(final AbstractRemoteMethodTx rm) {
        logger.debug("Register rm to engine : {} {}", rm.getMethodName(), rm.id);
        rm.setRegistered(true);
        remoteMethodTx = rm;
        rm.setDtoSender(sender);
        rm.setTimeout(timeout);
    }
}
