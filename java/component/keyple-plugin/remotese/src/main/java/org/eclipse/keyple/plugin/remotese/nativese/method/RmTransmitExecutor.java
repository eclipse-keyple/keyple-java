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

import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execute the Transmit on Native Reader
 */
public class RmTransmitExecutor implements RemoteMethodExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RmTransmitExecutor.class);

    private final SlaveAPI slaveAPI;

    public RemoteMethod getMethodName() {
        return RemoteMethod.READER_TRANSMIT;
    }


    public RmTransmitExecutor(SlaveAPI slaveAPI) {
        this.slaveAPI = slaveAPI;
    }

    @Override
    public TransportDto execute(TransportDto transportDto) {

        KeypleDto keypleDto = transportDto.getKeypleDTO();
        TransportDto out = null;
        SeResponse seResponse = null;

        // Extract info from keypleDto
        SeRequest seRequest = JsonParser.getGson().fromJson(keypleDto.getBody(), SeRequest.class);
        String nativeReaderName = keypleDto.getNativeReaderName();
        logger.trace("Execute locally seRequest : {}", seRequest);

        try {
            // find native reader by name
            ProxyReader reader = slaveAPI.findLocalReader(nativeReaderName);

            // execute transmitSet
            seResponse = reader.transmit(seRequest);

            // prepare response
            String parseBody = JsonParser.getGson().toJson(seResponse, SeResponse.class);
            out = transportDto.nextTransportDTO(KeypleDtoHelper.buildResponse(
                    getMethodName().getName(), parseBody,
                    keypleDto.getSessionId(), nativeReaderName, keypleDto.getVirtualReaderName(),
                    keypleDto.getTargetNodeId(), keypleDto.getRequesterNodeId(), keypleDto.getId()));

        } catch (KeypleReaderException e) {
            // if an exception occurs, send it into a keypleDto to the Master
            out = transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(
                    getMethodName().getName(), e, keypleDto.getSessionId(),
                    nativeReaderName, keypleDto.getVirtualReaderName(), keypleDto.getTargetNodeId(),
                    keypleDto.getRequesterNodeId(),keypleDto.getId()));
        }

        return out;
    }
}
