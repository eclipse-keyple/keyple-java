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

import java.io.IOException;

/**
 * Factory for Clients and Servers sharing a protocol and a configuration to connectAReader each
 * others
 */
public abstract class TransportFactory {

    abstract public ClientNode getClient(String clientNodeId);

    abstract public ServerNode getServer() throws IOException;

    abstract public String getServerNodeId();



}
