/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.event;

import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public abstract class AbstractThreadedObservablePlugin extends AbstractObservablePlugin {

    private static final ILogger logger =
            SLoggerFactory.getLogger(AbstractThreadedObservablePlugin.class);

    private static final long SETTING_THREAD_TIMEOUT_DEFAULT = 1000;

    /**
     * Thread wait timeout in ms<br/>
     * This timeout value will determined the latency to detect changes
     */
    private long threadWaitTimeout = SETTING_THREAD_TIMEOUT_DEFAULT;

    /**
     * Local thread to monitoring readers presence
     */
    private EventThread thread;


    /**
     * List of names of the connected readers
     */
    protected static SortedSet<String> nativeReadersNames = new ConcurrentSkipListSet<String>();

    /**
     * Returns the list of names of all connected readers
     * 
     * @return readers names list
     * @throws IOReaderException
     */
    abstract protected SortedSet<String> getNativeReadersNames() throws IOReaderException;

    public AbstractThreadedObservablePlugin(String name) {
        super(name);
        /// create and launch the monitoring thread
        thread = new EventThread(this.getName());
        thread.start();
    }

    @Override
    public final void addObserver(Observer observer) {
        super.addObserver(observer);
    }

    @Override
    public final void removeObserver(Observer observer) {
        super.addObserver(observer);
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

        private void exceptionThrown(Exception e) {
            notifyObservers(new ErrorPluginEvent(e));
        }

        public void run() {
            try {
                while (running) {
                    // retrieves the current readers names list
                    SortedSet<String> actualNativeReadersNames = getNativeReadersNames();
                    // checks if it has changed
                    // this algorithm favors cases where nothing change
                    if (!nativeReadersNames.equals(actualNativeReadersNames)) {
                        // parse the current readers list, notify for disappeared readers, update
                        // readers list
                        for (AbstractObservableReader reader : readers) {
                            if (!actualNativeReadersNames.contains(reader.getName())) {
                                notifyObservers(new ReaderPresencePluginEvent(false,
                                        this.pluginName, reader.getName()));
                                readers.remove(reader);
                                logger.info("Remove unplugged reader from readers list", "plugin",
                                        pluginName, "reader", reader.getName());
                                reader = null;
                            }
                        }
                        // parse the new readers list, notify for readers appearance, update readers
                        // list
                        for (String readerName : actualNativeReadersNames) {
                            if (!nativeReadersNames.contains(readerName)) {
                                AbstractObservableReader reader = getNativeReader(readerName);
                                readers.add(reader);
                                notifyObservers(new ReaderPresencePluginEvent(true, this.pluginName,
                                        reader.getName()));
                                logger.info("Add plugged reader to readers list", "reader",
                                        reader.getName());
                            }
                        }
                        // update the readers names list
                        nativeReadersNames = actualNativeReadersNames;
                    }
                    // sleep for a while.
                    Thread.sleep(threadWaitTimeout);
                }
            } catch (Exception e) {
                exceptionThrown(e);
                // TODO add log
            }
        }
    }

    /**
     * Called when the class is unloaded. Attempt to do a clean exit.
     * 
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        thread.end();
        thread = null;
        logger.info("Observable Plugin thread ended.", "name", this.getName());
        super.finalize();
    }
}
