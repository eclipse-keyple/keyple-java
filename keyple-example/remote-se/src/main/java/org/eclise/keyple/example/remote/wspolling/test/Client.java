package org.eclise.keyple.example.remote.wspolling.test;

import org.eclise.keyple.example.remote.wspolling.WsPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static String ENDPOINT_URL = "http://localhost:8004/keypleDTO";
    private static String POLLING_URL = "http://localhost:8004/polling";



    void boot(){


        WsPClient client = new WsPClient(ENDPOINT_URL, POLLING_URL);
        client.startPollingWorker("node1");

    }

    void demo(){

    }

    public static void main(String[] args) throws Exception {


        Client client = new Client();
        client.boot();
        client.demo();


    }


}
