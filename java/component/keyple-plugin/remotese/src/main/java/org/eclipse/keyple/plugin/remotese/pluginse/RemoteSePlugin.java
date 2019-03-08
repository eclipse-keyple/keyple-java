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
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTxEngine;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.plugin.AbstractObservablePlugin;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
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
     * Only {@link VirtualReaderService} can instanciate a RemoteSePlugin
     */
    RemoteSePlugin(VirtualReaderSessionFactory sessionManager, DtoSender sender) {
        super(PLUGIN_NAME);
        this.sessionManager = sessionManager;
        logger.info("Init RemoteSePlugin");
        this.sender = sender;
        this.parameters = new HashMap<String, String>();
    }

    /**
     * Retrieve a reader by its native reader name
     *
     * @param remoteName : name of the reader on its native device
     * @return corresponding Virtual reader if exists
     * @throws KeypleReaderNotFoundException if no virtual reader match the native reader name
     */
    public VirtualReader getReaderByRemoteName(String remoteName)
            throws KeypleReaderNotFoundException {
        for (AbstractObservableReader virtualReader : readers) {
            if (((VirtualReader) virtualReader).getNativeReaderName().equals(remoteName)) {
                return (VirtualReader) virtualReader;
            }
        }
        throw new KeypleReaderNotFoundException(remoteName);
    }

    /**
     * Create a virtual reader
     *
     */
    ProxyReader createVirtualReader(String clientNodeId, String nativeReaderName,
            DtoSender dtoSender) throws KeypleReaderException {
        logger.debug("createVirtualReader for nativeReader {}", nativeReaderName);

        // create a new session for the new reader
        VirtualReaderSession session = sessionManager.createSession(nativeReaderName, clientNodeId);

        try {
            if (getReaderByRemoteName(nativeReaderName) != null) {
                throw new KeypleReaderException(
                        "Virtual Reader already exists for reader " + nativeReaderName);
            } ;
        } catch (KeypleReaderNotFoundException e) {
            // no reader found, continue
        }


        // check if reader is not already connected (by localReaderName)
        logger.info("Create a new Virtual Reader with localReaderName {} with session {}",
                nativeReaderName, session.getSessionId());

        // Create virtual reader with a remote method engine so the reader can send dto
        // with a session
        // and the provided name
        final VirtualReader virtualReader =
                new VirtualReader(session, nativeReaderName, new RemoteMethodTxEngine(sender));
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
     * Delete a virtual reader
     * 
     * @param nativeReaderName name of the virtual reader to be deleted
     */
    void disconnectRemoteReader(String nativeReaderName) throws KeypleReaderNotFoundException {

        logger.debug("Disconnect Virtual reader {}", nativeReaderName);

        // retrieve virtual reader to delete
        final VirtualReader virtualReader =
                (VirtualReader) this.getReaderByRemoteName(nativeReaderName);

        logger.info("Disconnect VirtualReader with name {} with session {}", nativeReaderName);

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
     * Propagate a received event from slave device
     * 
     * @param event
     * @param sessionId : not used yet
     */
    void onReaderEvent(ReaderEvent event, String sessionId) {
        logger.debug("OnReaderEvent {}", event);
        logger.debug("Dispatch ReaderEvent to the appropriate Reader : {} sessionId : {}",
                event.getReaderName(), sessionId);
        try {
            VirtualReader virtualReader =
                    (VirtualReader) getReaderByRemoteName(event.getReaderName());
            virtualReader.onRemoteReaderEvent(event);

        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        }

    }


    /**
     * Init Native Readers to empty Set
     */
    @Override
    protected SortedSet<AbstractObservableReader> initNativeReaders() throws KeypleReaderException {
        return new TreeSet<AbstractObservableReader>();
    }

    /**
     * Not used
     */
    @Override
    protected AbstractObservableReader fetchNativeReader(String name) throws KeypleReaderException {
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

}
