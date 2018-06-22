/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.event;

import org.eclipse.keyple.seproxy.ReadersPlugin;

/**
 * Observable plugin. These plugin can report when a reader is added or removed.
 */
public abstract class AbstractObservablePlugin extends AbstractLoggedObservable<AbstractPluginEvent>
        implements ReadersPlugin {

    protected String name;

    /**
     * Gets the reader name
     * 
     * @return the reader name string
     */
    public final String getName() {
        return name;
    }

    /**
     * Compare the name of the current ReadersPlugin to the name of the ReadersPlugin provided in
     * argument
     * 
     * @param plugin
     * @return true if the names match (The method is needed for the SortedSet lists)
     */
    public final int compareTo(ReadersPlugin plugin) {
        return this.getName().compareTo(plugin.getName());
    }

    public interface PluginObserver extends Observer {
        void update(AbstractPluginEvent event);
    }
}
