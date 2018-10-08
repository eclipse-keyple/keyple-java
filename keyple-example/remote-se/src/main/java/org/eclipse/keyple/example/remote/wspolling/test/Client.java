package org.eclipse.keyple.example.remote.wspolling.test;

import org.eclipse.keyple.example.remote.wspolling.WsPClient;
import org.eclipse.keyple.plugin.remote_se.transport.DtoDispatcher;
import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTOHelper;
import org.eclipse.keyple.plugin.remote_se.transport.TransportDTO;
import org.eclipse.keyple.example.remote.wspolling.WsPTransportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static String ENDPOINT_URL = "http://localhost:8004/keypleDTO";
    private static String POLLING_URL = "http://localhost:8004/polling";



    void boot(){


        WsPClient client = new WsPClient(ENDPOINT_URL, POLLING_URL,"test1");
        client.startPollingWorker("node1");
        client.setDtoDispatcher(new DtoDispatcher() {
            @Override
            public TransportDTO onDTO(TransportDTO message) {
                return new WsPTransportDTO(KeypleDTOHelper.NoResponse(), null);
            }
        });

    }

    void demo(){

    }

    public static void main(String[] args) throws Exception {


        Client client = new Client();
        client.boot();
        client.demo();


    }


}
