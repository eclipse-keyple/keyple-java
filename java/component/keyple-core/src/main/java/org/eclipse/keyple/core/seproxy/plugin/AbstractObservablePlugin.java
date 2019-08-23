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
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observable plugin. These plugin can report when a reader is added or removed.
 */
public abstract class AbstractObservablePlugin extends AbstractLoggedObservable<PluginEvent>
        implements ReaderPlugin {

    private static final Logger logger = LoggerFactory.getLogger(AbstractObservablePlugin.class);

    /**
     * The list of readers
     */
    protected SortedSet<SeReader> readers = null;


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
                /* retrieve the current native reader list */
                readers = initNativeReaders();
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
     * @return the current reader list, may be empty but can't be null
     */
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
     * {@link SeReader} are new instances.
     * 
     * @return the list of SeReader objects.
     * @throws KeypleReaderException if a reader error occurs
     */
    protected abstract SortedSet<SeReader> initNativeReaders() throws KeypleReaderException;

    /**
     * Fetch connected native reader (from third party library) by its name Returns the current
     * {@link SeReader} if it is already
     * listed. Creates and returns a new
     * {@link SeReader} if not.
     *
     * @return the list of SeReader objects.
     * @throws KeypleReaderException if a reader error occurs
     */
    protected abstract SeReader fetchNativeReader(String name) throws KeypleReaderException;

    /**
     * Starts the monitoring thread
     * <p>
     * This abstract method has to be implemented by the class that handle the monitoring thread. It
     * will be called when a first observer is added.
     */
    protected abstract void startObservation();

    /**
     * Ends the monitoring thread
     * <p>
     * This abstract method has to be implemented by the class that handle the monitoring thread. It
     * will be called when the observer is removed.
     */
    protected abstract void stopObservation();

    /**
     * Add a plugin observer.
     * <p>
     * The observer will receive all the events produced by this plugin (reader insertion, removal,
     * etc.)
     * <p>
     * The monitoring thread is started when the first observer is added.
     *
     * @param observer the observer object
     */
    public final void addObserver(ObservablePlugin.PluginObserver observer) {
        super.addObserver(observer);
        if (super.countObservers() == 1) {
            logger.debug("Start monitoring the plugin {}", this.getName());
            startObservation();
        }
    }

    /**
     * Remove a plugin observer.
     * <p>
     * The observer will do not receive any of the events produced by this plugin.
     * <p>
     * The monitoring thread is ended when the last observer is removed.
     *
     * @param observer the observer object
     */
    public final void removeObserver(ObservablePlugin.PluginObserver observer) {
        super.removeObserver(observer);
        if (super.countObservers() == 0) {
            logger.debug("Stop the plugin monitoring.");
            stopObservation();
        }
    }

    @Override
    public void clearObservers() {
        super.clearObservers();
        stopObservation();
    }

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
    public final SeReader getReader(String name) throws KeypleReaderNotFoundException {
        for (SeReader reader : readers) {
            if (reader.getName().equals(name)) {
                return reader;
            }
        }
        throw new KeypleReaderNotFoundException(name);
    }
}
