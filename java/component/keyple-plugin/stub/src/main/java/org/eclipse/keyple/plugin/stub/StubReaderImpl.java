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
package org.eclipse.keyple.plugin.stub;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.plugin.local.*;
import org.eclipse.keyple.core.seproxy.plugin.local.monitoring.SmartInsertionMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.local.monitoring.SmartRemovalMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.local.state.WaitForSeInsertion;
import org.eclipse.keyple.core.seproxy.plugin.local.state.WaitForSeProcessing;
import org.eclipse.keyple.core.seproxy.plugin.local.state.WaitForSeRemoval;
import org.eclipse.keyple.core.seproxy.plugin.local.state.WaitForStartDetect;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates communication with a {@link StubSecureElement}. StubReader is observable, it raises
 * {@link org.eclipse.keyple.core.seproxy.event.ReaderEvent} : SE_INSERTED, SE_REMOVED
 */
class StubReaderImpl extends AbstractObservableLocalReader
        implements StubReader, SmartInsertionReader, SmartRemovalReader {

    private static final Logger logger = LoggerFactory.getLogger(StubReaderImpl.class);

    private StubSecureElement se;

    private Map<String, String> parameters = new HashMap<String, String>();

    TransmissionMode transmissionMode = TransmissionMode.CONTACTLESS;

    final protected ExecutorService executorService;


    final private AtomicBoolean loopWaitSe = new AtomicBoolean();
    final private AtomicBoolean loopWaitSeRemoval = new AtomicBoolean();

    /**
     * Do not use directly
     * 
     * @param readerName
     */
    StubReaderImpl(String pluginName, String readerName) {
        super(pluginName, readerName);

        // create a executor service with one thread whose name is customized
        executorService = Executors
                .newSingleThreadExecutor(new NamedThreadFactory("MonitoringThread-" + readerName));

        stateService = initStateService();


    }

    /**
     * Specify
     * 
     * @param pluginName
     * @param name
     * @param transmissionMode
     */
    StubReaderImpl(String pluginName, String name, TransmissionMode transmissionMode) {
        this(pluginName, name);
        this.transmissionMode = transmissionMode;
    }

    @Override
    protected byte[] getATR() {
        return se.getATR();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isPhysicalChannelOpen() {
        return se != null && se.isPhysicalChannelOpen();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void openPhysicalChannel() {
        if (se != null) {
            se.openPhysicalChannel();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closePhysicalChannel() {
        if (se != null) {
            se.closePhysicalChannel();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] transmitApdu(byte[] apduIn) {
        if (se == null) {
            throw new KeypleReaderIOException("No SE available.");
        }
        return se.processApdu(apduIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean protocolFlagMatches(SeProtocol protocolFlag) {
        boolean result;
        if (se == null) {
            throw new KeypleReaderIOException("No SE available.");
        }
        // Test protocolFlag to check if ATR based protocol filtering is required
        if (protocolFlag != null) {
            if (!isPhysicalChannelOpen()) {
                openPhysicalChannel();
            }
            // the request will be executed only if the protocol match the requestElement
            String selectionMask = protocolsMap.get(protocolFlag);
            if (selectionMask == null) {
                throw new KeypleReaderIOException("Target selector mask not found!", null);
            }
            Pattern p = Pattern.compile(selectionMask);
            String protocol = se.getSeProcotol();
            if (!p.matcher(protocol).matches()) {
                logger.trace("[{}] protocolFlagMatches => unmatching SE. PROTOCOLFLAG = {}",
                        this.getName(), protocolFlag);
                result = false;
            } else {
                logger.trace("[{}] protocolFlagMatches => matching SE. PROTOCOLFLAG = {}",
                        this.getName(), protocolFlag);
                result = true;
            }
        } else {
            // no protocol defined returns true
            result = true;
        }
        return result;
    }


    @Override
    protected synchronized boolean checkSePresence() {
        return se != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParameter(String name, String value) {
        parameters.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * @return the current transmission mode
     */
    @Override
    public TransmissionMode getTransmissionMode() {
        return transmissionMode;
    }

    /*
     * STATE CONTROLLERS FOR INSERTING AND REMOVING SECURE ELEMENT
     */

    public synchronized void insertSe(StubSecureElement _se) {
        logger.debug("Insert SE {}", _se);
        /* clean channels status */
        if (isPhysicalChannelOpen()) {
            try {
                closePhysicalChannel();
            } catch (KeypleReaderException e) {
                logger.error("Error while closing channel reader", e);
            }
        }
        if (_se != null) {
            se = _se;
        }
    }

    public synchronized void removeSe() {
        logger.debug("Remove SE {}", se != null ? se : "none");

        se = null;
    }

    public StubSecureElement getSe() {
        return se;
    }

    /**
     * This method is called by the monitoring thread to check SE presence
     * 
     * @return true if the SE is present
     */
    @Override
    public boolean waitForCardPresent() {
        loopWaitSe.set(true);
        while (loopWaitSe.get()) {
            if (checkSePresence()) {
                return true;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                logger.debug("Sleep was interrupted");
            }
        }
        return false;
        // logger.trace("[{}] no card was inserted", this.getName());
        // return false;
    }

    @Override
    public void stopWaitForCard() {
        loopWaitSe.set(false);
    }

    /**
     * Defined in the {@link org.eclipse.keyple.core.seproxy.plugin.local.SmartRemovalReader}
     * interface, this method is called by the monitoring thread to check SE absence
     * 
     * @return true if the SE is absent
     */
    @Override
    public boolean waitForCardAbsentNative() {
        loopWaitSeRemoval.set(true);
        while (loopWaitSeRemoval.get()) {
            if (!checkSePresence()) {
                logger.trace("[{}] card removed", this.getName());
                return true;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                logger.debug("Sleep was interrupted");
            }
        }
        return false;
        // logger.trace("[{}] no card was removed", this.getName());
        // return false;
    }

    @Override
    public void stopWaitForCardRemoval() {
        loopWaitSeRemoval.set(false);
    }

    @Override
    final protected ObservableReaderStateService initStateService() {
        if (executorService == null) {
            throw new IllegalArgumentException("Executor service has not been initialized");
        }

        Map<AbstractObservableState.MonitoringState, AbstractObservableState> states =
                new HashMap<AbstractObservableState.MonitoringState, AbstractObservableState>();

        states.put(AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
                new WaitForStartDetect(this));

        states.put(AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
                new WaitForSeInsertion(this, new SmartInsertionMonitoringJob(this),
                        executorService));

        states.put(AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
                new WaitForSeProcessing(this, new SmartRemovalMonitoringJob(this),
                        executorService));

        states.put(AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
                new WaitForSeRemoval(this, new SmartRemovalMonitoringJob(this), executorService));

        return new ObservableReaderStateService(this, states,
                AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION);
    }
}
