/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.event;


public class ReaderPresencePluginEvent extends AbstractPluginEvent {
    private final boolean added;
    private final String pluginName;
    private final String readerName;

    public ReaderPresencePluginEvent(boolean added, String pluginName, String readerName) {
        this.added = added;
        this.pluginName = pluginName;
        this.readerName = readerName;
    }

    /**
     * Define if the reader was added or removed
     * 
     * @return true for added
     */
    public boolean isAdded() {
        return added;
    }

    /**
     * Gets the name of the plugin that produced the event
     *
     * @return String plugin name
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * Gets the name of the reader that was added or removed
     * 
     * @return String reader name
     */
    public String getReaderName() {
        return readerName;
    }
}
