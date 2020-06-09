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
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservablePlugin;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin allows to simulate Secure Element communication by creating @{@link StubReaderImpl}
 * and @{@link StubSecureElement}. Plug a new StubReader with StubPlugin#plugStubReader and insert
 * an implementation of your own of {@link StubSecureElement} to start simulation communication.
 * This class is a singleton, use StubPlugin#getInstance to access it
 *
 */
final class StubPluginImpl extends AbstractThreadedObservablePlugin implements StubPlugin {

    // private static final StubPlugin uniqueInstance = new StubPlugin();

    private static final Logger logger = LoggerFactory.getLogger(StubPluginImpl.class);

    private final Map<String, String> parameters = new HashMap<String, String>();

    // simulated list of real-time connected stubReader
    private SortedSet<String> connectedStubNames =
            Collections.synchronizedSortedSet(new ConcurrentSkipListSet<String>());


    /**
     * Constructor
     * 
     * @param pluginName : custom name for the plugin
     */
    StubPluginImpl(String pluginName) throws KeypleReaderException {
        super(pluginName);

        /*
         * Monitoring is not handled by a lower layer (as in PC/SC), reduce the threading period to
         * 10 ms to speed up responsiveness.
         */
        threadWaitTimeout = 10;
    }

    /**
     * Gets the single instance of StubPlugin.
     *
     * @return single instance of StubPlugin
     */
    /*
     * public static StubPlugin getInstance() { return uniqueInstance; }
     */

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public void setParameter(String key, String value) throws KeypleReaderIOException {
        parameters.put(key, value);
    }

    @Override
    public void plugStubReader(String readerName, Boolean synchronous) {
        plugStubReader(readerName, TransmissionMode.CONTACTLESS, synchronous);
    }

    @Override
    public void plugStubReader(String readerName, TransmissionMode transmissionMode,
            Boolean synchronous) {

        logger.info("Plugging a new reader with readerName " + readerName);
        /* add the native reader to the native readers list */
        Boolean exist = connectedStubNames.contains(readerName);

        if (!exist && synchronous) {
            /* add the reader as a new reader to the readers list */
            readers.add(new StubReaderImpl(this.getName(), readerName, transmissionMode));
        }

        connectedStubNames.add(readerName);

        if (exist) {
            logger.error("Reader with readerName " + readerName + " was already plugged");
        }

    }

    @Override
    public void plugStubReaders(Set<String> readerNames, Boolean synchronous) {
        logger.debug("Plugging {} readers ..", readerNames.size());

        /* plug stub readers that were not plugged already */
        // duplicate readerNames
        Set<String> newNames = new HashSet<String>(readerNames);
        // remove already connected stubNames
        newNames.removeAll(connectedStubNames);

        logger.debug("New readers to be created #{}", newNames.size());


        /*
         * Add new readerNames to the connectedStubNames
         */

        if (newNames.size() > 0) {
            if (synchronous) {
                List<StubReaderImpl> newReaders = new ArrayList<StubReaderImpl>();
                for (String name : newNames) {
                    newReaders.add(new StubReaderImpl(this.getName(), name));
                }
                readers.addAll(newReaders);
            }

            connectedStubNames.addAll(readerNames);

        } else {
            logger.error("All {} readers were already plugged", readerNames.size());

        }


    }

    @Override
    public void unplugStubReader(String readerName, Boolean synchronous)
            throws KeypleReaderException {

        if (!connectedStubNames.contains(readerName)) {
            logger.warn("unplugStubReader() No reader found with name {}", readerName);
        } else {
            /* remove the reader from the readers list */
            if (synchronous) {
                connectedStubNames.remove(readerName);
                readers.remove(getReader(readerName));
            } else {
                connectedStubNames.remove(readerName);
            }
            /* remove the native reader from the native readers list */
            logger.info("Unplugged reader with name {}, connectedStubNames size {}", readerName,
                    connectedStubNames.size());
        }
    }

    @Override
    public void unplugStubReaders(Set<String> readerNames, Boolean synchronous) {
        logger.info("Unplug {} stub readers", readerNames.size());
        logger.debug("Unplug stub readers.. {}", readerNames);
        List<StubReaderImpl> readersToDelete = new ArrayList<StubReaderImpl>();
        for (String name : readerNames) {
            try {
                readersToDelete.add((StubReaderImpl) getReader(name));
            } catch (KeypleReaderNotFoundException e) {
                logger.warn("unplugStubReaders() No reader found with name {}", name);
            }
        }
        connectedStubNames.removeAll(readerNames);
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
    public SortedSet<String> fetchNativeReadersNames() throws KeypleReaderIOException {
        if (connectedStubNames.isEmpty()) {
            logger.trace("No reader available.");
        }
        return connectedStubNames;
    }

    /**
     * Init native Readers to empty Set
     * 
     * @return the list of SeReader objects.
     * @throws KeypleReaderException if a reader error occurs
     */
    @Override
    protected SortedSet<SeReader> initNativeReaders() throws KeypleReaderIOException {
        /* init Stub Readers response object */
        SortedSet<SeReader> newNativeReaders = new ConcurrentSkipListSet<SeReader>();
        return newNativeReaders;
    }

    /**
     * Fetch the reader whose name is provided as an argument. Returns the current reader if it is
     * already listed. Creates and returns a new reader if not.
     *
     * Throws an exception if the wanted reader is not found.
     *
     * @param readerName name of the reader
     * @return the reader object
     */
    @Override
    protected SeReader fetchNativeReader(String readerName)
            throws KeypleReaderNotFoundException, KeypleReaderIOException {
        for (SeReader reader : readers) {
            if (reader.getName().equals(readerName)) {
                return reader;
            }
        }
        SeReader reader = null;
        if (connectedStubNames.contains(readerName)) {
            reader = new StubReaderImpl(this.getName(), readerName);
        }
        return reader;
    }
}
