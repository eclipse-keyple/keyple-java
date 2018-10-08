/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.nse;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;


/**
 * Manages binding between readerName and a Master SessionId
 */
class NseSessionManager {

    private final Map<String, String> readerName_sessionId;

    public NseSessionManager() {
        readerName_sessionId = new HashMap<String, String>();
    }


    void addNewSession(String sessionId, String readerName) {
        readerName_sessionId.put(readerName, sessionId);

    }


    String getLastSession(String readerName) {
        return readerName_sessionId.get(readerName);
    }

    String findReaderNameBySession(String sessionId) throws KeypleReaderNotFoundException {
        for (String readerName : readerName_sessionId.keySet()) {
            if (readerName_sessionId.get(readerName).equals(sessionId)) {
                return readerName;
            }
        }
        throw new KeypleReaderNotFoundException("Reader not found by sessionId");
    }

}
