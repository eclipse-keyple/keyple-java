package org.eclise.keyple.example.remote.server.transport;

import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;

public interface ServerListener {

    void onReaderConnected(ProxyReader reader);

    void onRemoteReaderEvent(ReaderEvent event);

}
