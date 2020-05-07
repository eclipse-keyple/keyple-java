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
import org.eclipse.keyple.core.seproxy.exception.*;

/**
 * Simulates a @{@link ReaderPoolPlugin} with {@link StubReaderImpl} and {@link StubSecureElement}
 * Manages allocation readers by group reference, Limitations : - each group can contain only one
 * StubReader thus one StubSecureElement This class uses internally @{@link StubPluginImpl} which is
 * a singleton.
 */
final class StubPoolPluginImpl implements StubPoolPlugin {

    StubPluginImpl stubPlugin;
    Map<String, StubReaderImpl> readerPool; // groupReference, seReader = limitation each
                                            // groupReference
    // can have only one reader
    Map<String, String> allocatedReader;// readerName,groupReference


    public StubPoolPluginImpl(String pluginName) {
        // create an embedded stubplugin to manage reader
        this.stubPlugin = (StubPluginImpl) new StubPluginFactory(pluginName).getPluginInstance();
        this.readerPool = new HashMap<String, StubReaderImpl>();
        this.allocatedReader = new HashMap<String, String>();

    }

    @Override
    public String getName() {
        return stubPlugin.getName();
    }

    @Override
    public SortedSet<String> getReaderGroupReferences() {
        return new TreeSet<String>(readerPool.keySet());
    }

    @Override
    public SeReader plugStubPoolReader(String groupReference, String readerName,
            StubSecureElement se) {
        try {
            // create new reader
            stubPlugin.plugStubReader(readerName, true);

            // get new reader
            StubReaderImpl newReader = (StubReaderImpl) stubPlugin.getReader(readerName);

            newReader.insertSe(se);

            // map reader to groupReference
            readerPool.put(groupReference, newReader);

            return newReader;
        } catch (KeypleReaderNotFoundException e) {
            throw new IllegalStateException(
                    "Impossible to allocateReader, stubplugin failed to create a reader");
        }
    }

    @Override
    public void unplugStubPoolReader(String groupReference) {
        try {
            // get reader
            SeReader stubReader = readerPool.get(groupReference);

            // remove reader from pool
            readerPool.remove(groupReference);

            // remove reader from plugin
            stubPlugin.unplugStubReader(stubReader.getName(), true);

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
    public SeReader allocateReader(String groupReference) throws KeypleAllocationReaderException, KeypleAllocationNoReaderException {


        // find the reader in the readerPool
        StubReaderImpl seReader = readerPool.get(groupReference);

        // check if reader is found
        if (seReader == null) {
            throw new KeypleAllocationReaderException(
                    "Impossible to allocate a reader for groupReference : " + groupReference
                            + ". Has the reader being plugged to this referenceGroup?");
        }
        // check if reader is available
        if(allocatedReader.containsKey(seReader.getName())){
            throw new KeypleAllocationNoReaderException(
                    "Impossible to allocate a reader for groupReference : " + groupReference
                            + ". No reader Available");
        }

        //allocate reader
        allocatedReader.put(seReader.getName(), groupReference);
        return seReader;

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
        if (!(seReader instanceof StubReaderImpl)) {
            throw new IllegalArgumentException(
                    "Can not release seReader, SeReader should be of type StubReader");
        }

        /**
         * Remove and Re-insert SE to reset logical channel
         */
        StubReaderImpl stubReader = ((StubReaderImpl) seReader);
        if (stubReader.checkSePresence()) {
            StubSecureElement se = stubReader.getSe();
            stubReader.removeSe();
            stubReader.insertSe(se);
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
        return stubPlugin.getReaderNames();
    }

    @Override
    public SortedSet<SeReader> getReaders() {
        return stubPlugin.getReaders();
    }

    @Override
    public SeReader getReader(String name) throws KeypleReaderNotFoundException {
        return stubPlugin.getReader(name);
    }

    @Override
    public int compareTo(ReaderPlugin o) {
        return stubPlugin.compareTo(o);
    }

    @Override
    public Map<String, String> getParameters() {
        return stubPlugin.getParameters();
    }

    @Override
    public void setParameter(String key, String value)
            throws IllegalArgumentException, KeypleBaseException {
        stubPlugin.setParameter(key, value);
    }

    @Override
    public void setParameters(Map<String, String> parameters)
            throws IllegalArgumentException, KeypleBaseException {
        stubPlugin.setParameters(parameters);
    }

}
