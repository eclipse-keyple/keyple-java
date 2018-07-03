/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.event;

import java.util.SortedSet;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReadersPlugin;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.UnexpectedReaderException;

/**
 * Observable plugin. These plugin can report when a reader is added or removed.
 */
abstract class AbstractObservablePlugin extends AbstractLoggedObservable<AbstractPluginEvent>
        implements ReadersPlugin {

    /**
     * The list of readers
     */
    protected SortedSet<AbstractObservableReader> readers = null;


    /**
     * Instanciates a new readersplugin.
     * Retrieve the current readers list.<br/>
     * Gets the list for the native method the first time (null)<br/>
     * @param name name of the plugin
     */
    protected AbstractObservablePlugin(String name) {
        super(name);
        if (readers == null) {
            try {
                readers = getNativeReaders();
            } catch (IOReaderException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the current readers list.<br/>
     * The list is initialized in the constructor and may be updated in background in the case of a threaded plugin
     * {@link AbstractThreadedObservablePlugin}
     * 
     * @return
     */
    public final SortedSet<AbstractObservableReader> getReaders() {
        return readers;
    }

    /**
     * Gets a list of native readers from the native methods
     * 
     * @return the list of AbstractObservableReader objects.
     * @throws IOReaderException
     */
    protected abstract SortedSet<AbstractObservableReader> getNativeReaders()
            throws IOReaderException;

    /**
     * Gets the specific reader whose is provided as an argument.
     * 
     * @param name
     * @return the AbstractObservableReader object (null if not found)
     * @throws IOReaderException
     */
    protected abstract AbstractObservableReader getNativeReader(String name)
            throws IOReaderException;

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

    /**
     * Gets a specific reader designated by its name in the current readers list
     * @param name of the reader
     * @return the reader
     * @throws UnexpectedReaderException
     */
    public final ProxyReader getReader(String name) throws UnexpectedReaderException {
        for (ProxyReader reader : readers) {
            if (reader.getName().equals(name)) {
                return reader;
            }
        }
        throw new UnexpectedReaderException("Reader " + name + "not found.");
    }

    public interface PluginObserver extends Observer {
        void update(AbstractPluginEvent event);
    }
}
