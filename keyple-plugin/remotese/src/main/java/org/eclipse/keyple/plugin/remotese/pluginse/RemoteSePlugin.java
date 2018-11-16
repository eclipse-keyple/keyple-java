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

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.util.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Remote SE Plugin Creates a virtual reader when a remote readers connect Manages the dispatch of
 * events received from remote readers
 */
public class RemoteSePlugin extends Observable implements ObservablePlugin {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSePlugin.class);

    // private final VirtualReaderSessionFactory sessionManager;

    // virtual readers
    private final SortedSet<VirtualReader> virtualReaders = new TreeSet<VirtualReader>();

    private final VirtualReaderSessionFactory sessionManager;

    /**
     * Only {@link VirtualReaderService} can instanciate a RemoteSePlugin
     */
    RemoteSePlugin(VirtualReaderSessionFactory sessionManager) {
        this.sessionManager = sessionManager;
        logger.info("RemoteSePlugin");
    }

    @Override
    public String getName() {
        return "RemoteSePlugin";
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IllegalArgumentException {}

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {

    }

    @Override
    public SortedSet<? extends ProxyReader> getReaders() {
        return virtualReaders;
    }

    @Override
    public ProxyReader getReader(String name) throws KeypleReaderNotFoundException {
        for (VirtualReader VirtualReader : virtualReaders) {
            if (VirtualReader.getName().equals(name)) {
                return VirtualReader;
            }
        }
        throw new KeypleReaderNotFoundException("reader with name not found : " + name);
    }

    public ProxyReader getReaderByRemoteName(String remoteName)
            throws KeypleReaderNotFoundException {
        for (VirtualReader VirtualReader : virtualReaders) {
            if (VirtualReader.getNativeReaderName().equals(remoteName)) {
                return VirtualReader;
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

        // DtoSender sends Dto when the session requires to
        ((VirtualReaderSessionImpl) session).addObserver(dtoSender);

        // check if reader is not already connected (by localReaderName)
        if (!isReaderConnected(nativeReaderName)) {
            logger.info("Connecting a new RemoteSeReader with localReaderName {} with session {}",
                    nativeReaderName, session.getSessionId());

            final VirtualReader virtualReader = new VirtualReader(session, nativeReaderName);
            virtualReaders.add(virtualReader);

            // notify that a new reader is connected in a separated thread
            new Thread() {
                public void run() {
                    notifyObservers(new PluginEvent(getName(), virtualReader.getName(),
                            PluginEvent.EventType.READER_CONNECTED));
                }
            }.start();

            logger.info("*****************************");
            logger.info(" CONNECTED {} ", virtualReader.getName());
            logger.info("*****************************");
            return virtualReader;
        } else {
            throw new KeypleReaderException("Virtual Reader already exists");
        }
    }

    /**
     * Delete a virtual reader
     * 
     * @param nativeReaderName name of the virtual reader to be deleted
     */
    void disconnectRemoteReader(String nativeReaderName) throws KeypleReaderNotFoundException {
        logger.debug("disconnectRemoteReader {}", nativeReaderName);

        // check if reader is not already connected (by name)
        if (isReaderConnected(nativeReaderName)) {
            logger.info("DisconnectRemoteReader RemoteSeReader with name {} with session {}",
                    nativeReaderName);

            // retrieve virtual reader to delete
            final VirtualReader virtualReader =
                    (VirtualReader) this.getReaderByRemoteName(nativeReaderName);

            // remove observers
            ((VirtualReaderSessionImpl) virtualReader.getSession()).clearObservers();

            // remove reader
            virtualReaders.remove(virtualReader);

            new Thread() {
                public void run() {
                    notifyObservers(new PluginEvent(getName(), virtualReader.getName(),
                            PluginEvent.EventType.READER_DISCONNECTED));
                }
            }.start();


            logger.info("*****************************");
            logger.info(" DISCONNECTED {} ", nativeReaderName);
            logger.info("*****************************");

        } else {
            logger.warn("No remoteSeReader with name {} found", nativeReaderName);
        }
        // todo errors
    }

    /**
     * Propagate a received event from slave device
     * 
     * @param event
     * @param sessionId : not used yet
     */
    void onReaderEvent(ReaderEvent event, String sessionId) {
        logger.debug("OnReaderEvent {}", event);
        logger.debug("Dispatch ReaderEvent to the appropriate Reader {} {}", event.getReaderName(),
                sessionId);
        try {
            // todo dispatch is managed by name, should take sessionId also
            VirtualReader virtualReader =
                    (VirtualReader) getReaderByRemoteName(event.getReaderName());
            virtualReader.onRemoteReaderEvent(event);

        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        }

    }



    /**
     * Add an observer. This will allow to be notified about all readers or plugins events.
     *
     * @param observer Observer to notify
     */

    public void addObserver(ObservablePlugin.PluginObserver observer) {
        logger.trace("[{}][{}] addObserver => Adding an observer.", this.getClass(),
                this.getName());
        super.addObserver(observer);
    }

    /**
     * Remove an observer.
     *
     * @param observer Observer to stop notifying
     */

    public void removeObserver(ObservablePlugin.PluginObserver observer) {
        logger.trace("[{}] removeObserver => Deleting a reader observer", this.getName());
        super.removeObserver(observer);
    }



    /**
     * This method shall be called only from a SE Proxy plugin or reader implementing
     * AbstractObservableReader or AbstractObservablePlugin. Push a ReaderEvent / PluginEvent of the
     * selected AbstractObservableReader / AbstractObservablePlugin to its registered Observer.
     *
     * @param event the event
     */

    public final void notifyObservers(PluginEvent event) {
        logger.trace("[{}] AbstractObservableReader => Notifying a plugin event: ", this.getName(),
                event);
        setChanged();
        super.notifyObservers(event);

    }

    private Boolean isReaderConnected(String name) {
        for (VirtualReader VirtualReader : virtualReaders) {
            if (VirtualReader.getNativeReaderName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    // todo
    @Override
    public int compareTo(ReaderPlugin o) {
        return 0;
    }



}
