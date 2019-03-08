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
package org.eclipse.keyple.plugin.remotese.transport;

import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;

/**
 * Components that sends a DTO over the network to the other end. (slave or master) It can be an
 * observer for KeypleDto to propagate them through the network
 */
public interface DtoSender {

    /**
     * Send DTO with common information
     * 
     * @param message to be sent
     */
    void sendDTO(TransportDto message) throws KeypleRemoteException;

    /**
     * Send DTO with no common information (usually a new message)
     * 
     * @param message to be sent
     */
    void sendDTO(KeypleDto message) throws KeypleRemoteException;

    /**
     * get nodeId of this DtoSender, must identify the terminal. ie : androidDevice2
     * 
     * @return : nodeId
     */
    String getNodeId();

}
