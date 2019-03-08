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
package org.eclipse.keyple.plugin.remotese.nativese.method;

import org.eclipse.keyple.plugin.remotese.rm.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodInvoker;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.seproxy.message.ProxyReader;

@Deprecated
public class RmConnectReaderInvoker implements RemoteMethodInvoker {

    private final ProxyReader localReader;
    private final String clientNodeId;

    public RmConnectReaderInvoker(ProxyReader localReader, String clientNodeId) {
        this.localReader = localReader;
        this.clientNodeId = clientNodeId;
    }

    @Override
    public KeypleDto dto() {
        return new KeypleDto(RemoteMethod.READER_CONNECT.getName(), "{}", true, null,
                localReader.getName(), null, clientNodeId);
    }
}
