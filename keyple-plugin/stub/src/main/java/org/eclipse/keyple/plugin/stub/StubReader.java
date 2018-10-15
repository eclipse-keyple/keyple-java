/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.plugin.stub;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.plugin.AbstractThreadedLocalReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StubReader extends AbstractThreadedLocalReader {

    private static final Logger logger = LoggerFactory.getLogger(StubReader.class);

    private StubSecureElement se;

    private boolean sePresent;

    private Map<String, String> parameters = new HashMap<String, String>();

    public static final String ALLOWED_PARAMETER_1 = "parameter1";
    public static final String ALLOWED_PARAMETER_2 = "parameter2";

    static final String pluginName = "StubPlugin";
    String readerName = "StubReader";

    public StubReader(String name) {
        super(pluginName, name);
        readerName = name;
        sePresent = false;
        threadWaitTimeout = 5000;
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
    protected void openPhysicalChannel() throws KeypleChannelStateException {
        if (se != null) {
            se.openPhysicalChannel();
        }
    }

    @Override
    public void closePhysicalChannel() throws KeypleChannelStateException {
        if (se != null) {
            se.closePhysicalChannel();
        }
    }

    @Override
    public byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
        return se.processApdu(apduIn);
    }

    @Override
    protected final boolean protocolFlagMatches(SeProtocol protocolFlag)
            throws KeypleReaderException {
        boolean result;
        // Get protocolFlag to check if ATR filtering is required
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
    public boolean isSePresent() {
        return se != null;
    }

    @Override
    public void setParameter(String name, String value) throws KeypleReaderException {
        if (name.equals(ALLOWED_PARAMETER_1) || name.equals(ALLOWED_PARAMETER_2)) {
            parameters.put(name, value);
        } else {
            throw new KeypleReaderException("parameter name not supported : " + name);
        }
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }


    /*
     * HELPERS TO TEST INTERNAL METHOD TODO : is this necessary?
     */
    final ApduResponse processApduRequestTestProxy(ApduRequest apduRequest)
            throws KeypleReaderException {
        return this.processApduRequest(apduRequest);
    }

    final SeResponseSet processSeRequestSetTestProxy(SeRequestSet requestSet)
            throws KeypleReaderException {
        return this.processSeRequestSet(requestSet);
    }

    final boolean isLogicalChannelOpenTestProxy() {
        return this.isPhysicalChannelOpen();
    }



    /*
     * STATE CONTROLLERS FOR INSERTING AND REMOVING SECURE ELEMENT
     */
    public void insertSe(StubSecureElement _se) {
        /* clean channels status */
        if (isPhysicalChannelOpen()) {
            closeLogicalChannel();
            try {
                closePhysicalChannel();
            } catch (KeypleReaderException e) {
                e.printStackTrace();
            }
        }
        se = _se;
        sePresent = true;
    }

    public void removeSe() {
        se = null;
        sePresent = false;
    }

    /**
     * This method is called by the monitoring thread to check SE presence
     * 
     * @param timeout the delay in millisecond we wait for a card insertion
     * @return true if the SE is present
     * @throws NoStackTraceThrowable in case of unplugging the reader
     */
    @Override
    protected boolean waitForCardPresent(long timeout) throws NoStackTraceThrowable {
        for (int i = 0; i < timeout / 10; i++) {
            if (sePresent) {
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return sePresent;
    }

    /**
     * This method is called by the monitoring thread to check SE absence
     * 
     * @param timeout the delay in millisecond we wait for a card withdrawing
     * @return true if the SE is absent
     * @throws NoStackTraceThrowable in case of unplugging the reader
     */
    @Override
    protected boolean waitForCardAbsent(long timeout) throws NoStackTraceThrowable {
        for (int i = 0; i < timeout / 10; i++) {
            if (!sePresent) {
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return !sePresent;
    }
}
