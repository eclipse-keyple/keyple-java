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
package org.eclipse.keyple.plugin.remotese.pluginse;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservablePlugin;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTxEngine;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Remote SE Plugin Creates a virtual reader when a remote readers connect Manages the dispatch of
 * events received from remote readers
 */
public final class RemoteSePlugin extends AbstractObservablePlugin {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSePlugin.class);
    public static final String PLUGIN_NAME = "RemoteSePlugin";

    // private final VirtualReaderSessionFactory sessionManager;

    private final VirtualReaderSessionFactory sessionManager;
    private final DtoSender sender;
    private final Map<String, String> parameters;

    /**
     * Only {@link MasterAPI} can instanciate a RemoteSePlugin
     */
    RemoteSePlugin(VirtualReaderSessionFactory sessionManager, DtoSender sender) {
        super(PLUGIN_NAME);
        this.sessionManager = sessionManager;
        logger.info("Init RemoteSePlugin");
        this.sender = sender;
        this.parameters = new HashMap<String, String>();
    }

    /**
     * Retrieve a reader by its native reader name and slave Node Id
     *
     * @param remoteName : name of the reader on its native device
     * @param slaveNodeId : slave node Id of the reader to disconnect
     * @return corresponding Virtual reader if exists
     * @throws KeypleReaderNotFoundException if no virtual reader match the native reader name
     */
    public VirtualReader getReaderByRemoteName(String remoteName, String slaveNodeId)
            throws KeypleReaderNotFoundException {
        for (AbstractObservableReader virtualReader : readers) {
            if (((VirtualReader) virtualReader).getName()
                    .equals(RemoteSePlugin.generateReaderName(remoteName, slaveNodeId))) {
                return (VirtualReader) virtualReader;
            }
        }
        throw new KeypleReaderNotFoundException(remoteName);
    }

    /**
     * Create a virtual reader (internal method)
     */
    ProxyReader createVirtualReader(String slaveNodeId, String nativeReaderName,
            DtoSender dtoSender, TransmissionMode transmissionMode) throws KeypleReaderException {
        logger.debug("createVirtualReader for slaveNodeId {} and reader {}", slaveNodeId,
                nativeReaderName);

        // create a new session for the new reader
        VirtualReaderSession session =
                sessionManager.createSession(nativeReaderName, slaveNodeId, dtoSender.getNodeId());

        try {
            if (getReaderByRemoteName(nativeReaderName, slaveNodeId) != null) {
                throw new KeypleReaderException(
                        "Virtual Reader already exists for reader " + nativeReaderName);
            }
        } catch (KeypleReaderNotFoundException e) {
            // no reader found, continue
        }


        // check if reader is not already connected (by localReaderName)
        logger.info("Create a new Virtual Reader with localReaderName {} with session {}",
                nativeReaderName, session.getSessionId());

        // Create virtual reader with a remote method engine so the reader can send dto
        // with a session
        // and the provided name
        final VirtualReader virtualReader = new VirtualReader(session, nativeReaderName,
                new RemoteMethodTxEngine(sender), transmissionMode);
        readers.add(virtualReader);

        // notify that a new reader is connected in a separated thread
        /*
         * new Thread() { public void run() { } }.start();
         */
        notifyObservers(new PluginEvent(getName(), virtualReader.getName(),
                PluginEvent.EventType.READER_CONNECTED));

        return virtualReader;

    }

    /**
     * Delete a virtual reader (internal method)
     * 
     * @param nativeReaderName name of the virtual reader to be deleted
     */
    void disconnectRemoteReader(String nativeReaderName, String slaveNodeId)
            throws KeypleReaderNotFoundException {

        // logger.debug("Disconnect Virtual reader {}", nativeReaderName);

        // retrieve virtual reader to delete
        final VirtualReader virtualReader =
                this.getReaderByRemoteName(nativeReaderName, slaveNodeId);

        logger.info("Disconnect VirtualReader with name {} with slaveNodeId {}", nativeReaderName,
                slaveNodeId);

        // remove observers of reader
        virtualReader.clearObservers();

        // remove reader
        readers.remove(virtualReader);

        // send event READER_DISCONNECTED in a separate thread
        // new Thread() {public void run() { }}.start();

        notifyObservers(new PluginEvent(getName(), virtualReader.getName(),
                PluginEvent.EventType.READER_DISCONNECTED));

    }

    /**
     * Propagate a received event from slave device (internal method)
     * 
     * @param event : Reader Event to be propagated
     */

    void onReaderEvent(ReaderEvent event) throws KeypleReaderNotFoundException {
        logger.debug("Dispatch ReaderEvent to the appropriate Reader : {}", event.getReaderName());

        VirtualReader virtualReader = (VirtualReader) getReader(event.getReaderName());
        virtualReader.onRemoteReaderEvent(event);

    }


    /**
     * Init Native Readers to empty Set
     */
    @Override
    protected SortedSet<AbstractObservableReader> initNativeReaders() {
        return new TreeSet<AbstractObservableReader>();
    }

    /**
     * Not used
     */
    @Override
    protected AbstractObservableReader fetchNativeReader(String name) {
        // should not be call
        throw new IllegalArgumentException(
                "fetchNativeReader is not used in this plugin, did you meant to use getReader?");
    }

    @Override
    protected void startObservation() {

    }

    @Override
    protected void stopObservation() {

    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public void setParameter(String key, String value) throws IllegalArgumentException {
        parameters.put(key, value);
    }

    /**
     * Generate the name for the virtual reader based on the NativeReader name and its node Id.
     * Bijective function
     *
     * @param nativeReaderName
     * @param slaveNodeId
     * @return
     */
    static String generateReaderName(String nativeReaderName, String slaveNodeId) {
        return "remote-" + nativeReaderName + "-" + slaveNodeId;
    }


}
