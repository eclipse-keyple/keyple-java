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

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.seproxy.plugin.AbstractThreadedObservablePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StubPlugin extends AbstractThreadedObservablePlugin {

    private static final StubPlugin uniqueInstance = new StubPlugin();

    private static final Logger logger = LoggerFactory.getLogger(StubPlugin.class);

    private final Map<String, String> parameters = new HashMap<String, String>();

    // simulated list of real-time connected stubReader
    private static SortedSet<String> connectedStubNames = new ConcurrentSkipListSet<String>();

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


    /**
     * Plug a Stub Reader
     * 
     * @param name : name of the created reader
     * @param synchronous : should the stubreader added synchronously (without waiting for the
     *        observation thread)
     */
    public void plugStubReader(String name, Boolean synchronous) {

        logger.info("Plugging a new reader with name " + name);
        /* add the native reader to the native readers list */
        Boolean exist = connectedStubNames.contains(name);

        if (!exist && synchronous) {
            /* add the reader as a new reader to the readers list */
            readers.add(new StubReader(name));
        }

        connectedStubNames.add(name);

        if (exist) {
            logger.error("Reader with name " + name + " was already plugged");
        }

    }

    /**
     * Plug a list of stub Reader at once
     *
     * @param names : names of readers to be connected
     */
    public void plugStubReaders(Set<String> names, Boolean synchronous) {
        logger.debug("Plugging {} readers ..", names.size());

        /* plug stub readers that were not plugged already */
        // duplicate names
        Set<String> newNames = new HashSet<String>(names);
        // remove already connected stubNames
        newNames.removeAll(connectedStubNames);

        logger.debug("New readers to be created #{}", newNames.size());


        /*
         * Add new names to the connectedStubNames
         */

        if (newNames.size() > 0) {
            if (synchronous) {
                List<StubReader> newReaders = new ArrayList<StubReader>();
                for (String name : newNames) {
                    newReaders.add(new StubReader(name));
                }
                readers.addAll(newReaders);
            }

            connectedStubNames.addAll(names);

        } else {
            logger.error("All {} readers were already plugged", names.size());

        }


    }


    /**
     * Unplug a Stub Reader
     * 
     * @param name the name of the reader
     * @throws KeypleReaderException in case of a reader exception
     */
    public void unplugStubReader(String name, Boolean synchronous)
            throws KeypleReaderException, InterruptedException {

        if (!connectedStubNames.contains(name)) {
            logger.warn("unplugStubReader() No reader found with name {}", name);
        } else {
            /* remove the reader from the readers list */
            if (synchronous) {
                connectedStubNames.remove(name);
                readers.remove(getReader(name));
            } else {
                connectedStubNames.remove(name);
            }
            /* remove the native reader from the native readers list */
            logger.info("Unplugged reader with name {}, connectedStubNames size {}", name,
                    connectedStubNames.size());
        }
    }


    public void unplugStubReaders(Set<String> names, Boolean synchronous) {
        logger.info("Unplug {} stub readers", names.size());
        logger.debug("Unplug stub readers.. {}", names);
        List<StubReader> readersToDelete = new ArrayList<StubReader>();
        for (String name : names) {
            try {
                readersToDelete.add((StubReader) getReader(name));
            } catch (KeypleReaderNotFoundException e) {
                logger.warn("unplugStubReaders() No reader found with name {}", name);
            }
        }
        connectedStubNames.removeAll(names);
        if (synchronous) {
            readers.removeAll(readersToDelete);
        }
    }


    /**
     * Fetch the list of connected native reader (from a simulated list) and returns their names (or
     * id)
     *
     * @return connected readers' name list
     */
    @Override
    protected SortedSet<String> fetchNativeReadersNames() {
        if (connectedStubNames.isEmpty()) {
            logger.trace("No reader available.");
        }
        return connectedStubNames;
    }

    /**
     * Init native Readers to empty Set
     * 
     * @return the list of AbstractObservableReader objects.
     * @throws KeypleReaderException if a reader error occurs
     */
    @Override
    protected SortedSet<AbstractObservableReader> initNativeReaders() throws KeypleReaderException {
        /* init Stub Readers response object */
        SortedSet<AbstractObservableReader> newNativeReaders =
                new ConcurrentSkipListSet<AbstractObservableReader>();

        /*
         * parse the current readers list to create the ProxyReader(s) associated with new reader(s)
         * if (connectedStubNames != null && connectedStubNames.size() > 0) { for (String name :
         * connectedStubNames) { newNativeReaders.add(new StubReader(name)); } }
         */
        return newNativeReaders;
    }

    /**
     * Fetch the reader whose name is provided as an argument. Returns the current reader if it is
     * already listed. Creates and returns a new reader if not.
     *
     * Throws an exception if the wanted reader is not found.
     *
     * @param name name of the reader
     * @return the reader object
     */
    @Override
    protected AbstractObservableReader fetchNativeReader(String name) {
        for (AbstractObservableReader reader : readers) {
            if (reader.getName().equals(name)) {
                return reader;
            }
        }
        AbstractObservableReader reader = null;
        if (connectedStubNames.contains(name)) {
            reader = new StubReader(name);
        }
        return reader;
    }
}
