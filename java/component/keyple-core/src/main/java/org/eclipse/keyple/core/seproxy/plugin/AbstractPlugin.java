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
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.Configurable;
import org.eclipse.keyple.core.util.Nameable;
import org.eclipse.keyple.core.util.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observable plugin. These plugin can report when a reader is added or removed.
 */
public abstract class AbstractPlugin extends Observable<PluginEvent>
        implements ReaderPlugin, Nameable, Configurable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPlugin.class);

    /** The plugin name (must be unique) */
    private final String name;

    /**
     * The list of readers
     */
    protected SortedSet<SeReader> readers = null;


    /**
     * Instantiates a new ReaderPlugin. Retrieve the current readers list.
     *
     * Gets the list for the native method the first time (null)
     *
     * @param name name of the plugin
     */
    protected AbstractPlugin(String name) {
        this.name = name;

        try {
            readers = initNativeReaders();
        } catch (KeypleReaderException e) {
            logger.error("Could not instantiate readers in plugin constructor {}", e.getMessage());
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
     * @return the current reader list, can be null if the
     */
    @Override
    public final SortedSet<SeReader> getReaders() throws KeypleReaderException {
        if (readers == null) {
            throw new KeypleReaderException("List of readers has not been initialized");
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
     * Fetch connected native readers (from third party library) and returns a list of corresponding
     * {@link SeReader}
     * <p>
     * {@link SeReader} are new instances.
     *
     * @return the list of AbstractReader objects.
     * @throws KeypleReaderException if a reader error occurs
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
     * Add a plugin observer.
     * <p>
     * The observer will receive all the events produced by this plugin (reader insertion, removal,
     * etc.)
     *
     * @param observer the observer object
     */
    public void addObserver(ObservablePlugin.PluginObserver observer) {
        logger.trace("[{}] addObserver => Adding '{}' as an observer of '{}'.",
                this.getClass().getSimpleName(), observer.getClass().getSimpleName(),
                this.getName());
        super.addObserver(observer);
    }

    /**
     * Remove a plugin observer.
     * <p>
     * The observer will do not receive any of the events produced by this plugin.
     *
     * @param observer the observer object
     */
    public void removeObserver(ObservablePlugin.PluginObserver observer) {
        logger.trace("[{}] removeObserver => Deleting a plugin observer", this.getName());
        super.removeObserver(observer);
    }

    /**
     * This method shall be called only from a SE Proxy plugin implementing AbstractPlugin. Push a
     * PluginEvent of the selected AbstractPlugin to its registered Observer.
     *
     * @param event the event
     */
    @Override
    public final void notifyObservers(final PluginEvent event) {

        logger.trace(
                "[{}] AbstractPlugin => Notifying a plugin event to {} observers. EVENTNAME = {} ",
                this.getName(), this.countObservers(), event.getEventType().getName());

        setChanged();

        super.notifyObservers(event);
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
    public final void setParameters(Map<String, String> parameters)
            throws IllegalArgumentException, KeypleBaseException {
        for (Map.Entry<String, String> en : parameters.entrySet()) {
            setParameter(en.getKey(), en.getValue());
        }
    }
}
