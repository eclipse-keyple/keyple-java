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
 * Wraps a stubplugin to add ReaderPoolPlugin methods
 */
public class StubPoolPlugin implements ReaderPoolPlugin {

    StubPlugin stubPlugin;
    Map<String,SeReader> readerPool; //groupReference, seReader = limitation each groupReference can have only one reader
    Map<String, String> allocatedReader;//readerName,groupReference

    static public String PREFIX_NAME = "POOL_";

    public StubPoolPlugin() {
        this.stubPlugin = StubPlugin.getInstance();
        this.readerPool = new HashMap<String, SeReader>();
        this.allocatedReader = new HashMap<String, String>();

    }

    @Override
    public String getName() {
        return PREFIX_NAME + stubPlugin.getName();
    }

    @Override
    public SortedSet<String> getReaderGroupReferences() {
        return new TreeSet<String>(readerPool.keySet());
    }

    /**
     * Plug a new reader in Pool with groupReference
     */
    public SeReader plugStubPoolReader(String groupReference, String readerName, StubSecureElement se){
        try {
            //create new reader
            stubPlugin.plugStubReader(readerName, true);

            //get new reader
            StubReader newReader = (StubReader) stubPlugin.getReader(readerName);

            newReader.insertSe(se);

            //map reader to groupReference
            readerPool.put(groupReference, newReader);

            return newReader;
        } catch (KeypleReaderNotFoundException e) {
            throw new IllegalStateException(
                    "Impossible to allocateReader, stubplugin failed to create a reader");
        }
    }

    /**
     * Unplug a new reader by groupReference
     * @param groupReference
     */
    public void unplugStubPoolReader(String groupReference){
        try {
            //get reader
            SeReader stubReader = readerPool.get(groupReference);

            //remove reader from pool
            readerPool.remove(groupReference);

            //remove reader from plugin
            stubPlugin.unplugStubReader(stubReader.getName(),true);

        } catch (KeypleReaderException e) {
            throw new IllegalStateException(
                    "Impossible to release reader, reader with groupReference was not found in stubplugin : "
                            + groupReference);
        }
    }






    /**
     * Allocate a reader
     * 
     * @param groupReference the reference of the group to which the reader belongs (may be null
     *        depending on the implementation made)
     * @return
     * @throws KeypleReaderException
     */
    @Override
    public SeReader allocateReader(String groupReference) {
        SeReader seReader = readerPool.get(groupReference);
        allocatedReader.put(seReader.getName(), groupReference);
        return seReader;
    }

    /**
     * Release a reader
     * 
     * @param seReader the SeReader to be released.
     * @throws KeypleReaderException
     */
    @Override
    public void releaseReader(SeReader seReader) {
        allocatedReader.remove(seReader.getName());
    }

    public Map<String,String > listAllocatedReaders(){
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
    public SortedSet<? extends SeReader> getReaders() throws KeypleReaderException {
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
