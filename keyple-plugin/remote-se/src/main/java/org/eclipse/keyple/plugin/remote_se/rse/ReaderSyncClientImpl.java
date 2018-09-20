/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.rse;

import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;

public class ReaderSyncClientImpl implements IReaderSyncSession {

    String sessionId;
    SeRequestSet seRequestSet;
    ISeResponseSetCallback seResponseSetCallback;

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
