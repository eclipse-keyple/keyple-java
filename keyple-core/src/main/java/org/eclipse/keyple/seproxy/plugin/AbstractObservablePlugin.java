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

package org.eclipse.keyple.seproxy.plugin;

import java.util.SortedSet;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;

/**
 * Observable plugin. These plugin can report when a reader is added or removed.
 */
abstract class AbstractObservablePlugin extends AbstractLoggedObservable<PluginEvent>
        implements ReaderPlugin {

    /**
     * The list of readers
     */
    protected SortedSet<AbstractObservableReader> readers = null;


    /**
     * Instanciates a new ReaderPlugin. Retrieve the current readers list.
     *
     * Gets the list for the native method the first time (null)
     * 
     * @param name name of the plugin
     */
    protected AbstractObservablePlugin(String name) {
        super(name);
        if (readers == null) {
            try {
                readers = getNativeReaders();
            } catch (KeypleReaderException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the current readers list.
     *
     * The list is initialized in the constructor and may be updated in background in the case of a
     * threaded plugin {@link AbstractThreadedObservablePlugin}
     * 
     * @return the current reader list, can be null if the
     */
    public final SortedSet<AbstractObservableReader> getReaders() throws KeypleReaderException {
        if (readers == null) {
            throw new KeypleReaderException("List of readers has not been initialized");
        }
        return readers;
    }

    /**
     * Gets a list of native readers from the native methods
     * 
     * @return the list of AbstractObservableReader objects.
     * @throws KeypleReaderException if a reader error occurs
     */
    protected abstract SortedSet<AbstractObservableReader> getNativeReaders()
            throws KeypleReaderException;

    /**
     * Gets the specific reader whose is provided as an argument.
     * 
     * @param name the of the reader
     * @return the AbstractObservableReader object (null if not found)
     * @throws KeypleReaderException if a reader error occurs
     */
    protected abstract AbstractObservableReader getNativeReader(String name)
            throws KeypleReaderException;

    /**
     * Compare the name of the current ReaderPlugin to the name of the ReaderPlugin provided in
     * argument
     * 
     * @param plugin a {@link ReaderPlugin} object
     * @return true if the names match (The method is needed for the SortedSet lists)
     */
    public final int compareTo(ReaderPlugin plugin) {
        return this.getName().compareTo(plugin.getName());
    }

    /**
     * Gets a specific reader designated by its name in the current readers list
     * 
     * @param name of the reader
     * @return the reader
     * @throws KeypleReaderNotFoundException if the wanted reader is not found
     */
    public final ProxyReader getReader(String name) throws KeypleReaderNotFoundException {
        for (ProxyReader reader : readers) {
            if (reader.getName().equals(name)) {
                return reader;
            }
        }
        throw new KeypleReaderNotFoundException("Reader " + name + "not found.");
    }
}
