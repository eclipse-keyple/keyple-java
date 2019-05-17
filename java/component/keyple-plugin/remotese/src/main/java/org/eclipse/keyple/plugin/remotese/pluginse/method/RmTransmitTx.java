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

import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTx;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle the Transmit keypleDTO serialization and deserialization
 */
public class RmTransmitTx extends RemoteMethodTx<SeResponse> {

    private static final Logger logger = LoggerFactory.getLogger(RmTransmitTx.class);

    private final SeRequest seRequest;

    @Override
    public RemoteMethod getMethodName() {
        return RemoteMethod.READER_TRANSMIT;
    }

    public RmTransmitTx(SeRequest seRequest, String sessionId, String nativeReaderName,
            String virtualReaderName, String requesterNodeId, String slaveNodeId) {
        super(sessionId, nativeReaderName, virtualReaderName, slaveNodeId, requesterNodeId);
        this.seRequest = seRequest;
    }

    @Override
    public KeypleDto dto() {
        return new KeypleDto(RemoteMethod.READER_TRANSMIT.getName(),
                JsonParser.getGson().toJson(seRequest, SeRequest.class), true, this.sessionId,
                this.nativeReaderName, this.virtualReaderName, requesterNodeId, targetNodeId);
    }


    @Override
    public SeResponse parseResponse(KeypleDto keypleDto) throws KeypleRemoteException {

        logger.trace("KeypleDto : {}", keypleDto);
        if (KeypleDtoHelper.containsException(keypleDto)) {
            logger.trace("KeypleDto contains an exception: {}", keypleDto);
            KeypleReaderException ex =
                    JsonParser.getGson().fromJson(keypleDto.getBody(), KeypleReaderException.class);
            throw new KeypleRemoteException(
                    "An exception occurs while calling the remote method transmitSet", ex);
        } else {
            logger.trace("KeypleDto contains a response: {}", keypleDto);
            return JsonParser.getGson().fromJson(keypleDto.getBody(), SeResponse.class);
        }
    }


}
