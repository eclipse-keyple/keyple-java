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

import java.util.*;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservablePlugin;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTxEngine;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Remote SE Plugin Creates a virtual reader when a remote readers connect Manages the dispatch of
 * events received from remote readers
 */
class RemoteSePluginImpl extends AbstractObservablePlugin implements RemoteSePlugin {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSePluginImpl.class);

    // in milliseconds, throw an exception if slave hasn't answer during this time
    public final long rpcTimeout;

    private final VirtualReaderSessionFactory sessionManager;
    protected final DtoSender dtoSender;
    private final Map<String, String> parameters;

    /**
     * RemoteSePlugin is wrapped into MasterAPI and instantiated like a standard plugin
     * by @SeProxyService. Use MasterAPI
     */
    RemoteSePluginImpl(VirtualReaderSessionFactory sessionManager, DtoSender dtoSender,
            long rpcTimeout, String pluginName) {
        super(pluginName);
        this.sessionManager = sessionManager;
        logger.info("Init RemoteSePlugin");
        this.dtoSender = dtoSender;
        this.parameters = new HashMap<String, String>();
        this.rpcTimeout = rpcTimeout;
    }


    public VirtualReaderImpl getReaderByRemoteName(String remoteName, String slaveNodeId)
            throws KeypleReaderNotFoundException {
        for (SeReader virtualReader : readers) {
            if (((VirtualReaderImpl) virtualReader).getName()
                    .equals(RemoteSePluginImpl.generateReaderName(remoteName, slaveNodeId))) {
                return (VirtualReaderImpl) virtualReader;
            }
        }
        throw new KeypleReaderNotFoundException(remoteName);
    }


    @Override
    public void disconnectVirtualReader(String nativeReaderName, String slaveNodeId)
            throws KeypleReaderException {
        removeVirtualReader(nativeReaderName, slaveNodeId);
    }


    /**
     * Create a virtual reader (internal method)
     */
    ProxyReader createVirtualReader(String slaveNodeId, String nativeReaderName,
            DtoSender dtoSender, TransmissionMode transmissionMode, Boolean isObservable,
            Map<String, String> options) throws KeypleReaderException {

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
        logger.trace(
                "Create a new Virtual Reader with localReaderName {} with session {} isObservable {}  for slaveNodeId {}",
                nativeReaderName, session.getSessionId(), isObservable, slaveNodeId);

        /*
         * Create virtual reader with a remote method engine so the reader can send dto with a
         * session and the provided name Virtual Reader can be Observable or not.
         */
        VirtualReaderImpl virtualReader;
        if (Boolean.TRUE.equals(isObservable)) {
            virtualReader = new VirtualObservableReaderImpl(session, nativeReaderName,
                    new RemoteMethodTxEngine(dtoSender, rpcTimeout), slaveNodeId, transmissionMode,
                    options);
        } else {
            virtualReader = new VirtualReaderImpl(session, nativeReaderName,
                    new RemoteMethodTxEngine(dtoSender, rpcTimeout), slaveNodeId, transmissionMode,
                    options);
        }
        readers.add(virtualReader);

        notifyObservers(new PluginEvent(getName(), virtualReader.getName(),
                PluginEvent.EventType.READER_CONNECTED));

        return virtualReader;

    }

    /**
     * Remove a virtual reader (internal method)
     *
     * @param nativeReaderName : name of the virtual reader to be deleted
     * @param slaveNodeId : slave node where the remoteReader is hosted
     * @throws KeypleReaderNotFoundException if no virtual reader match the native reader name and
     *         slave node Id
     */
    void removeVirtualReader(String nativeReaderName, String slaveNodeId)
            throws KeypleReaderNotFoundException {

        // retrieve virtual reader to delete
        final VirtualReaderImpl virtualReader =
                this.getReaderByRemoteName(nativeReaderName, slaveNodeId);

        logger.trace("Remove VirtualReader with name {} with slaveNodeId {}", nativeReaderName,
                slaveNodeId);

        // remove observers of reader
        if (virtualReader instanceof VirtualObservableReader) {
            ((VirtualObservableReader) virtualReader).clearObservers();
        }

        // remove reader
        readers.remove(virtualReader);

        notifyObservers(new PluginEvent(getName(), virtualReader.getName(),
                PluginEvent.EventType.READER_DISCONNECTED));
    }

    /**
     * Propagate a received event from slave device (internal method)
     *
     * @param event : Reader Event to be propagated
     */

    void onReaderEvent(ReaderEvent event) throws KeypleReaderNotFoundException {
        logger.trace("Dispatch ReaderEvent to the appropriate Reader : {}", event.getReaderName());

        VirtualReaderImpl virtualReader = (VirtualReaderImpl) getReader(event.getReaderName());
        virtualReader.onRemoteReaderEvent(event);

    }


    /**
     * Init Native Readers to empty Set
     */
    @Override
    protected SortedSet<SeReader> initNativeReaders() {
        return new TreeSet<SeReader>();
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
