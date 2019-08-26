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

import java.util.Set;
import org.eclipse.keyple.core.seproxy.PluginFactory;
import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

/**
 * The purpose of {@link StubPluginFactory} singleton class is to manage the creation of the
 * StubPlugin unique instance and to provide the corresponding ReaderPlugin instance.
 * <p>
 * Thus, the internal methods of StubPlugin are hidden from the point of view of the calling
 * application.
 */
public final class StubPluginFactory implements PluginFactory {

    /**
     * singleton instance of {@link StubPluginFactory}
     */
    private static volatile StubPluginFactory uniqueInstance = new StubPluginFactory();

    /**
     * unique instance of StubPlugin
     */
    private static StubPlugin stubPluginUniqueInstance = null;

    /**
     * Private constructor
     */
    private StubPluginFactory() {}

    /**
     * Gets the single instance of {@link StubPluginFactory}.
     * <p>
     * Creates the {@link StubPlugin} unique instance if not already created.
     *
     * @return single instance of {@link StubPluginFactory}
     */
    public static StubPluginFactory getInstance() {
        if (stubPluginUniqueInstance == null) {
            stubPluginUniqueInstance = new StubPlugin();
        }
        return uniqueInstance;
    }


    /**
     * Get the StubPlugin instance casted to ReaderPlugin
     *
     * @return the ReaderPlugin
     */
    public ReaderPlugin getPluginInstance() {
        return (ReaderPlugin) stubPluginUniqueInstance;
    }

    /**
     * Plug a Stub Reader (package entry)
     *
     * @param name : name of the created reader
     * @param synchronous : should the stubreader added synchronously (without waiting for the
     *        observation thread). An READER_CONNECTED event is raised in both cases
     */
    public void plugStubReader(String name, Boolean synchronous) {
        stubPluginUniqueInstance.plugStubReader(name, synchronous);
    }

    /**
     * Plug a Stub Reader (package entry)
     *
     * @param name : name of the created reader
     * @param transmissionMode : transmissionMode of the created reader
     * @param synchronous : should the stubreader added synchronously (without waiting for the
     *        observation thread). An READER_CONNECTED event is raised in both cases
     */
    public void plugStubReader(String name, TransmissionMode transmissionMode,
            Boolean synchronous) {
        stubPluginUniqueInstance.plugStubReader(name, transmissionMode, synchronous);
    }

    /**
     * Plug a list of stub Reader at once (package entry)
     *
     * @param names : names of readers to be connected
     * @param synchronous : should the stubreader be added synchronously (without waiting for the
     *        observation thread). An READER_CONNECTED event is raised in both cases
     */
    public void plugStubReaders(Set<String> names, Boolean synchronous) {
        stubPluginUniqueInstance.plugStubReaders(names, synchronous);
    }

    /**
     * Unplug a Stub Reader (package entry)
     *
     * @param name the name of the reader
     * @param synchronous : should the stubreader be removed synchronously (without waiting for the
     *        observation thread). An READER_DISCONNECTED event is raised in both cases
     * @throws KeypleReaderException in case of a reader exception
     */
    public void unplugStubReader(String name, Boolean synchronous) throws KeypleReaderException {
        stubPluginUniqueInstance.unplugStubReader(name, synchronous);
    }

    /**
     * Unplug a list of readers (package entry)
     *
     * @param names : names of the reader to be unplugged
     * @param synchronous : should the stubreader removed synchronously (without waiting for the
     *        observation thread). An READER_DISCONNECTED event is raised in both cases
     */
    public void unplugStubReaders(Set<String> names, Boolean synchronous) {
        stubPluginUniqueInstance.unplugStubReaders(names, synchronous);
    }

    /**
     * Insert a stub se into the reader. Will raise a SE_INSERTED event (package entry).
     *
     * @param _se stub secure element to be inserted in the reader
     */
    public synchronized void insertSe(String readerName, StubSecureElement _se) {
        StubReader stubReader = null;
        try {
            stubReader = (StubReader) stubPluginUniqueInstance.getReader(readerName);
        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        }
        stubReader.insertSe(_se);
    }

    /**
     * Remove se from reader if any (package entry)
     */
    public synchronized void removeSe(String readerName) {
        StubReader stubReader = null;
        try {
            stubReader = (StubReader) stubPluginUniqueInstance.getReader(readerName);
        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        }
        stubReader.removeSe();
    }

    /**
     * Get inserted SE (package entry)
     *
     * @return se, can be null if no Se inserted
     */
    public StubSecureElement getSe(String readerName) {
        StubReader stubReader = null;
        try {
            stubReader = (StubReader) stubPluginUniqueInstance.getReader(readerName);
        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        }
        return stubReader.getSe();
    }

    /**
     * Check the presence of a SE
     * 
     * @param readerName
     * @return true or false
     */
    public boolean checkSePresence(String readerName) {
        StubReader stubReader = null;
        try {
            stubReader = (StubReader) stubPluginUniqueInstance.getReader(readerName);
        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        }
        return stubReader.checkSePresence();
    }
}
