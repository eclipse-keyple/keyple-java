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
package org.eclipse.keyple.core.seproxy.event;

import org.eclipse.keyple.core.seproxy.ReaderPlugin;

/**
 * The ObservablePlugin interface provides the API to observe readers in plugins.
 * <p>
 * Allows subscribed observers to receive a PluginEvent when a reader is connected/disconnected
 */
public interface ObservablePlugin extends ReaderPlugin {
    /**
     * Interface to be implemented by plugin observers.
     */
    interface PluginObserver {
        void update(PluginEvent event);
    }

    /**
     * Add a plugin observer.
     * <p>
     * The observer will receive all the events produced by this plugin (reader connection,
     * disconnection)
     *
     * @param observer the observer object
     */
    void addObserver(PluginObserver observer);

    /**
     * Remove a plugin observer.
     * <p>
     * The observer will not receive any of the events produced by this plugin.
     *
     * @param observer the observer object
     */
    void removeObserver(PluginObserver observer);

    /**
     * Push a PluginEvent of the {@link ObservablePlugin} to its registered observers.
     *
     * @param event the event (see {@link PluginEvent})
     */
    void notifyObservers(PluginEvent event);

    /**
     * Remove all observers at once
     */
    void clearObservers();

    /**
     * @return the number of observers
     */
    int countObservers();
}
