/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.sync.local.client;

import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclise.keyple.example.remote.server.transport.sync.SyncClientListener;

/**
 * Link with Local Reader
 */
public class LocalClientListener implements SyncClientListener {

    ProxyReader localReader;

    public LocalClientListener(ProxyReader _localReader) {
        localReader = _localReader;
    }

    @Override
    public SeResponseSet onTransmit(SeRequestSet req) {
        try {
            return localReader.transmit(req);
        } catch (IOReaderException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String onGetName() {
        return localReader.getName();
    }

    @Override
    public Boolean onIsSePresent() {
        try {
            return localReader.isSePresent();
        } catch (NoStackTraceThrowable noStackTraceThrowable) {
            noStackTraceThrowable.printStackTrace();
        }
        return null;
    }

    @Override
    public void onAddSeProtocolSetting(SeProtocolSetting seProtocolSetting) {
        localReader.addSeProtocolSetting(seProtocolSetting);
    }
}
