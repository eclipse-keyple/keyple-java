package org.eclipse.keyple.plugin.remote_se.rse;

import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;

public class ReaderSyncClientImpl implements ReaderSyncSession {

    String sessionId;

    public ReaderSyncClientImpl(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public SeResponseSet transmit(SeRequestSet seApplicationRequest) {
        return null;
    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public Boolean isAsync() {
        return null;
    }
}
