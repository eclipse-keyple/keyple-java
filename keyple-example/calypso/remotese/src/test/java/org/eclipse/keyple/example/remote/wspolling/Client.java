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
package org.eclipse.keyple.example.remote.wspolling;

import org.eclipse.keyple.example.remote.wspolling.client.WsPClient;
import org.eclipse.keyple.plugin.remotese.transport.DtoHandler;
import org.eclipse.keyple.plugin.remotese.transport.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.TransportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final String BASE_URL = "http://localhost:8004";
    private final String ENDPOINT_URL = "/keypleDTO";
    private final String POLLING_URL = "/polling";



    private void boot() {


        WsPClient client = new WsPClient(BASE_URL, ENDPOINT_URL, POLLING_URL, "test1");
        client.startPollingWorker("node1");
        client.setDtoHandler(new DtoHandler() {
            @Override
            public TransportDto onDTO(TransportDto message) {
                return new WsPTransportDTO(KeypleDtoHelper.NoResponse(), null);
            }
        });

    }


    public static void main(String[] args) {
        Client client = new Client();
        client.boot();
    }


}
