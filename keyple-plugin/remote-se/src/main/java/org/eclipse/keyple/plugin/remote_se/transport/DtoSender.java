/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.transport;

import org.eclipse.keyple.util.Observable;

/**
 * Components that sends a DTO over the network It can be an observer for KeypleDTOs
 */
public interface DtoSender extends Observable.Observer<KeypleDto> {

    /**
     * Send DTO with transport information
     * 
     * @param message to be sent
     */
    void sendDTO(TransportDto message);

    /**
     * Send DTO with no transport information (usually a new message)
     * 
     * @param message to be sent
     */
    void sendDTO(KeypleDto message);

}
