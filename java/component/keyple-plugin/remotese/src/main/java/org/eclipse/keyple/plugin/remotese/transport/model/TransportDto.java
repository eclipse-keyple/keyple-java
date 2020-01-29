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
 * Container message with an embedded Keyple Dto.
 * <p>
 * The implementation can be enriched with network layer information. For instance, it can embed web
 * socket connection information
 * <p>
 * This message transits from the DtoNode to the Keyple layers, back to the DtoNode. Use the method
 * #nextTransportDTO() to build a new container message from a keypleDto.
 *
 * <p>
 * If you don't need to wrap a KeypleDto with additional info, you can use the
 * {@link DefaultTransportDto}
 */
public interface TransportDto {


    /**
     * Retrieve the embedded Keyple DTO
     * 
     * @return embedded Keyple DTO
     */
    KeypleDto getKeypleDTO();

    /**
     * Embed a Keyple DTO into a new TransportDto with additional transport information if needed
     * 
     * @param keypleDto : keyple DTO to be embedded
     * @return Transport DTO with embedded keyple DTO
     */
    TransportDto nextTransportDTO(KeypleDto keypleDto);

}
