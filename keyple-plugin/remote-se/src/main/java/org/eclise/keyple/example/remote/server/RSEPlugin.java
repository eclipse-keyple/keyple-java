/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server;

import java.io.IOException;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.UnexpectedReaderException;
import org.eclipse.keyple.util.Observable;
import org.eclise.keyple.example.remote.server.transport.RSEReaderSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RSEPlugin extends Observable implements ObservablePlugin {

    private static final Logger logger = LoggerFactory.getLogger(RSEPlugin.class);

    SortedSet<RSEReader> remoteReaders = new TreeSet<RSEReader>();

    public RSEPlugin() {
        logger.info("RemoteSePlugin");
    }

    @Override
    public String getName() {
        return "RemoteSePlugin";
    }

    @Override
    public SortedSet<? extends ProxyReader> getReaders() {
        return remoteReaders;
    }

    @Override
    public ProxyReader getReader(String name) throws UnexpectedReaderException {
        for (RSEReader RSEReader : remoteReaders) {
            if (RSEReader.getName().equals(name)) {
                return RSEReader;
            }
        }
        throw new UnexpectedReaderException("reader with name not found : " + name);
    }

    public ProxyReader getReaderByRemoteName(String remoteName) throws UnexpectedReaderException {
        for (RSEReader RSEReader : remoteReaders) {
            if (RSEReader.getRemoteName().equals(remoteName)) {
                return RSEReader;
            }
        }
        throw new UnexpectedReaderException("reader with Remote Name not found : " + remoteName);
    }


    public String connectRemoteReader(String name, RSEReaderSession session) {
        logger.debug("connectRemoteReader {}", name);

        // check if reader is not already connected (by name)
        if (!isReaderConnected(name)) {
            logger.info("Connecting a new RemoteSeReader with name {}", name);
            RSEReader RSEReader = new RSEReader(session, name);
            remoteReaders.add(RSEReader);
            notifyObservers(new PluginEvent(getName(), RSEReader.getName(),
                    PluginEvent.EventType.READER_CONNECTED));
            logger.info("*****************************");
            logger.info(" CONNECTED {} ", RSEReader.getName());
            logger.info("*****************************");

            return session.getSessionId();
        } else {
            logger.warn("RemoteSeReader with name {} is already connected", name);
            return session.getSessionId();
        }
    }

    public void onReaderEvent(ReaderEvent event, String sessionId) {
        logger.debug("onReaderEvent {}", event);
        logger.debug("Dispatch ReaderEvent to the appropriate Reader {} {}", event.getReaderName(), sessionId);
        try {
            //todo dispatch only by name not by sessionId for the moment
            RSEReader RSEReader =
                    (RSEReader) getReaderByRemoteName(event.getReaderName());
            RSEReader.onRemoteReaderEvent(event);

        } catch (UnexpectedReaderException e) {
            e.printStackTrace();
        }

    }


    @Override
    public int compareTo(ReaderPlugin o) {
        return 0;
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IOException {

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

    /**
     * Set a list of parameters on a reader.
     * <p>
     * See {@link #setParameter(String, String)} for more details
     *
     * @param parameters the new parameters
     * @throws IOReaderException This method can fail when disabling the exclusive mode as it's
     *         executed instantly
     */
    public final void setParameters(Map<String, String> parameters) throws IOException {
        for (Map.Entry<String, String> en : parameters.entrySet()) {
            setParameter(en.getKey(), en.getValue());
        }
    }


    private Boolean isReaderConnected(String name) {
        for (RSEReader RSEReader : remoteReaders) {
            if (RSEReader.getRemoteName().equals(name)) {
                return true;
            }
        }
        return false;
    }


}
