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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.util.Configurable;
import org.eclipse.keyple.core.util.Nameable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observable plugin. These plugin can report when a reader is added or removed.
 */
public abstract class AbstractPlugin
        implements ReaderPlugin, ObservablePlugin, Nameable, Configurable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPlugin.class);

    /** The plugin name (must be unique) */
    private final String name;

    /**
     * The list of readers
     */
    protected SortedSet<SeReader> readers = null;

    /* The observers of this object */
    private Set<ObservablePlugin.PluginObserver> observers;
    /*
     * this object will be used to synchronize the access to the observers list in order to be
     * thread safe
     */
    private final Object SYNC = new Object();

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
     * Add a plugin observer.
     * <p>
     * The observer will receive all the events produced by this plugin (reader insertion, removal,
     * etc.)
     *
     * @param observer the observer object
     */
    @Override
    public void addObserver(ObservablePlugin.PluginObserver observer) {
        if (observer == null) {
            return;
        }

        logger.trace("[{}] addObserver => Adding '{}' as an observer of '{}'.",
                this.getClass().getSimpleName(), observer.getClass().getSimpleName(), getName());

        synchronized (SYNC) {
            if (observers == null) {
                observers = new HashSet<ObservablePlugin.PluginObserver>(1);
            }
            observers.add(observer);
        }
    }

    /**
     * Remove a plugin observer.
     * <p>
     * The observer will do not receive any of the events produced by this plugin.
     *
     * @param observer the observer object
     */
    @Override
    public void removeObserver(ObservablePlugin.PluginObserver observer) {
        if (observer == null) {
            return;
        }

        logger.trace("[{}] removeObserver => Deleting a plugin observer", getName());

        synchronized (SYNC) {
            if (observers != null) {
                observers.remove(observer);
            }
        }
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

        Set<ObservablePlugin.PluginObserver> observersCopy;

        synchronized (SYNC) {
            if (observers == null) {
                return;
            }
            observersCopy = new HashSet<ObservablePlugin.PluginObserver>(observers);
        }

        for (ObservablePlugin.PluginObserver observer : observersCopy) {
            observer.update(event);
        }
    }

    /**
     * @return the current number of observers
     */
    @Override
    public int countObservers() {
        return observers == null ? 0 : observers.size();
    }

    /**
     * Remove all the observers
     */
    @Override
    public void clearObservers() {
        if (observers != null) {
            this.observers.clear();
        }
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
