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

/**
 * Component that process locally DTO
 */
public interface DtoHandler {

    /**
     * Process synchronously a message and returns a response message
     * 
     * @param message to be processed
     * @return response can be a NO_RESPONSE DTO, can not be null
     */
    TransportDto onDTO(TransportDto message);

}
