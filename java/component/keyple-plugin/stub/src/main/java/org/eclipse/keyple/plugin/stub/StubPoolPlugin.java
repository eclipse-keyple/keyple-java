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
package org.eclipse.keyple.plugin.stub;

import java.util.*;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;

/**
 * Simulates a @{@link ReaderPoolPlugin} with {@link StubReader} and {@link StubSecureElement}
 * Manages allocation readers by group reference, Limitations : - each group can contain only one
 * StubReader thus one StubSecureElement This class uses internally @{@link StubPlugin} which is a
 * singleton.
 */
public class StubPoolPlugin implements ReaderPoolPlugin {

    StubPluginFactory stubPluginFactory;
    Map<String, SeReader> readerPool; // groupReference, seReader = limitation each groupReference
                                      // can have only one reader
    Map<String, String> allocatedReader;// readerName,groupReference

    static public String PREFIX_NAME = "POOL_";

    public StubPoolPlugin() {
        this.stubPluginFactory = StubPluginFactory.getInstance();
        this.readerPool = new HashMap<String, SeReader>();
        this.allocatedReader = new HashMap<String, String>();
    }

    @Override
    public String getName() {
        return PREFIX_NAME + stubPluginFactory.getPluginInstance().getName();
    }

    @Override
    public SortedSet<String> getReaderGroupReferences() {
        return new TreeSet<String>(readerPool.keySet());
    }

    /**
     * Plug synchronously a new @{@link StubReader} in Pool with groupReference and a StubSE. A
     * READER_CONNECTED event will be raised.
     * 
     * @param groupReference : group refence of the new stub reader
     * @param readerName : name of the new stub reader
     * @param se : insert a se at creation (can be null)
     * @return created StubReader
     */
    public SeReader plugStubPoolReader(String groupReference, String readerName,
            StubSecureElement se) {
        try {
            // create new reader
            stubPluginFactory.plugStubReader(readerName, true);

            // get new reader
            SeReader newReader = stubPluginFactory.getPluginInstance().getReader(readerName);

            stubPluginFactory.insertSe(newReader.getName(), se);

            // map reader to groupReference
            readerPool.put(groupReference, newReader);

            return newReader;
        } catch (KeypleReaderNotFoundException e) {
            throw new IllegalStateException(
                    "Impossible to allocateReader, stubplugin failed to create a reader");
        }
    }

    /**
     * Unplug synchronously a new reader by groupReference. A READER_DISCONNECTED event will be
     * raised.
     * 
     * @param groupReference groupReference of the reader to be unplugged
     */
    public void unplugStubPoolReader(String groupReference) {
        try {
            // get reader
            SeReader stubReader = readerPool.get(groupReference);

            // remove reader from pool
            readerPool.remove(groupReference);

            // remove reader from plugin
            stubPluginFactory.unplugStubReader(stubReader.getName(), true);

        } catch (KeypleReaderException e) {
            throw new IllegalStateException(
                    "Impossible to release reader, reader with groupReference was not found in stubplugin : "
                            + groupReference);
        }
    }



    /**
     * Allocate a reader if available by groupReference
     * 
     * @param groupReference the reference of the group to which the reader belongs (may be null
     *        depending on the implementation made)
     * @return seReader if available, null otherwise
     */
    @Override
    public SeReader allocateReader(String groupReference) {
        // find the reader in the readerPool
        SeReader seReader = readerPool.get(groupReference);

        // check if the reader is available
        if (seReader == null || allocatedReader.containsKey(seReader.getName())) {
            return null;
        } else {
            allocatedReader.put(seReader.getName(), groupReference);
            return seReader;
        }

    }

    /**
     * Release a reader
     * 
     * @param seReader the SeReader to be released.
     */
    @Override
    public void releaseReader(SeReader seReader) {
        if (seReader == null) {
            throw new IllegalArgumentException("Could not release seReader, seReader is null");
        }
        if (!(seReader instanceof StubReader)) {
            throw new IllegalArgumentException(
                    "Can not release seReader, SeReader should be of type StubReader");
        }

        /**
         * Remove and Re-insert SE to reset logical channel
         */
        StubPluginFactory stubPluginFactory = StubPluginFactory.getInstance();
        if (stubPluginFactory.checkSePresence(seReader.getName())) {
            StubSecureElement se = stubPluginFactory.getSe(seReader.getName());
            stubPluginFactory.removeSe(seReader.getName());
            stubPluginFactory.insertSe(seReader.getName(), se);
        }

        allocatedReader.remove(seReader.getName());

    }

    public Map<String, String> listAllocatedReaders() {
        return allocatedReader;
    }


    /*
     * Delegate methods to embedded stub plugin
     */

    @Override
    public SortedSet<String> getReaderNames() {
        return stubPluginFactory.getPluginInstance().getReaderNames();
    }

    @Override
    public SortedSet<SeReader> getReaders() throws KeypleReaderException {
        return stubPluginFactory.getPluginInstance().getReaders();
    }

    @Override
    public SeReader getReader(String name) throws KeypleReaderNotFoundException {
        return stubPluginFactory.getPluginInstance().getReader(name);
    }

    @Override
    public int compareTo(ReaderPlugin o) {
        return stubPluginFactory.getPluginInstance().compareTo(o);
    }

    @Override
    public Map<String, String> getParameters() {
        return stubPluginFactory.getPluginInstance().getParameters();
    }

    @Override
    public void setParameter(String key, String value)
            throws IllegalArgumentException, KeypleBaseException {
        stubPluginFactory.getPluginInstance().setParameter(key, value);
    }

    @Override
    public void setParameters(Map<String, String> parameters)
            throws IllegalArgumentException, KeypleBaseException {
        stubPluginFactory.getPluginInstance().setParameters(parameters);
    }

}
