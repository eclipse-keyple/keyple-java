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

import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.util.Configurable;
import org.eclipse.keyple.core.util.Nameable;

/**
 * Observable plugin. These plugin can report when a reader is added or removed.
 */
public abstract class AbstractPlugin
        implements ReaderPlugin, ObservablePlugin, Nameable, Configurable {

    /** The plugin name (must be unique) */
    private final String name;

    /**
     * The list of readers
     */
    protected SortedSet<SeReader> readers = null;

    /**
     * Instantiates a new ReaderPlugin. Retrieve the current readers list.
     *
     * Initialize the list of readers calling the abstract method initNativeReaders
     *
     * When readers initialisation failed, a KeypleRuntimeException is thrown
     *
     * @param name name of the plugin
     */
    protected AbstractPlugin(String name) {
        this.name = name;

        try {
            readers = initNativeReaders();
        } catch (KeypleReaderException e) {
            throw new KeypleRuntimeException("Could not instantiate readers in plugin constructor",
                    e);
        }
    }

    /**
     * Gets the plugin name
     *
     * @return the plugin name string
     */
    @Override
    public final String getName() {
        return name;
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
        if (readers == null) {
            throw new KeypleRuntimeException(
                    "Readers list is null, it has not been initialized properly, check initNativeReaders()");
        }
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
     * @throws KeypleReaderException if a reader error when readers list initialization, it will be
     *         thrown by the constructor in a KeypleRuntimeException to be caught at a higher level
     *         by the {@link org.eclipse.keyple.core.seproxy.AbstractPluginFactory}
     */
    protected abstract SortedSet<SeReader> initNativeReaders() throws KeypleReaderException;

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

    /**
     * Set a list of parameters on a plugin.
     * <p>
     * See {@link #setParameter(String, String)} for more details
     *
     * @param parameters the new parameters
     * @throws KeypleBaseException This method can fail when disabling the exclusive mode as it's
     *         executed instantly
     */
    @Override
    public final void setParameters(Map<String, String> parameters) throws KeypleBaseException {
        for (Map.Entry<String, String> en : parameters.entrySet()) {
            setParameter(en.getKey(), en.getValue());
        }
    }
}
