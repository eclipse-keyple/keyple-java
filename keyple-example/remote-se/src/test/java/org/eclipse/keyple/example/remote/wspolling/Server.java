package org.eclipse.keyple.example.remote.wspolling;

import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static Integer PORT = 8004;
    public static String END_POINT = "/keypleDTO";
    public static String POLLING_END_POINT = "/polling";
    public static String URL = "0.0.0.0";




    void boot() throws IOException {


        logger.info("*************************************");
        logger.info("Start Polling Webservice server      ");
        logger.info("*************************************");

        WsPServer server = new WsPServer(URL, PORT, END_POINT,POLLING_END_POINT);
        server.start();


        for(int i=0 ; i<10 ; i++ ){

            try {
                Thread.sleep(17000);

                //send message to client by polling (suppose that a client is polling)
                server.update(KeypleDTOHelper.ACK());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    void demo(){

    }

    public static void main(String[] args) throws Exception {

        Server server = new Server();
        server.boot();
    }
}
