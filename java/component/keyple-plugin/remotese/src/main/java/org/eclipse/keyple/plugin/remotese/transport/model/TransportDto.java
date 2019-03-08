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
package org.eclipse.keyple.plugin.remotese.transport.model;


/**
 * Message DTO with an embedded KeypleDto enriched with common layer information For instance, can
 * embed web socket connection information
 */
public interface TransportDto {


    /**
     * Retrieve the embedded Keyple DTO
     * 
     * @return embedded Keyple DTO
     */
    KeypleDto getKeypleDTO();

    /**
     * Embed a Keyple DTO into a new TransportDto with transport information
     * 
     * @param keypleDto : keyple DTO to be embedded
     * @return Transport DTO with embedded keyple DTO
     */
    TransportDto nextTransportDTO(KeypleDto keypleDto);


    /**
     * Get the sender Object to send back a response if needed
     * 
     * @return DtoSender if any
     */
    // not in used
    // DtoSender getDtoSender();



}
