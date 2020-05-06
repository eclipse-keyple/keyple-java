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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.rm.AbstractRemoteMethodTx;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodName;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Handle the Transmit keypleDTO serialization and deserialization
 */
public class RmTransmitSetTx extends AbstractRemoteMethodTx<List<SeResponse>> {

    private static final Logger logger = LoggerFactory.getLogger(RmTransmitSetTx.class);

    private final Set<SeRequest> seRequestSet;
    private final MultiSeRequestProcessing multiSeRequestProcessing;
    private final ChannelControl channelControl;

    @Override
    public RemoteMethodName getMethodName() {
        return RemoteMethodName.READER_TRANSMIT_SET;
    }

    public RmTransmitSetTx(Set<SeRequest> seRequestSet,
            MultiSeRequestProcessing multiSeRequestProcessing, ChannelControl channelControl,
            String sessionId, String nativeReaderName, String virtualReaderName,
            String requesterNodeId, String slaveNodeId) {
        super(sessionId, nativeReaderName, virtualReaderName, slaveNodeId, requesterNodeId);
        this.seRequestSet = seRequestSet;
        this.multiSeRequestProcessing = multiSeRequestProcessing;
        this.channelControl = channelControl;
    }

    @Override
    public KeypleDto dto() {


        JsonObject body = new JsonObject();

        body.addProperty("seRequestSet", JsonParser.getGson().toJson(seRequestSet,
                new TypeToken<LinkedHashSet<SeRequest>>() {}.getType()));

        body.addProperty("multiSeRequestProcessing", multiSeRequestProcessing.name());

        body.addProperty("channelControl", channelControl.name());

        return KeypleDtoHelper.buildRequest(getMethodName().getName(), body.toString(),
                this.sessionId, this.nativeReaderName, this.virtualReaderName, requesterNodeId,
                targetNodeId, id);
    }


    @Override
    public List<SeResponse> parseResponse(KeypleDto keypleDto) throws KeypleRemoteException {

        // logger.trace("KeypleDto : {}", keypleDto);

        if (KeypleDtoHelper.containsException(keypleDto)) {
            logger.trace("KeypleDto contains an exception: {}", keypleDto);
            KeypleReaderException ex =
                    JsonParser.getGson().fromJson(keypleDto.getError(), KeypleReaderException.class);
            throw new KeypleRemoteException(
                    "An exception occurs while calling the remote method transmitSet", ex);
        } else {
            logger.trace("KeypleDto contains a response: {}", keypleDto);
            return JsonParser.getGson().fromJson(keypleDto.getBody(),
                    new TypeToken<ArrayList<SeResponse>>() {}.getType());
        }
    }


}
