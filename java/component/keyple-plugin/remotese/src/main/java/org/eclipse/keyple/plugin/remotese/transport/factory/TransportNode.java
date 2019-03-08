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
package org.eclipse.keyple.plugin.remotese.transport.factory;


import org.eclipse.keyple.plugin.remotese.transport.DtoHandler;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;

/**
 * TransportNode is a one-point gateway for incoming and outgoing TransportDto. It extend DtoSender
 * thus sends KeypleDto and contains a DtoHandler for incoming KeypleDto
 */
public interface TransportNode extends DtoSender {

    void setDtoHandler(DtoHandler handler);

}
