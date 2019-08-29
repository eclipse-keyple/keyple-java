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
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;

/**
 * RemoteSePlugin is wrapped into MasterAPI.
 */
public interface RemoteSePlugin extends ReaderPlugin, ObservablePlugin {

    /**
     * Retrieve a reader by its native reader name and slave Node Id
     *
     * @param remoteName : name of the reader on its native device
     * @param slaveNodeId : slave node Id of the reader to disconnect
     * @return corresponding Virtual reader if exists
     * @throws KeypleReaderNotFoundException if no virtual reader match the native reader name
     */
    VirtualReader getReaderByRemoteName(String remoteName, String slaveNodeId)
            throws KeypleReaderNotFoundException;
}
