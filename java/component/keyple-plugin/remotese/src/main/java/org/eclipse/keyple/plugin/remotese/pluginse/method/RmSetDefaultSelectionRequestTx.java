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
package org.eclipse.keyple.plugin.remotese.pluginse.method;

import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTx;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.seproxy.event.DefaultSelectionRequest;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import com.google.gson.JsonObject;

public class RmSetDefaultSelectionRequestTx extends RemoteMethodTx {

    private DefaultSelectionRequest defaultSelectionRequest;
    private ObservableReader.NotificationMode notificationMode;


    public RmSetDefaultSelectionRequestTx(DefaultSelectionRequest defaultSelectionRequest,
            ObservableReader.NotificationMode notificationMode, String nativeReaderName,
            String virtualReaderName, String sessionId, String clientNodeId) {
        super(sessionId, nativeReaderName, virtualReaderName, clientNodeId);
        this.defaultSelectionRequest = defaultSelectionRequest;
        this.notificationMode = notificationMode;

    }


    @Override
    public Object parseResponse(KeypleDto keypleDto) throws KeypleRemoteException {
        return new Object();

    }

    @Override
    public KeypleDto dto() {
        JsonObject body = new JsonObject();
        body.addProperty("defaultSelectionRequest",
                JsonParser.getGson().toJson(defaultSelectionRequest));
        body.addProperty("notificationMode", notificationMode.getName());

        return new KeypleDto(RemoteMethod.DEFAULT_SELECTION_REQUEST.getName(),
                JsonParser.getGson().toJson(body, JsonObject.class), true, sessionId,
                nativeReaderName, virtualReaderName, clientNodeId);

    }
}
