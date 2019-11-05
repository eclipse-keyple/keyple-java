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
package org.eclipse.keyple.core.seproxy.plugin.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableState;
import org.eclipse.keyple.core.seproxy.plugin.local.ObservableReaderStateService;
import org.eclipse.keyple.core.seproxy.plugin.local.SmartInsertionReader;
import org.eclipse.keyple.core.seproxy.plugin.local.monitoring.CardAbsentPingMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.local.monitoring.SmartInsertionMonitoringJob;
import org.eclipse.keyple.core.seproxy.plugin.local.state.WaitForSeInsertion;
import org.eclipse.keyple.core.seproxy.plugin.local.state.WaitForSeProcessing;
import org.eclipse.keyple.core.seproxy.plugin.local.state.WaitForSeRemoval;
import org.eclipse.keyple.core.seproxy.plugin.local.state.WaitForStartDetect;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlankSmartInsertionTheadedReader extends AbstractObservableLocalReader
        implements SmartInsertionReader {

    private static final Logger logger =
            LoggerFactory.getLogger(BlankSmartInsertionTheadedReader.class);

    Integer mockDetect;
    Integer detectCount = 0;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Reader constructor
     * <p>
     * Force the definition of a name through the use of super method.
     * <p>
     *
     * @param pluginName the name of the plugin that instantiated the reader
     * @param readerName the name of the reader
     */
    public BlankSmartInsertionTheadedReader(String pluginName, String readerName,
            Integer mockDetect) {
        super(pluginName, readerName);

        stateService = initStateService();
    }

    @Override
    final public ObservableReaderStateService initStateService() {


        Map<AbstractObservableState.MonitoringState, AbstractObservableState> states =
                new HashMap<AbstractObservableState.MonitoringState, AbstractObservableState>();
        states.put(AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
                new WaitForStartDetect(this));

        states.put(AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
                new WaitForSeInsertion(this, new SmartInsertionMonitoringJob(this),
                        executorService));

        states.put(AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
                new WaitForSeProcessing(this));

        states.put(AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
                new WaitForSeRemoval(this, new CardAbsentPingMonitoringJob(this), executorService));


        return new ObservableReaderStateService(this, states,
                AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION);
    }


    private Runnable waitForCardPresentFuture() {
        return new Runnable() {
            @Override
            public void run() {
                logger.trace("[{}] Invoke waitForCardPresent asynchronously",
                        BlankSmartInsertionTheadedReader.this.getName());
                try {
                    if (BlankSmartInsertionTheadedReader.this.waitForCardPresent()) {
                        onEvent(AbstractObservableLocalReader.InternalEvent.SE_INSERTED);
                    }
                } catch (KeypleIOReaderException e) {
                    logger.trace(
                            "[{}] waitForCardPresent => Error while polling card with waitForCardPresent",
                            BlankSmartInsertionTheadedReader.this.getName());
                    onEvent(AbstractObservableLocalReader.InternalEvent.STOP_DETECT);
                }
            }
        };
    }


    @Override
    public boolean checkSePresence() {
        return false;
    }

    @Override
    public byte[] getATR() {
        return new byte[0];
    }

    @Override
    public void openPhysicalChannel() throws KeypleChannelControlException {

    }

    @Override
    public void closePhysicalChannel() throws KeypleChannelControlException {

    }

    @Override
    public boolean isPhysicalChannelOpen() {
        return false;
    }

    @Override
    public boolean protocolFlagMatches(SeProtocol protocolFlag) throws KeypleReaderException {
        return false;
    }

    @Override
    public byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
        return new byte[0];
    }

    @Override
    public TransmissionMode getTransmissionMode() {
        return null;
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value)
            throws IllegalArgumentException, KeypleBaseException {

    }

    /*
     * @Override public boolean waitForCardPresent(long timeout) { // Obtain a number between [0 -
     * 49]. int n = new Random().nextInt(10); boolean isCardPresent = (n==2);
     * logger.trace("is card present {}",isCardPresent); return isCardPresent; }
     */

    @Override
    public boolean waitForCardPresent() throws KeypleIOReaderException {
        detectCount++;
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return detectCount <= mockDetect;
    }


}
