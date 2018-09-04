package org.eclise.keyple.example.remote.server;

import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.UnexpectedReaderException;
import org.eclise.keyple.example.remote.server.transport.Transport;
import org.eclise.keyple.example.remote.server.transport.TransportListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class RemoteSePlugin implements ReaderPlugin, TransportListener {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSePlugin.class);

    SortedSet<ProxyReader> remoteReaders = new TreeSet<ProxyReader>();

    public RemoteSePlugin(Transport transport){
        logger.info("RemoteSePlugin {}", transport);
        transport.start(this);
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

    @Override
    public void onReaderConnected(ProxyReader remoteReader) {
        //remote physical reader connected
        logger.info("onReaderConnected {}", remoteReader);

        //create a virtual reader with a Listener to duplex physical reader
        remoteReaders.add(new RemoteSeReader(remoteReader));
    }


}
