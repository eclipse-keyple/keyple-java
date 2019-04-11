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
 * DtoNode is a one-point gateway for incoming and outgoing TransportDto. It extend DtoSender thus
 * sends KeypleDto and contains a DtoHandler for incoming KeypleDto
 */
public interface DtoNode extends DtoSender {

    /**
     * Binds a {@link DtoHandler} that will process incoming KeypleDto (usually
     * {@link org.eclipse.keyple.plugin.remotese.pluginse.MasterAPI} or
     * {@link org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI})
     * 
     * @param handler : process incoming
     *        {@link org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto}, usually
     *        {@link org.eclipse.keyple.plugin.remotese.pluginse.MasterAPI} or
     *        {@link org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI}
     */
    void setDtoHandler(DtoHandler handler);

}
