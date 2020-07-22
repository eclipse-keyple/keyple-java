/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
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

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractThreadedObservablePlugin} class provides the means to observe a plugin
 * (insertion/removal of readers) using a monitoring thread.
 */
public abstract class AbstractThreadedObservablePlugin extends AbstractObservablePlugin {
    private static final Logger logger =
            LoggerFactory.getLogger(AbstractThreadedObservablePlugin.class);

    /**
     * Instantiates a threaded observable plugin.
     *
     * @param name name of the plugin
     * @throws KeypleReaderException when an issue is raised with reader
     */
    protected AbstractThreadedObservablePlugin(String name) {
        super(name);
    }

    /**
     * Fetch the list of connected native reader (usually from third party library) and returns
     * their names (or id)
     *
     * @return connected readers' name list
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    protected abstract SortedSet<String> fetchNativeReadersNames();

    /**
     * Fetch connected native reader (from third party library) by its name Returns the current
     * {@link AbstractReader} if it is already listed. Creates and returns a new
     * {@link AbstractReader} if not.
     *
     * @param name the reader name
     * @return the list of AbstractReader objects.
     * @throws KeypleReaderNotFoundException if the reader was not found by its name
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    protected abstract SeReader fetchNativeReader(String name);

    /**
     * Add a plugin observer.
     * <p>
     * Overrides the method defined in {@link AbstractObservablePlugin}, a thread is created if it
     * does not already exist (when the first observer is added).
     *
     * @param observer the observer object
     */
    @Override
    public final void addObserver(final ObservablePlugin.PluginObserver observer) {
        super.addObserver(observer);
        if (countObservers() == 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("Start monitoring the plugin {}", this.getName());
            }
            thread = new EventThread(this.getName());
            thread.start();
        }
    }

    /**
     * Remove a plugin observer.
     * <p>
     * Overrides the method defined in {@link AbstractObservablePlugin}, the monitoring thread is
     * ended when the last observer is removed.
     *
     * @param observer the observer object
     */
    @Override
    public final void removeObserver(final ObservablePlugin.PluginObserver observer) {
        super.removeObserver(observer);
        if (countObservers() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Stop the plugin monitoring.");
            }
            if (thread != null) {
                thread.end();
            }
        }
    }

    /**
     * Remove all observers at once
     * <p>
     * Overrides the method defined in {@link AbstractObservablePlugin}, the thread is ended.
     * 
     */
    @Override
    public final void clearObservers() {
        super.clearObservers();
        if (thread != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Stop the plugin monitoring.");
            }
            thread.end();
        }
    }

    /**
     * Check weither the background job is monitoring for new readers
     * 
     * @return true, if the background job is monitoring, false in all other cases.
     * @deprecated will change in a later version
     */
    @Deprecated
    protected Boolean isMonitoring() {
        return thread != null && thread.isAlive() && thread.isMonitoring();
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
    private final SortedSet<String> nativeReadersNames = new ConcurrentSkipListSet<String>();

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

        boolean isMonitoring() {
            return running;
        }

        @Override
        public void run() {
            SortedSet<String> changedReaderNames = new ConcurrentSkipListSet<String>();
            try {
                while (running) {
                    /* retrieves the current readers names list */
                    SortedSet<String> actualNativeReadersNames =
                            AbstractThreadedObservablePlugin.this.fetchNativeReadersNames();
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
                        final Collection<SeReader> readerCollection = readers.values();
                        for (SeReader reader : readerCollection) {
                            if (!actualNativeReadersNames.contains(reader.getName())) {
                                changedReaderNames.add(reader.getName());
                            }
                        }
                        /* notify disconnections if any and update the reader list */
                        if (!changedReaderNames.isEmpty()) {
                            /* grouped notification */
                            if (logger.isTraceEnabled()) {
                                logger.trace("Notifying disconnection(s): {}", changedReaderNames);
                            }
                            notifyObservers(new PluginEvent(this.pluginName, changedReaderNames,
                                    PluginEvent.EventType.READER_DISCONNECTED));
                            /* list update */
                            for (SeReader reader : readerCollection) {
                                if (!actualNativeReadersNames.contains(reader.getName())) {
                                    /* removes any possible observers before removing the reader */
                                    if (reader instanceof ObservableReader) {
                                        ((ObservableReader) reader).clearObservers();

                                        // In case where Reader was detecting SE
                                        ((ObservableReader) reader).stopSeDetection();
                                    }
                                    readers.remove(reader.getName());
                                    if (logger.isTraceEnabled()) {
                                        logger.trace(
                                                "[{}][{}] Plugin thread => Remove unplugged reader from readers list.",
                                                this.pluginName, reader.getName());
                                    }
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
                                readers.put(reader.getName(), reader);
                                /* add to the notification list */
                                changedReaderNames.add(readerName);
                                if (logger.isTraceEnabled()) {
                                    logger.trace(
                                            "[{}][{}] Plugin thread => Add plugged reader to readers list.",
                                            this.pluginName, reader.getName());
                                }
                                /* add reader name to the current list */
                                nativeReadersNames.add(readerName);
                            }
                        }
                        /* notify connections if any */
                        if (!changedReaderNames.isEmpty()) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("Notifying connection(s): {}", changedReaderNames);
                            }
                            notifyObservers(new PluginEvent(this.pluginName, changedReaderNames,
                                    PluginEvent.EventType.READER_CONNECTED));
                        }
                    }
                    /* sleep for a while. */
                    Thread.sleep(threadWaitTimeout);
                }
            } catch (InterruptedException e) {
                logger.warn("[{}] An exception occurred while monitoring plugin: {}",
                        this.pluginName, e.getMessage(), e);
                // Restore interrupted state...      
                Thread.currentThread().interrupt();
            } catch (KeypleReaderException e) {
                logger.warn("[{}] An exception occurred while monitoring plugin: {}",
                        this.pluginName, e.getMessage(), e);
            }
        }
    }

    /**
     * Called when the class is unloaded. Attempt to do a clean exit.
     *
     * @throws Throwable a generic exception
     * @deprecated will change in a later version
     */
    @Deprecated
    @Override
    protected void finalize() throws Throwable {
        thread.end();
        if (logger.isTraceEnabled()) {
            logger.trace("[{}] Observable Plugin thread ended.", this.getName());
        }
        super.finalize();
    }
}
