/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.event;


import java.util.Map;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractLoggedObservable;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSettings;
import org.eclipse.keyple.util.Observable;


/**
 * 
 * Abstract definition of an observable reader. Factorizes setSetProtocols and will factorize the
 * transmit method logging
 * 
 */

public abstract class AbstractObservableReader extends AbstractLoggedObservable<ReaderEvent>
        implements ProxyReader {


    public interface ReaderObserver extends AbstractLoggedObservable.Observer<ReaderEvent> {
        void update(Observable reader, ReaderEvent event);
    }

    /**
     * PO selection map associating seProtocols and selection strings (e.g. ATR regex for Pcsc
     * plugins)
     */
    public Map<SeProtocol, String> protocolsMap;

    @Override
    public final void addSeProtocolSetting(Map<SeProtocol, String> seProtocolSettings)
            throws IOReaderException {
        this.protocolsMap.putAll(seProtocolSettings);
    }

    @Override
    public final void addSeProtocolSetting(SeProtocol flag, String value) {
        this.protocolsMap.put(flag, value);
    }

    @Override
    public final void addSeProtocolSetting(SeProtocolSettings seProtocolSetting) {
        addSeProtocolSetting(seProtocolSetting.getFlag(), seProtocolSetting.getValue());
    }

    @Override
    public final <T extends Enum<T>> void addSeProtocolSetting(Class<T> settings) {
        for (Enum<T> setting : settings.getEnumConstants()) {
            addSeProtocolSetting((SeProtocolSettings) setting);
        }
    }
}
