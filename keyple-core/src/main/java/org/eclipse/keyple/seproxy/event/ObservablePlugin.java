/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.seproxy.event;

import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.util.Observable;

public interface ObservablePlugin extends ReaderPlugin {
    interface PluginObserver extends Observable.Observer<PluginEvent> {
        void update(PluginEvent event);
    }

    void addObserver(PluginObserver observer);

    void removeObserver(PluginObserver observer);

    void notifyObservers(PluginEvent event);
}
