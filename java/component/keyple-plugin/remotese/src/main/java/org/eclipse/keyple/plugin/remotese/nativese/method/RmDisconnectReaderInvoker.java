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
import com.google.gson.JsonObject;

@Deprecated
public class RmDisconnectReaderInvoker implements RemoteMethodInvoker {

    private final String sessionId;
    private final String nativeReaderName;
    private final String slaveNodeId;

    public RmDisconnectReaderInvoker(String sessionId, String nativeReaderName,
            String slaveNodeId) {
        this.sessionId = sessionId;
        this.nativeReaderName = nativeReaderName;
        this.slaveNodeId = slaveNodeId;
    }

    @Override
    public KeypleDto dto() {

        JsonObject body = new JsonObject();
        body.addProperty("sessionId", sessionId);

        return new KeypleDto(RemoteMethod.READER_DISCONNECT.getName(), body.getAsString(), true,
                null, nativeReaderName, null, slaveNodeId);
    }
}
