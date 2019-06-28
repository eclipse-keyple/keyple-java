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

import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
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
    SortedSet<String> groupReferences;

    static public String PREFIX_NAME = "POOL_";

    public StubPoolPlugin(SortedSet<String> groupReferences) {
        this.stubPlugin = StubPlugin.getInstance();
        this.groupReferences = groupReferences;
    }

    @Override
    public String getName() {
        return PREFIX_NAME + stubPlugin.getName();
    }

    @Override
    public SortedSet<String> getReaderGroupReferences() {
        return groupReferences;
    }

    /**
     * Allocate a reader (in stub, it creates a new reader)
     * 
     * @param groupReference the reference of the group to which the reader belongs (may be null
     *        depending on the implementation made)
     * @return
     * @throws KeypleReaderException
     */
    @Override
    public SeReader allocateReader(String groupReference) {
        /*
         * Plug a new reader in stubPlugin with name groupeReference+timestamp
         */
        String readerName = groupReference + new Date().getTime();
        stubPlugin.plugStubReader(readerName, true);

        try {
            return stubPlugin.getReader(readerName);
        } catch (KeypleReaderNotFoundException e) {
            throw new IllegalStateException(
                    "Impossible to allocateReader, stubplugin failed to create a reader");
        }
    }

    /**
     * Deallocate a reader (in stub, it creates a new reader)
     * 
     * @param seReader the SeReader to be released.
     * @throws KeypleReaderException
     */
    @Override
    public void releaseReader(SeReader seReader) {
        try {
            stubPlugin.unplugStubReader(seReader.getName(), true);
        } catch (KeypleReaderException e) {
            throw new IllegalStateException(
                    "Impossible to release reader, reader with name was not found in stubplugin : "
                            + seReader.getName());
        }
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
