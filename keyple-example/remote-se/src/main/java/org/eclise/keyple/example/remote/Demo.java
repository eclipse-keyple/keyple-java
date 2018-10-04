package org.eclise.keyple.example.remote;


import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclise.keyple.example.remote.common.TransportFactory;
import org.eclise.keyple.example.remote.wspolling.WsPollingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Demo {

    private static final Logger logger = LoggerFactory.getLogger(Demo.class);


    static public void startServer(final Boolean isMaster, final TransportFactory factory){
        Thread server = new Thread(){
            @Override
            public void run() {
                try {

                    logger.info("**** Starting Server Thread ****");

                    if(isMaster){
                        Master master = new Master(factory, true);
                        master.boot();
                    }else{
                        Slave slave = new Slave(factory, true);
                        Thread.sleep(10000);
                        logger.info("Wait for 10 seconds, then connect to master");
                        slave.connect();
                        slave.insertSe();
                    }

                } catch (KeypleReaderNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };

        server.start();
    };

    static public void startClient(final Boolean isMaster, final TransportFactory factory){
        Thread client = new Thread(){
            @Override
            public void run() {
                logger.info("**** Starting Client Thread ****");

                try {
                    if(isMaster){
                        Master master = new Master(factory, false);
                        master.boot();
                    }else{
                        Slave slave = new Slave(factory, false);
                        slave.connect();
                        logger.info("Wait for 10 seconds, then insert SE");
                        Thread.sleep(10000);
                        slave.insertSe();
                    }

                } catch (KeypleReaderNotFoundException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        client.start();
    };

    public static void main(String[] args) throws Exception {

        TransportFactory factory  = new WsPollingFactory();
        //TransportFactory factory = new WskFactory();

        /**
         * Demo Client is Slave
         */

        startServer(true, factory);
        Thread.sleep(1000);
        startClient(false, factory);

        /**
         * Demo Client is Master

        startServer(false, factory);
        Thread.sleep(1000);
        startClient(true, factory);
         */



    }
}
