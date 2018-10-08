/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.rse;

import java.util.Map;
import org.eclipse.keyple.seproxy.*;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.util.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RseReader extends Observable implements ObservableReader {

    private final IReaderSession session;// can be sync or async
    private final String remoteName;
    private final String name;

    private static final Logger logger = LoggerFactory.getLogger(RseReader.class);

    public RseReader(IReaderSession session, String remoteName) {
        this.session = session;
        this.remoteName = remoteName;
        this.name = "remote-" + remoteName;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public IReaderSession getSession() {
        return session;
    }

    @Override
    public boolean isSePresent() {
        return false;// not implemented
    }

    /**
     * Transmit synchronously (works only with Sync Session)
     * 
     * @param seRequestSet : SeRequestSe to be transmitted
     * @return seResponseSet : SeResponSet
     * @throws KeypleReaderException, IllegalArgumentException
     */
    @Override
    public SeResponseSet transmit(SeRequestSet seRequestSet) throws IllegalArgumentException {
        return ((IReaderAsyncSession) session).transmit(seRequestSet);
    }

    @Override
    public SeResponse transmit(SeRequest seApplicationRequest) throws IllegalArgumentException {
        return ((IReaderAsyncSession) session).transmit(new SeRequestSet(seApplicationRequest))
                .getSingleResponse();
        // todo change this?
    }

    /**
     * Transmit synchronously (works only with Sync Session)
     *
     * @param seRequestSet : SeRequestSe to be transmitted
     * @return seResponseSet : SeResponSet
     * @throws KeypleReaderException, IllegalArgumentException
     */

    public SeResponseSet transmitNewThread(SeRequestSet seRequestSet)
            throws IllegalArgumentException {
        return ((IReaderAsyncSession) session).transmit(seRequestSet);
    }


    /**
     * Transmit asynchronously (works only with ASync Session)
     * 
     * @param seRequestSet : SeRequestSe to be transmitted
     * @param seResponseSetCallback : callback to get the seResponseSet
     */
    public void asyncTransmit(SeRequestSet seRequestSet,
            ISeResponseSetCallback seResponseSetCallback) throws IllegalArgumentException {
        ((IReaderAsyncSession) session).asyncTransmit(seRequestSet, seResponseSetCallback);
    }

    @Override
    public void addSeProtocolSetting(SeProtocolSetting seProtocolSetting) {
        // not implemented
    }

    /**
     * When an event occurs on the Remote LocalReader, notify Observers
     * 
     * @param event
     */
    public void onRemoteReaderEvent(ReaderEvent event) {
        logger.info("*****************************");
        logger.info(" EVENT {} ", event.getEventType());
        logger.info("*****************************");
        this.notifyObservers(event);
    }


    /**
     *
     * HELPERS
     */


    // compare by name
    @Override
    public int compareTo(ProxyReader o) {
        return o.getName().compareTo(this.getName());
    }// todo

    @Override
    public Map<String, String> getParameters() {
        return null;
    }// todo

    @Override
    public void setParameter(String key, String value) throws IllegalArgumentException {

    }

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {

    }


    /**
     * Add an observer. This will allow to be notified about all readers or plugins events.
     *
     * @param observer Observer to notify
     */

    public void addObserver(ReaderObserver observer) {
        logger.trace("[{}][{}] addObserver => Adding an observer.", this.getClass(),
                this.getName());
        super.addObserver(observer);
    }

    /**
     * Remove an observer.
     *
     * @param observer Observer to stop notifying
     */

    public void removeObserver(ReaderObserver observer) {
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

    public final void notifyObservers(ReaderEvent event) {
        logger.trace("[{}] AbstractObservableReader => Notifying a reader event: ", this.getName(),
                event);
        setChanged();
        super.notifyObservers(event);

    }



}
