package org.eclise.keyple.example.remote.server;

import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.exception.UnexpectedReaderException;
import org.eclise.keyple.example.remote.server.transport.ServerTransport;
import org.eclise.keyple.example.remote.server.transport.ServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class RemoteSePlugin implements ReaderPlugin {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSePlugin.class);

    SortedSet<ProxyReader> remoteReaders = new TreeSet<ProxyReader>();

    public RemoteSePlugin(){
        logger.info("RemoteSePlugin");
    }

    @Override
    public String getName() {
        return "RemoteSePlugin";
    }

    @Override
    public SortedSet<? extends ProxyReader> getReaders() {
        return remoteReaders;
    }

    @Override
    public ProxyReader getReader(String name) throws UnexpectedReaderException {
        return null;
    }


    public void connectRemoteReader(ServerTransport transport){

        if(remoteReaders.contains(transport.hashCode())){
            logger.warn("Remote Reader is already connected {}", transport.hashCode());
        }else{
            remoteReaders.add(new RemoteSeReader(transport));
        }
    }



    @Override
    public int compareTo(ReaderPlugin o) {
        return 0;
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IOException {

    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IOException {

    }



}
