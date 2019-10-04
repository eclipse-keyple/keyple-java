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
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observable plugin. These plugin can report when a reader is added or removed.
 */
public abstract class AbstractPlugin extends AbstractLoggedObservable<PluginEvent>
        implements ReaderPlugin {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPlugin.class);

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
    protected AbstractPlugin(String name) {
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
     * threaded plugin {@link ThreadedMonitoringPlugin}
     * 
     * @return the current reader list, can be null if the
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
     * <p>
     * {@link SeReader} are new instances.
     * 
     * @return the list of AbstractReader objects.
     * @throws KeypleReaderException if a reader error occurs
     */
    protected abstract SortedSet<SeReader> initNativeReaders() throws KeypleReaderException;

    /**
     * Fetch connected native reader (from third party library) by its name Returns the current
     * {@link AbstractReader} if it is already listed. Creates and returns a new
     * {@link AbstractReader} if not.
     *
     * @param name the reader name
     * @return the list of AbstractReader objects.
     * @throws KeypleReaderException if a reader error occurs
     */
    protected abstract SeReader fetchNativeReader(String name) throws KeypleReaderException;

    /**
     * Add a plugin observer.
     * <p>
     * The observer will receive all the events produced by this plugin (reader insertion, removal,
     * etc.)
     * <p>
     * In the case of a {@link ThreadedMonitoringPlugin}, a thread is created if it does not already
     * exist (when the first observer is added).
     *
     * @param observer the observer object
     */
    public final void addObserver(ObservablePlugin.PluginObserver observer) {
        super.addObserver(observer);
        if (this instanceof ThreadedMonitoringPlugin) {
            if (super.countObservers() == 1) {
                logger.debug("Start monitoring the plugin {}", this.getName());
                thread = new EventThread(this.getName());
                thread.start();
            }
        }
    }

    /**
     * Remove a plugin observer.
     * <p>
     * The observer will do not receive any of the events produced by this plugin.
     * <p>
     * In the case of a {@link ThreadedMonitoringPlugin}, the monitoring thread is ended when the
     * last observer is removed.
     *
     * @param observer the observer object
     */
    public final void removeObserver(ObservablePlugin.PluginObserver observer) {
        super.removeObserver(observer);
        if (super.countObservers() == 0) {
            logger.debug("Stop the plugin monitoring.");
            if (thread != null) {
                thread.end();
            }
        }
    }

    @Override
    public void clearObservers() {
        super.clearObservers();
        if (thread != null) {
            logger.debug("Stop the plugin monitoring.");
            thread.end();
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
    public final SeReader getReader(String name) throws KeypleReaderNotFoundException {
        for (SeReader reader : readers) {
            if (reader.getName().equals(name)) {
                return reader;
            }
        }
        throw new KeypleReaderNotFoundException(name);
    }

    /* Reader insertion/removal management */

    private static final long SETTING_THREAD_TIMEOUT_DEFAULT = 1000;

    /**
     * Local thread to monitoring readers presence
     */
    private EventThread thread;

    /**
     * Thread wait timeout in ms
     *
     * This timeout value will determined the latency to detect changes
     */
    protected long threadWaitTimeout = SETTING_THREAD_TIMEOUT_DEFAULT;

    /**
     * List of names of the physical (native) connected readers This list helps synchronizing
     * physical readers managed by third-party library such as smardcard.io and the list of keyple
     * {@link org.eclipse.keyple.core.seproxy.SeReader} Insertion, removal, and access operations
     * safely execute concurrently by multiple threads.
     */
    private SortedSet<String> nativeReadersNames = new ConcurrentSkipListSet<String>();

    /**
     * Thread in charge of reporting live events
     */
    private class EventThread extends Thread {
        private final String pluginName;
        private boolean running = true;

        private EventThread(String pluginName) {
            this.pluginName = pluginName;
        }

        /**
         * Marks the thread as one that should end when the last cardWaitTimeout occurs
         */
        void end() {
            running = false;
            this.interrupt();
        }

        public void run() {
            SortedSet<String> changedReaderNames = new ConcurrentSkipListSet<String>();
            try {
                while (running) {
                    /* retrieves the current readers names list */
                    SortedSet<String> actualNativeReadersNames =
                            ((ThreadedMonitoringPlugin) AbstractPlugin.this)
                                    .fetchNativeReadersNames();
                    /*
                     * checks if it has changed this algorithm favors cases where nothing change
                     */
                    if (!nativeReadersNames.equals(actualNativeReadersNames)) {
                        /*
                         * parse the current readers list, notify for disappeared readers, update
                         * readers list
                         */
                        /* build changed reader names list */
                        changedReaderNames.clear();
                        for (SeReader reader : readers) {
                            if (!actualNativeReadersNames.contains(reader.getName())) {
                                changedReaderNames.add(reader.getName());
                            }
                        }
                        /* notify disconnections if any and update the reader list */
                        if (changedReaderNames.size() > 0) {
                            /* grouped notification */
                            logger.trace("Notifying disconnection(s): {}", changedReaderNames);
                            notifyObservers(new PluginEvent(this.pluginName, changedReaderNames,
                                    PluginEvent.EventType.READER_DISCONNECTED));
                            /* list update */
                            for (SeReader reader : readers) {
                                if (!actualNativeReadersNames.contains(reader.getName())) {
                                    /* removes any possible observers before removing the reader */
                                    if (reader instanceof ObservableReader) {
                                        ((ObservableReader) reader).clearObservers();
                                    }
                                    readers.remove(reader);
                                    logger.trace(
                                            "[{}][{}] Plugin thread => Remove unplugged reader from readers list.",
                                            this.pluginName, reader.getName());
                                    /* remove reader name from the current list */
                                    nativeReadersNames.remove(reader.getName());
                                }
                            }
                            /* clean the list for a possible connection notification */
                            changedReaderNames.clear();
                        }
                        /*
                         * parse the new readers list, notify for readers appearance, update readers
                         * list
                         */
                        for (String readerName : actualNativeReadersNames) {
                            if (!nativeReadersNames.contains(readerName)) {
                                SeReader reader = fetchNativeReader(readerName);
                                readers.add(reader);
                                /* add to the notification list */
                                changedReaderNames.add(readerName);
                                logger.trace(
                                        "[{}][{}] Plugin thread => Add plugged reader to readers list.",
                                        this.pluginName, reader.getName());
                                /* add reader name to the current list */
                                nativeReadersNames.add(readerName);
                            }
                        }
                        /* notify connections if any */
                        if (changedReaderNames.size() > 0) {
                            logger.trace("Notifying connection(s): {}", changedReaderNames);
                            notifyObservers(new PluginEvent(this.pluginName, changedReaderNames,
                                    PluginEvent.EventType.READER_CONNECTED));
                        }
                    }
                    /* sleep for a while. */
                    Thread.sleep(threadWaitTimeout);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.warn("[{}] An exception occurred while monitoring plugin: {}, cause {}",
                        this.pluginName, e.getMessage(), e.getCause());
            } catch (KeypleReaderException e) {
                e.printStackTrace();
                logger.warn("[{}] An exception occurred while monitoring plugin: {}, cause {}",
                        this.pluginName, e.getMessage(), e.getCause());
            }
        }
    }

    /**
     * Called when the class is unloaded. Attempt to do a clean exit.
     *
     * @throws Throwable a generic exception
     */
    @Override
    protected void finalize() throws Throwable {
        thread.end();
        thread = null;
        logger.trace("[{}] Observable Plugin thread ended.", this.getName());
        super.finalize();
    }
}
