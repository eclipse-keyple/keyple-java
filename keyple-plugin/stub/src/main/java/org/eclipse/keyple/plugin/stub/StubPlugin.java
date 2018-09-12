/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.seproxy.plugin.AbstractThreadedObservablePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StubPlugin extends AbstractThreadedObservablePlugin {

    private static final StubPlugin uniqueInstance = new StubPlugin();

    private static final Logger logger = LoggerFactory.getLogger(StubPlugin.class);

    private final Map<String, String> parameters = new HashMap<String, String>();

    private StubPlugin() {
        super("StubPlugin");
    }

    /**
     * Gets the single instance of StubPlugin.
     *
     * @return single instance of StubPlugin
     */
    public static StubPlugin getInstance() {
        return uniqueInstance;
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public void setParameter(String key, String value) {
        parameters.put(key, value);
    }

    @Override
    protected SortedSet<AbstractObservableReader> getNativeReaders() throws IOReaderException {
        // init Stub Readers list
        SortedSet<AbstractObservableReader> nativeReaders =
                new ConcurrentSkipListSet<AbstractObservableReader>();

        return nativeReaders;
    }

    @Override
    protected AbstractObservableReader getNativeReader(String name) {
        for (AbstractObservableReader reader : readers) {
            if (reader.getName().equals(name)) {
                return reader;
            }
        }
        return null;
    }


    /**
     * Plug a Stub Reader
     * 
     * @param name : name of the reader
     */
    public StubReader plugStubReader(String name) {

        if (getNativeReader(name) == null) {
            logger.info("Plugging a new reader with name " + name);
            StubReader stubReader = new StubReader(name);
            readers.add((AbstractObservableReader) stubReader);
            notifyObservers(
                    new PluginEvent(getName(), name, PluginEvent.EventType.READER_CONNECTED));
            return stubReader;

        } else {
            logger.warn("Reader with name " + name + " was already plugged");
            return (StubReader) getNativeReader(name);
        }

    }

    /**
     * Unplug a Stub Reader
     * 
     * @param name
     */
    public void unplugReader(String name) throws IOReaderException {
        ProxyReader reader = getNativeReader(name);
        if (reader == null) {
            logger.warn("No reader found with name " + name);

        } else {
            readers.remove(reader);
            notifyObservers(
                    new PluginEvent(getName(), name, PluginEvent.EventType.READER_DISCONNECTED));
            logger.info("Unplugged reader with name " + reader.getName());
        }
    }

    /**
     * Get a list of available reader names
     * 
     * @return String list
     */
    @Override
    protected SortedSet<String> getNativeReadersNames() {
        SortedSet<String> nativeReadersNames = new ConcurrentSkipListSet<String>();
        for (AbstractObservableReader reader : readers) {
            nativeReadersNames.add(reader.getName());
        }
        return nativeReadersNames;
    }
}
