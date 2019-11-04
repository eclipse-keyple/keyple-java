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
import java.util.regex.Pattern;
import org.eclipse.keyple.core.seproxy.exception.KeypleChannelControlException;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.plugin.*;
import org.eclipse.keyple.core.seproxy.plugin.monitor.SmartInsertionMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.monitor.SmartRemovalMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.state.*;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulates communication with a {@link StubSecureElement}. StubReader is observable, it raises
 * {@link org.eclipse.keyple.core.seproxy.event.ReaderEvent} : SE_INSERTED, SE_REMOVED
 */
final class StubReaderImpl extends AbstractObservableLocalReader
        implements StubReader, SmartInsertionReader, SmartRemovalReader {

    private static final Logger logger = LoggerFactory.getLogger(StubReaderImpl.class);

    private StubSecureElement se;

    private Map<String, String> parameters = new HashMap<String, String>();

    TransmissionMode transmissionMode = TransmissionMode.CONTACTLESS;

    protected ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Do not use directly
     * 
     * @param name
     */
    StubReaderImpl(String name) {
        super(StubPlugin.PLUGIN_NAME, name);

        stateService = initStateService();
    }

    StubReaderImpl(String name, TransmissionMode transmissionMode) {
        this(name);
        this.transmissionMode = transmissionMode;
    }

    @Override
    protected byte[] getATR() {
        return se.getATR();
    }

    @Override
    protected boolean isPhysicalChannelOpen() {
        return se != null && se.isPhysicalChannelOpen();
    }

    @Override
    protected void openPhysicalChannel() throws KeypleChannelControlException {
        if (se != null) {
            se.openPhysicalChannel();
        }
    }

    @Override
    public void closePhysicalChannel() throws KeypleChannelControlException {
        if (se != null) {
            se.closePhysicalChannel();
        }
    }

    @Override
    public byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
        if (se == null) {
            throw new KeypleIOReaderException("No SE available.");
        }
        return se.processApdu(apduIn);
    }

    @Override
    protected boolean protocolFlagMatches(SeProtocol protocolFlag) throws KeypleReaderException {
        boolean result;
        if (se == null) {
            throw new KeypleReaderException("No SE available.");
        }
        // Test protocolFlag to check if ATR based protocol filtering is required
        if (protocolFlag != null) {
            if (!isPhysicalChannelOpen()) {
                openPhysicalChannel();
            }
            // the requestSet will be executed only if the protocol match the requestElement
            String selectionMask = protocolsMap.get(protocolFlag);
            if (selectionMask == null) {
                throw new KeypleReaderException("Target selector mask not found!", null);
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

    @Override
    public void setParameter(String name, String value) throws KeypleReaderException {
        if (name.equals(ALLOWED_PARAMETER_1) || name.equals(ALLOWED_PARAMETER_2)) {
            parameters.put(name, value);
        } else if (name.equals(CONTACTS_PARAMETER)) {
            transmissionMode = TransmissionMode.CONTACTS;
        } else if (name.equals(CONTACTLESS_PARAMETER)) {
            transmissionMode = TransmissionMode.CONTACTLESS;
        } else {
            throw new KeypleReaderException("parameter name not supported : " + name);
        }
    }

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
        // logger.info("Insert SE {}", _se);
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
        // for (int i = 0; i < timeout / 10; i++) {
        while (true) {
            if (checkSePresence()) {
                return true;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                logger.debug("Sleep was interrupted");
            }
        }
        // logger.trace("[{}] no card was inserted", this.getName());
        // return false;
    }

    /**
     * Defined in the {@link org.eclipse.keyple.core.seproxy.plugin.SmartRemovalReader} interface,
     * this method is called by the monitoring thread to check SE absence
     * 
     * @return true if the SE is absent
     */
    @Override
    public boolean waitForCardAbsentNative() {
        // for (int i = 0; i < timeout / 10; i++) {
        while (true) {
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
        // logger.trace("[{}] no card was removed", this.getName());
        // return false;
    }

    @Override
    protected ObservableReaderStateService initStateService() {
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
