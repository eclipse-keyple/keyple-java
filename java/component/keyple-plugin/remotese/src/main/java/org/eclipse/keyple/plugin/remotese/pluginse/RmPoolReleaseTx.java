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

import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTx;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;

public class RmPoolReleaseTx extends RemoteMethodTx<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(RmPoolReleaseTx.class);

    RemoteSePoolPluginImpl virtualPoolPlugin;
    DtoSender dtoSender;

    public RmPoolReleaseTx(String nativeReaderName, String virtualReaderName,
            RemoteSePoolPluginImpl virtualPoolPlugin, DtoSender dtoSender, String slaveNodeId,
            String requesterNodeId) {
        super(null, nativeReaderName, virtualReaderName, slaveNodeId, requesterNodeId);
        this.dtoSender = dtoSender;
        this.virtualPoolPlugin = virtualPoolPlugin;
    }

    @Override
    public RemoteMethod getMethodName() {
        return RemoteMethod.POOL_RELEASE_READER;
    }

    @Override
    protected KeypleDto dto() {
        JsonObject body = new JsonObject();
        body.addProperty("nativeReaderName", nativeReaderName);

        return KeypleDtoHelper.buildRequest(getMethodName().getName(), body.toString(), null,
                nativeReaderName, virtualReaderName, requesterNodeId, targetNodeId, id);
    }


    @Override
    protected Boolean parseResponse(KeypleDto keypleDto) throws KeypleRemoteException {
        logger.trace("KeypleDto : {}", keypleDto);
        if (KeypleDtoHelper.containsException(keypleDto)) {
            logger.trace("KeypleDto contains an exception: {}", keypleDto);
            KeypleReaderException ex =
                    JsonParser.getGson().fromJson(keypleDto.getBody(), KeypleReaderException.class);
            throw new KeypleRemoteException(
                    "An exception occurs while calling the remote method transmitSet", ex);
        } else {
            logger.trace("KeypleDto contains a response: {}", keypleDto);

            JsonObject body = JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);
            String nativeReaderName = body.get("nativeReaderName").getAsString();

            // create the Virtual Reader related to the Reader Allocation
            try {
                this.virtualPoolPlugin.removeVirtualReader(nativeReaderName,
                        keypleDto.getRequesterNodeId());
                return true;
            } catch (KeypleReaderException e) {
                throw new KeypleRemoteException(e.getMessage());
            }


        }
    }
}
