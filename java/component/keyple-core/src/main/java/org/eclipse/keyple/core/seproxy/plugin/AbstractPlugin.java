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
package org.eclipse.keyple.core.seproxy.plugin;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.*;

/**
 * Observable plugin. These plugin can report when a reader is added or removed.
 */
public abstract class AbstractPlugin extends AbstractSeProxyComponent implements ReaderPlugin {

    /**
     * The list of readers
     */
    protected ConcurrentSkipListSet<SeReader> readers = new ConcurrentSkipListSet<SeReader>();

    /**
     * Instantiates a new ReaderPlugin. Retrieve the current readers list.
     *
     * Initialize the list of readers calling the abstract method initNativeReaders
     *
     * When readers initialisation failed, a KeypleReaderException is thrown
     *
     * @param name name of the plugin
     * @throws KeypleReaderException when an issue is raised with reader
     */
    protected AbstractPlugin(String name) throws KeypleReaderException {
        super(name);
        readers.addAll(initNativeReaders());
    }

    /**
     * Returns the current readers list.
     *
     * The list is initialized in the constructor and may be updated in background in the case of a
     * threaded plugin {@link AbstractThreadedObservablePlugin}
     *
     * @return the current reader list, can be an empty list
     */
    @Override
    public final SortedSet<SeReader> getReaders() {
        return readers;
    }

    /**
     * Returns the current list of reader names.
     *
     * The list of names is built from the current readers list
     *
     * @return a list of String
     */
    @Override
    public final SortedSet<String> getReaderNames() {
        SortedSet<String> readerNames = new ConcurrentSkipListSet<String>();
        for (SeReader reader : readers) {
            readerNames.add(reader.getName());
        }
        return readerNames;
    }

    /**
     * Init connected native readers (from third party library) and returns a list of corresponding
     * {@link SeReader}
     * <p>
     * {@link SeReader} are new instances.
     * <p>
     * this method is called once in the plugin constructor.
     *
     * @return the list of AbstractReader objects.
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    protected abstract SortedSet<SeReader> initNativeReaders() throws KeypleReaderIOException;

    /**
     * Compare the name of the current ReaderPlugin to the name of the ReaderPlugin provided in
     * argument
     *
     * @param plugin a {@link ReaderPlugin} object
     * @return true if the names match (The method is needed for the SortedSet lists)
     */
    @Override
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
    @Override
    public final SeReader getReader(String name) throws KeypleReaderNotFoundException {
        for (SeReader reader : readers) {
            if (reader.getName().equals(name)) {
                return reader;
            }
        }
        throw new KeypleReaderNotFoundException(name);
    }
}
