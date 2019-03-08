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
package org.eclipse.keyple.seproxy.plugin;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
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
     * @return the current reader list, can be null if the
     */
    public final SortedSet<AbstractObservableReader> getReaders() throws KeypleReaderException {
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
        for (AbstractObservableReader reader : readers) {
            readerNames.add(reader.getName());
        }
        return readerNames;
    }

    /**
     * Fetch connected native readers (from third party library) and returns a list of corresponding
     * {@link org.eclipse.keyple.seproxy.plugin.AbstractObservableReader}
     * {@link org.eclipse.keyple.seproxy.plugin.AbstractObservableReader} are new instances.
     * 
     * @return the list of AbstractObservableReader objects.
     * @throws KeypleReaderException if a reader error occurs
     */
    protected abstract SortedSet<AbstractObservableReader> initNativeReaders()
            throws KeypleReaderException;

    /**
     * Fetch connected native reader (from third party library) by its name Returns the current
     * {@link org.eclipse.keyple.seproxy.plugin.AbstractObservableReader} if it is already listed.
     * Creates and returns a new {@link org.eclipse.keyple.seproxy.plugin.AbstractObservableReader}
     * if not.
     *
     * @return the list of AbstractObservableReader objects.
     * @throws KeypleReaderException if a reader error occurs
     */
    protected abstract AbstractObservableReader fetchNativeReader(String name)
            throws KeypleReaderException;

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
            logger.debug("Start the plugin monitoring.");
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
        throw new KeypleReaderNotFoundException(name);
    }
}
