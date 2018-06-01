/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;


import java.util.Map;
import org.eclipse.keyple.util.Observable;

/**
 * 
 * Abstract definition of an observable reader.
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
    public void setProtocols(Map<SeProtocol, String> seProtocolSettings) {
        this.protocolsMap = seProtocolSettings;
    }
}
