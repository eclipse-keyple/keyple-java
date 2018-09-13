/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponseSet;
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
    protected ByteBuffer getATR() {
        return se.getATR();
    }

    @Override
    protected boolean isPhysicalChannelOpen() {
        return se != null && se.isPhysicalChannelOpen();
    }

    @Override
    protected void openPhysicalChannel() throws KeypleReaderException, KeypleReaderException {
        if (se != null) {
            se.openPhysicalChannel();
        }
    }

    @Override
    public void closePhysicalChannel() throws KeypleReaderException {
        if (se != null) {
            se.closePhysicalChannel();
        }
    }

    @Override
    public ByteBuffer transmitApdu(ByteBuffer apduIn) throws KeypleReaderException {
        return se.processApdu(apduIn);
    }

    @Override
    public boolean protocolFlagMatches(SeProtocol protocolFlag) {
        return protocolFlag == null || se != null && protocolFlag.equals(se.getSeProcotol());
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
     * @throws NoStackTraceThrowable
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
     * @throws NoStackTraceThrowable
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
