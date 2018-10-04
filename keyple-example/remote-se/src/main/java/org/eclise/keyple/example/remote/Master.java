package org.eclise.keyple.example.remote;

import org.eclipse.keyple.plugin.remote_se.rse.RsePlugin;
import org.eclipse.keyple.plugin.remote_se.rse.RseReader;
import org.eclipse.keyple.plugin.remote_se.rse.VirtualSeRemoteService;
import org.eclipse.keyple.plugin.remote_se.transport.ServerNode;
import org.eclipse.keyple.plugin.remote_se.transport.TransportNode;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclise.keyple.example.remote.common.TransportFactory;
import org.eclise.keyple.example.remote.common.CommandSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Master implements org.eclipse.keyple.util.Observable.Observer {

    private static final Logger logger = LoggerFactory.getLogger(Master.class);

    private TransportFactory transportFactory;
    private Boolean isServer;
    private TransportNode node;

    public Master(TransportFactory transportFactory, Boolean isServer) {
        logger.info("*******************");
        logger.info("Create Master    ");
        logger.info("*******************");


        this.transportFactory = transportFactory;
        this.isServer = isServer;
    }

    public void boot() throws IOException {


        if(isServer){
            node = transportFactory.getServer(true);
        }else{
            node = transportFactory.getClient(true);
        }



        logger.info("Create Remote PLugin Interface");


        logger.info("Create SeRemotePLugin and register it to SeProxyService");
        RsePlugin rsePlugin = new RsePlugin();

        logger.info("Observe SeRemotePLugin for Plugin Events and Reader Events");
        VirtualSeRemoteService vse = new VirtualSeRemoteService();
        vse.bindTransportNode(node);
        vse.registerRsePlugin(rsePlugin);

        rsePlugin.addObserver(this);

        if(isServer){
            ((ServerNode)node).start();
            logger.info("Waits for remote connections");
        }

    }




    /*
     * public void status() throws UnexpectedPluginException, IOReaderException { // should show
     * remote readers after a while SeProxyService service = SeProxyService.getInstance();
     * logger.info("Remote readers connected {}",
     * service.getPlugin("RemoteSePlugin").getReaders().size()); }
     */

    /**
     * Receives Event from RSE Plugin
     *
     * @param o : can be a ReaderEvent or PluginEvent
     */
    @Override
    public void update(Object o) {

        logger.debug("UPDATE {}", o);

        // PluginEvent
        if (o instanceof PluginEvent) {
            PluginEvent event = (PluginEvent) o;
            switch (event.getEventType()) {
                case READER_CONNECTED:
                    logger.info("READER_CONNECTED {} {}", event.getPluginName(),
                            event.getReaderName());
                    try {
                        RsePlugin rsePlugin =
                                (RsePlugin) SeProxyService.getInstance().getPlugin("RemoteSePlugin");
                        RseReader rseReader =
                                (RseReader) rsePlugin.getReader(event.getReaderName());

                        logger.info("Add ServerTicketingApp as a Observer of RSE reader");
                        rseReader.addObserver(this);

                    } catch (KeypleReaderNotFoundException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }catch (KeyplePluginNotFoundException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }


                    break;
                case READER_DISCONNECTED:
                    logger.info("READER_DISCONNECTED {} {}", event.getPluginName(),
                            event.getReaderName());
                    break;
            }
        }
        // ReaderEvent
        else if (o instanceof ReaderEvent) {
            ReaderEvent event = (ReaderEvent) o;
            switch (event.getEventType()) {
                case SE_INSERTED:
                    logger.info("SE_INSERTED {} {}", event.getPluginName(), event.getReaderName());
                    CommandSample.transmitASyncCommand(logger, event.getReaderName());
                    //CommandSample.transmitSyncCommand(logger, event.getReaderName());
                    break;
                case SE_REMOVAL:
                    logger.info("SE_REMOVAL {} {}", event.getPluginName(), event.getReaderName());
                    break;
                case IO_ERROR:
                    logger.info("IO_ERROR {} {}", event.getPluginName(), event.getReaderName());
                    break;

            }
        }
    }

}
