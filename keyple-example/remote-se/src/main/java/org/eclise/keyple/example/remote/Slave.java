package org.eclise.keyple.example.remote;

import org.eclipse.keyple.example.pc.calypso.stub.se.StubHoplink;
import org.eclipse.keyple.plugin.remote_se.nse.NativeSeRemoteService;
import org.eclipse.keyple.plugin.remote_se.transport.ClientNode;
import org.eclipse.keyple.plugin.remote_se.transport.ServerNode;
import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;
import org.eclipse.keyple.plugin.stub.StubPlugin;
import org.eclipse.keyple.plugin.stub.StubReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclise.keyple.example.remote.common.TransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

public class Slave {

    private static final Logger logger = LoggerFactory.getLogger(Slave.class);

    // physical reader
    StubReader localReader;
    private TransportNode node;

    public Slave(TransportFactory transportFactory, Boolean isServer) {
        logger.info("*******************");
        logger.info("Create Slave    ");
        logger.info("*******************");

        if(isServer){
            try {
                node = transportFactory.getServer(false);
                //start server in a new thread
                new Thread(){
                    @Override
                    public void run() {
                        ((ServerNode)node).start();
                        logger.info("Waits for remote connections");
                    }
                }.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            node = transportFactory.getClient(false);
            ((ClientNode)node).connect();
        }
    }

    public void connect() throws KeypleReaderNotFoundException, InterruptedException, IOException {


        logger.info("Boot Slave LocalReader ");

        // get seProxyService
        SeProxyService seProxyService = SeProxyService.getInstance();

        logger.info("Create Local StubPlugin");
        StubPlugin stubPlugin = StubPlugin.getInstance();
        SortedSet<ReaderPlugin> plugins = SeProxyService.getInstance().getPlugins();
        plugins.add(stubPlugin);
        seProxyService.setPlugins(plugins);
        stubPlugin.plugStubReader("stubClientSlave");

        Thread.sleep(1000);

        // get the created proxy reader
        localReader = (StubReader) stubPlugin.getReader("stubClientSlave");

        NativeSeRemoteService seRemoteService = new NativeSeRemoteService();
        seRemoteService.bind(node);

        Map<String, Object> options = new HashMap<String, Object>();
        options.put("isAsync", true);
        seRemoteService.connectReader(localReader, options);

        logger.info("Connect remotely the StubPlugin ");
    }

    public void insertSe() {
        logger.info("************************");
        logger.info("Start DEMO - insert SE  ");
        logger.info("************************");

        logger.info("Insert HoplinkStubSE into Local StubReader");
        // insert SE
        localReader.insertSe(new StubHoplink());

        // todo Remove SE
        // logger.info("************************");
        // logger.info(" remove SE ");
        // logger.info("************************");
        //
        // localReader.removeSe();

    }




}
