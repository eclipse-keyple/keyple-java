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
package org.eclipse.keyple.plugin.remotese.transport.impl.java;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.factory.ServerNode;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.eclipse.keyple.seproxy.exception.KeypleRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server side of the 1 to many local transport for unit testing purposes only one server
 */
public class LocalServer implements ServerNode {

    private static final Logger logger = LoggerFactory.getLogger(LocalServer.class);

    private DtoHandler dtoHandler;
    private final String serverNodeId;
    private final Map<String, LocalClient> client_ids;

    public LocalServer(String serverNodeId) {
        this.client_ids = new HashMap<String, LocalClient>();
        this.serverNodeId = serverNodeId;
    }

    public void onLocalMessage(TransportDto transportDto) {
        LocalClient theClient = ((LocalTransportDto) transportDto).getTheClient();
        client_ids.put(transportDto.getKeypleDTO().getRequesterNodeId(), theClient);

        if (dtoHandler != null) {
            TransportDto response =
                    dtoHandler.onDTO(new LocalTransportDto(transportDto.getKeypleDTO(), theClient));
            // send back response
            this.sendDTO(response);
        } else {
            throw new IllegalStateException("no DtoHanlder defined");
        }
    }

    @Override
    public void start() {
        logger.info("Local server start");
    }

    @Override
    public void setDtoHandler(DtoHandler handler) {
        this.dtoHandler = handler;
    }

    @Override
    public void sendDTO(TransportDto transportDto) {
        LocalClient theClient = ((LocalTransportDto) transportDto).getTheClient();

        if (KeypleDtoHelper.isNoResponse(transportDto.getKeypleDTO())) {
            logger.trace("Keyple DTO is empty, do not send it");
        } else {
            // send keypleDto to the unique client
            theClient.onLocalMessage(transportDto.getKeypleDTO());
        }
    }

    @Override
    public void sendDTO(KeypleDto keypleDto) {
        LocalClient theClient = client_ids.get(keypleDto.getTargetNodeId());
        if (theClient != null) {
            if (KeypleDtoHelper.isNoResponse(keypleDto)) {
                logger.trace("Keyple DTO is empty, do not send it");
            } else {
                logger.trace("LocalClient was found for {}", keypleDto.getTargetNodeId());
                // send keypleDto to the unique client
                theClient.onLocalMessage(keypleDto);
            }
        } else {
            throw new KeypleRuntimeException(
                    "LocalServer#sendDTO could be invoked, localClient was not found by "
                            + keypleDto.getTargetNodeId() + " - " + keypleDto.getRequesterNodeId());
        }
    }

    @Override
    public String getNodeId() {
        return serverNodeId;
    }

}
