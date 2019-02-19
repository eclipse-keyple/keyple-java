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
package org.eclipse.keyple.plugin.stub;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.seproxy.plugin.AbstractThreadedObservablePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StubPlugin extends AbstractThreadedObservablePlugin {

    private static final StubPlugin uniqueInstance = new StubPlugin();

    private static final Logger logger = LoggerFactory.getLogger(StubPlugin.class);

    private final Map<String, String> parameters = new HashMap<String, String>();

    private static SortedSet<String> nativeStubReadersNames = new ConcurrentSkipListSet<String>();

    private StubPlugin() {
        super("StubPlugin");

        /*
         * Monitoring is not handled by a lower layer (as in PC/SC), reduce the threading period to
         * 50 ms to speed up responsiveness.
         */
        threadWaitTimeout = 50;
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
    protected SortedSet<AbstractObservableReader> getNativeReaders() throws KeypleReaderException {
        /* init Stub Readers list */
        SortedSet<AbstractObservableReader> nativeReaders =
                new ConcurrentSkipListSet<AbstractObservableReader>();

        /*
         * parse the current readers list to create the ProxyReader(s) associated with new reader(s)
         */
        if (nativeStubReadersNames != null && nativeStubReadersNames.size() > 0) {
            for (String name : nativeStubReadersNames) {
                nativeReaders.add(new StubReader(name));
            }
        }
        return nativeReaders;
    }

    @Override
    protected AbstractObservableReader getNativeReader(String name) {
        for (AbstractObservableReader reader : readers) {
            if (reader.getName().equals(name)) {
                return reader;
            }
        }
        AbstractObservableReader reader = null;
        if (nativeStubReadersNames.contains(name)) {
            reader = new StubReader(name);
        }
        return reader;
    }

    /**
     * Plug a Stub Reader
     * 
     * @param name : name of the reader
     */
    public void plugStubReader(String name) {
        if (!nativeStubReadersNames.contains(name)) {
            logger.info("Plugging a new reader with name " + name);
            nativeStubReadersNames.add(name);
            // StubReader stubReader = new StubReader(name);
            // readers.add((AbstractObservableReader) stubReader);
        } else {
            logger.error("Reader with name " + name + " was already plugged");
        }
    }

    /**
     * Unplug a Stub Reader
     * 
     * @param name the name of the reader
     * @throws KeypleReaderException in case of a reader exception
     */
    public void unplugReader(String name) throws KeypleReaderException {

        if (!nativeStubReadersNames.contains(name)) {
            logger.warn("No reader found with name " + name);
        } else {
            nativeStubReadersNames.remove(name);
            logger.info("Unplugged reader with name " + name);
        }
    }

    /**
     * Get a list of available reader names
     * 
     * @return String list
     */
    @Override
    protected SortedSet<String> getNativeReadersNames() {
        if (nativeStubReadersNames.isEmpty()) {
            logger.trace("No reader available.");
        }
        return nativeStubReadersNames;
    }
}
