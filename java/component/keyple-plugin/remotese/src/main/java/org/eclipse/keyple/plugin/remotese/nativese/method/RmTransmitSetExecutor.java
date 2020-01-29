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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.plugin.remotese.nativese.SlaveAPI;
import org.eclipse.keyple.plugin.remotese.rm.IRemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodName;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Execute the TransmitSet on Native Reader from KeypleDto
 *
 * <p>
 * See {@link org.eclipse.keyple.plugin.remotese.pluginse.method.RmTransmitTx}
 *
 */
public class RmTransmitSetExecutor implements IRemoteMethodExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RmTransmitSetExecutor.class);

    private final SlaveAPI slaveAPI;

    @Override
    public RemoteMethodName getMethodName() {
        return RemoteMethodName.READER_TRANSMIT_SET;
    }

    public RmTransmitSetExecutor(SlaveAPI slaveAPI) {
        this.slaveAPI = slaveAPI;
    }

    @Override
    public TransportDto execute(TransportDto transportDto) {
        KeypleDto keypleDto = transportDto.getKeypleDTO();
        TransportDto out = null;
        List<SeResponse> seResponseList = null;
        MultiSeRequestProcessing multiSeRequestProcessing;
        ChannelControl channelControl;

        // parse body
        JsonObject bodyJsonO = JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);

        // extract info
        multiSeRequestProcessing = MultiSeRequestProcessing
                .valueOf(bodyJsonO.get("multiSeRequestProcessing").getAsString());

        channelControl = ChannelControl.valueOf(bodyJsonO.get("channelControl").getAsString());

        Set<SeRequest> seRequestSet =
                JsonParser.getGson().fromJson(bodyJsonO.get("seRequestSet").getAsString(),
                        new TypeToken<LinkedHashSet<SeRequest>>() {}.getType());


        // prepare transmitSet on nativeReader
        String nativeReaderName = keypleDto.getNativeReaderName();
        logger.trace("Execute locally seRequestSet : {} with params {} {}", seRequestSet,
                channelControl, multiSeRequestProcessing);

        try {
            // find native reader by name
            ProxyReader reader = (ProxyReader) slaveAPI.findLocalReader(nativeReaderName);

            // execute transmitSet
            seResponseList =
                    reader.transmitSet(seRequestSet, multiSeRequestProcessing, channelControl);

            // prepare response
            String parseBody = JsonParser.getGson().toJson(seResponseList,
                    new TypeToken<ArrayList<SeResponse>>() {}.getType());
            out = transportDto.nextTransportDTO(KeypleDtoHelper.buildResponse(
                    getMethodName().getName(), parseBody, keypleDto.getSessionId(),
                    nativeReaderName, keypleDto.getVirtualReaderName(), keypleDto.getTargetNodeId(),
                    keypleDto.getRequesterNodeId(), keypleDto.getId()));

        } catch (KeypleReaderException e) {
            // if an exception occurs, send it into a keypleDto to the Master
            out = transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(
                    getMethodName().getName(), e, keypleDto.getSessionId(), nativeReaderName,
                    keypleDto.getVirtualReaderName(), keypleDto.getTargetNodeId(),
                    keypleDto.getRequesterNodeId(), keypleDto.getId()));
        }

        return out;
    }
}
