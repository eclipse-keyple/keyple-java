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

import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.transport.*;
import org.eclipse.keyple.plugin.remotese.transport.factory.ClientNode;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client side of the 1 to 1 local transport for unit testing purposes only one server, only one
 * client initied by the {@link LocalTransportFactory}
 */
public class LocalClient implements ClientNode {

    private static final Logger logger = LoggerFactory.getLogger(LocalClient.class);
    private final LocalServer theServer;
    private DtoHandler dtoHandler;

    public LocalClient(LocalServer server) {
        this.theServer = server;
    }

    public void onLocalMessage(KeypleDto keypleDto) {
        if (dtoHandler != null) {
            TransportDto response = dtoHandler.onDTO(new LocalTransportDto(keypleDto, this));
            // send back response
            this.sendDTO(response);
        } else {
            throw new IllegalStateException("no DtoHandler defined");
        }
    }

    @Override
    public void setDtoHandler(DtoHandler handler) {
        this.dtoHandler = handler;
    }

    @Override
    public void sendDTO(TransportDto transportDto) {
        if (KeypleDtoHelper.isNoResponse(transportDto.getKeypleDTO())) {
            logger.trace("Keyple DTO is empty, do not send it");
        } else {
            // send keypleDto to the server
            theServer.onLocalMessage(transportDto);
        }
    }

    @Override
    public void sendDTO(KeypleDto keypleDto) throws KeypleRemoteException {

        if (theServer == null) {
            throw new KeypleRemoteException("Unable to connect to server");
        }

        if (KeypleDtoHelper.isNoResponse(keypleDto)) {
            logger.trace("Keyple DTO is empty, do not send it");
        } else {
            // send keypleDto to the server
            theServer.onLocalMessage(new LocalTransportDto(keypleDto, this));
        }
    }

    @Override
    public String getNodeId() {
        return "localClient1";
    }

    /*
     * @Override public void update(KeypleDto event) { try { sendDTO(event); } catch
     * (KeypleRemoteException e) { // Error is not propagated
     * logger.error("Exception while sending event throw KeypleRemoteInterface", e); } }
     */


    @Override
    public void connect(ConnectCallback connectCallback) {
        if (connectCallback != null) {
            logger.warn("Connection callback is not implemented for this client");
        }
        logger.info("Connect Local Client");
    }

    @Override
    public void disconnect() {
        // dummy
        logger.info("Disconnect Local Client");
    }

}
