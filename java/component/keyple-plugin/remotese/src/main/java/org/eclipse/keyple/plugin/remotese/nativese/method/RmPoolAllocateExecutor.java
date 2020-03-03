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
package org.eclipse.keyple.plugin.remotese.nativese.method;

import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.plugin.remotese.rm.IRemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodName;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;
import com.google.gson.JsonObject;

public class RmPoolAllocateExecutor implements IRemoteMethodExecutor {

    ReaderPoolPlugin poolPlugin;
    String slaveNodeId;

    @Override
    public RemoteMethodName getMethodName() {
        return RemoteMethodName.POOL_ALLOCATE_READER;
    }

    public RmPoolAllocateExecutor(ReaderPoolPlugin poolPlugin, String slaveNodeId) {
        this.poolPlugin = poolPlugin;
        this.slaveNodeId = slaveNodeId;
    }

    @Override
    public TransportDto execute(TransportDto transportDto) {

        KeypleDto keypleDto = transportDto.getKeypleDTO();
        TransportDto out = null;
        SeResponse seResponse = null;

        // Extract info from keypleDto
        JsonObject body = JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);
        String groupReference = body.get("groupReference").getAsString();

        // Execute Remote Method
        SeReader seReader = poolPlugin.allocateReader(groupReference);

        // Build Response
        JsonObject bodyResp = new JsonObject();
        bodyResp.addProperty("nativeReaderName", seReader.getName());
        bodyResp.addProperty("transmissionMode", seReader.getTransmissionMode().name());

        out = transportDto.nextTransportDTO(KeypleDtoHelper.buildResponse(getMethodName().getName(), //
                bodyResp.toString(), //
                null, // no session yet
                seReader.getName(), //
                null, // no virtualreader yet
                keypleDto.getTargetNodeId(), //
                slaveNodeId, // nodeId of the actual slave dtoNode, useful for load
                             // balancing
                keypleDto.getId()));

        return out;
    }
}
