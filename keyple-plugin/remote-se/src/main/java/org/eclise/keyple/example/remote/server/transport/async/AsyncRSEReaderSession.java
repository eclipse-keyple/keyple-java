package org.eclise.keyple.example.remote.server.transport.async;

import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclise.keyple.example.remote.server.transport.RSEReaderSession;
import org.eclise.keyple.example.remote.server.transport.async.webservice.server.SeResponseSetCallback;

public interface AsyncRSEReaderSession extends RSEReaderSession {

    /*
   Async Sessions (web services)
    */
    void asyncTransmit(SeRequestSet seApplicationRequest, SeResponseSetCallback seResponseSet);
    void asyncSetSeResponseSet(SeResponseSet seResponseSet);

    Boolean hasSeRequestSet();
    SeRequestSet getSeRequestSet();


}
