/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.pluginse;

import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;

/**
 * RemoteSePlugin manages Virtual Reader. Use its ObservablePlugin capacities to get notified when a
 * {@link VirtualReader} is connected/disconnected. It is created and registered by the
 * {@link MasterAPI}
 */
public interface RemoteSePlugin extends ReaderPlugin, ObservablePlugin {

    /**
     * Retrieve a {@link VirtualReader} by its native reader name and slave Node Id
     *
     * @param remoteName : name of the reader on its native device
     * @param slaveNodeId : slave node Id of the reader to disconnect
     * @return corresponding Virtual reader if exists
     * @throws KeypleReaderNotFoundException if no virtual reader match the native reader name
     */
    VirtualReader getReaderByRemoteName(String remoteName, String slaveNodeId)
            throws KeypleReaderNotFoundException;


    /**
     * Exceptionnaly disconnect a Virtual Reader. Use it in case of error, Slave node is not
     * notified of the disconnect. A READER_DISCONNECTED event is thrown.
     * 
     * @param remoteName : name of the reader on its native device
     * @param slaveNodeId : slave node Id of the reader to disconnect
     * @throws KeypleReaderNotFoundException if no virtual reader match the native reader name
     */
    void disconnectVirtualReader(String remoteName, String slaveNodeId)
            throws KeypleReaderException;


}
