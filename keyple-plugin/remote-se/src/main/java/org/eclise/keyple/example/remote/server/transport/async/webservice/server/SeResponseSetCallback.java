package org.eclise.keyple.example.remote.server.transport.async.webservice.server;

import org.eclipse.keyple.seproxy.SeResponseSet;

/**
 * Callback function for AsyncTransmit
 */
public interface SeResponseSetCallback {

    /**
     * Callback function called when SeResponseSet is received
     * @param seResponseSet
     */
    void getResponseSet(SeResponseSet seResponseSet);
}
