/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.seproxy.event;

import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.util.Observable;

public interface ObservableReader extends ProxyReader {
    interface ReaderObserver extends Observable.Observer<ReaderEvent> {
    }

    void addObserver(ReaderObserver observer);

    void removeObserver(ReaderObserver observer);

    void notifyObservers(ReaderEvent event);
}
