/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.webservice;

import java.io.IOException;
import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;

public class WsClient implements TransportNode {

    private static final Logger logger = LoggerFactory.getLogger(WsClient.class);

    private String endoint;
    private DtoReceiver dtoReceiver;

    public WsClient(String url) {
        this.endoint = url;
    }


    /*
     * TransportNode
     */
    @Override
    public void setStubplugin(DtoReceiver receiver) {
        this.dtoReceiver = receiver;
    }



    @Override
    public void sendDTO(TransportDTO tdto) {
        KeypleDTO ktdo = tdto.getKeypleDTO();
        logger.debug("Ws Client send DTO {}", KeypleDTOHelper.toJson(ktdo));
        if (!KeypleDTOHelper.isNoResponse(tdto.getKeypleDTO())) {
            try {
                // send keyple dto
                JsonObject httpResponse = HttpHelper.httpPOSTJson(HttpHelper.getConnection(endoint),
                        KeypleDTOHelper.toJson(ktdo));

                // is response DTO ?
                if (KeypleDTOHelper.isKeypleDTO(httpResponse)) {

                    KeypleDTO responseDTO = KeypleDTOHelper.fromJsonObject(httpResponse);
                    WsTransportDTO transportDTO = new WsTransportDTO(responseDTO, null);
                    TransportDTO sendback = this.dtoReceiver.onDTO(transportDTO);// stateless
                                                                                 // connection

                    // if sendBack is not a not reponse
                    if (!KeypleDTOHelper.isNoResponse(sendback.getKeypleDTO())) {
                        this.sendDTO(sendback);
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
                // todo manage exception or throw it
            }
        }
    }

    @Override
    public void sendDTO(KeypleDTO message) {
        sendDTO(new WsTransportDTO(message, null));
    }

    @Override
    public void update(KeypleDTO event) {
        // not in used in webservice
    }
}
