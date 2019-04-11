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
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.SeRequestSet;
import org.eclipse.keyple.seproxy.message.SeResponseSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmTransmitTx extends RemoteMethodTx<SeResponseSet> {

    private static final Logger logger = LoggerFactory.getLogger(RmTransmitTx.class);

    private final SeRequestSet seRequestSet;


    public RmTransmitTx(SeRequestSet seRequestSet, String sessionId, String nativeReaderName,
            String virtualReaderName, String requesterNodeId, String slaveNodeId) {
        super(sessionId, nativeReaderName, virtualReaderName, slaveNodeId, requesterNodeId);
        this.seRequestSet = seRequestSet;
    }

    @Override
    public KeypleDto dto() {
        return new KeypleDto(RemoteMethod.READER_TRANSMIT.getName(),
                JsonParser.getGson().toJson(seRequestSet, SeRequestSet.class), true, this.sessionId,
                this.nativeReaderName, this.virtualReaderName, requesterNodeId, targetNodeId);
    }


    @Override
    public SeResponseSet parseResponse(KeypleDto keypleDto) throws KeypleRemoteException {

        logger.trace("KeypleDto : {}", keypleDto);
        if (KeypleDtoHelper.containsException(keypleDto)) {
            logger.trace("KeypleDto contains an exception: {}", keypleDto);
            KeypleReaderException ex =
                    JsonParser.getGson().fromJson(keypleDto.getBody(), KeypleReaderException.class);
            throw new KeypleRemoteException(
                    "An exception occurs while calling the remote method transmitSet", ex);
        } else {
            logger.trace("KeypleDto contains a response: {}", keypleDto);
            return JsonParser.getGson().fromJson(keypleDto.getBody(), SeResponseSet.class);
        }
    }


}
