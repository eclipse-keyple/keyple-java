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
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractThreadedObservablePlugin extends AbstractObservablePlugin
        implements ObservablePlugin {

    private static final Logger logger =
            LoggerFactory.getLogger(AbstractThreadedObservablePlugin.class);

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
     * List of names of the connected readers
     */
    private static SortedSet<String> nativeReadersNames = new ConcurrentSkipListSet<String>();

    /**
     * Returns the list of names of all connected readers
     * 
     * @return readers names list
     * @throws KeypleReaderException if a reader error occurs
     */
    abstract protected SortedSet<String> getNativeReadersNames() throws KeypleReaderException;

    /**
     * Constructor
     * 
     * @param name name of the plugin
     */
    public AbstractThreadedObservablePlugin(String name) {
        super(name);
        /* create and launch the monitoring thread */
        thread = new EventThread(this.getName());
        thread.start();
    }

    @Override
    public final void addObserver(Observer observer) {
        super.addObserver(observer);
    }

    @Override
    public final void removeObserver(Observer observer) {
        super.removeObserver(observer);
    }

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
            try {
                while (running) {
                    /* retrieves the current readers names list */
                    SortedSet<String> actualNativeReadersNames = getNativeReadersNames();
                    /*
                     * checks if it has changed this algorithm favors cases where nothing change
                     */
                    if (!nativeReadersNames.equals(actualNativeReadersNames)) {
                        /*
                         * parse the current readers list, notify for disappeared readers, update
                         * readers list
                         */
                        for (AbstractObservableReader reader : readers) {
                            if (!actualNativeReadersNames.contains(reader.getName())) {
                                notifyObservers(new PluginEvent(this.pluginName, reader.getName(),
                                        PluginEvent.EventType.READER_DISCONNECTED));
                                readers.remove(reader);
                                logger.trace(
                                        "[{}][{}] Plugin thread => Remove unplugged reader from readers list.",
                                        this.pluginName, reader.getName());
                                /* remove reader name from the current list */
                                nativeReadersNames.remove(reader.getName());
                                reader = null;
                            }
                        }
                        /*
                         * parse the new readers list, notify for readers appearance, update readers
                         * list
                         */
                        for (String readerName : actualNativeReadersNames) {
                            if (!nativeReadersNames.contains(readerName)) {
                                AbstractObservableReader reader = getNativeReader(readerName);
                                readers.add(reader);
                                notifyObservers(new PluginEvent(this.pluginName, reader.getName(),
                                        PluginEvent.EventType.READER_CONNECTED));
                                logger.trace(
                                        "[{}][{}] Plugin thread => Add plugged reader to readers list.",
                                        this.pluginName, reader.getName());
                                /* add reader name to the current list */
                                nativeReadersNames.add(readerName);
                            }
                        }
                    }
                    /* sleep for a while. */
                    Thread.sleep(threadWaitTimeout);
                }
            } catch (Exception e) {
                logger.error("[{}] An exception occurred while monitoring plugin: {}, cause {}",
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

    public final void addObserver(PluginObserver observer) {
        super.addObserver(observer);
    }

    public final void removeObserver(PluginObserver observer) {
        super.removeObserver(observer);
    }
}
