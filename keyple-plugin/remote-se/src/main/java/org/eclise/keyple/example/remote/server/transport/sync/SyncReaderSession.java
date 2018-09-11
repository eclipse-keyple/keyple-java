package org.eclise.keyple.example.remote.server.transport.sync;

import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclise.keyple.example.remote.server.transport.RSEReaderSession;

public interface SyncReaderSession extends RSEReaderSession {


    Boolean hasSeRequestSet();

    String getName();
    boolean isSePresent();
    void addSeProtocolSetting(SeProtocolSetting seProtocolSetting);

    /*
     Sync Sessions (local, websocket = duplex connection)
     */
    SeResponseSet transmit(SeRequestSet seApplicationRequest);


}
