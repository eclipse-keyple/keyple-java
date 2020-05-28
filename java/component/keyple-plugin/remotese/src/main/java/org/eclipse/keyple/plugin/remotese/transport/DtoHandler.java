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

import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;

/**
 * Internal Remote SE Plugin components that processes incoming messages
 */
public interface DtoHandler {

    /**
     * Process synchronously a message and returns a response message. If an exception is thrown
     * during the onDto processing, a exception dto should be returned.
     * 
     * @param message to be processed
     * @return response message, can be any of keyple dto types : request, response, notification,
     *         exception, no response.
     */
    TransportDto onDTO(TransportDto message);

}
