/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.transport;


/**
 * Message with layer transport information
 */
public interface TransportDTO {


    /**
     * Retrieve the embedded Keyple DTO
     * 
     * @return embedded Keyple DTO
     */
    KeypleDTO getKeypleDTO();

    /**
     * Embed a Keyple DTO into a new TransportDTO with transport information
     * 
     * @param kdto : keyple DTO to be embedded
     * @return Transport DTO with embedded keyple DTO
     */
    TransportDTO nextTransportDTO(KeypleDTO kdto);


    // @Deprecated
    // DtoSender getDtoSender();



}
