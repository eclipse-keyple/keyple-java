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
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractSelectionLocalReader;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class StubReader extends AbstractSelectionLocalReader {

    private static final ILogger logger = SLoggerFactory.getLogger(StubReader.class);

    private StubSecureElement se;

    private Map<String, String> parameters = new HashMap<String, String>();

    public static final String ALLOWED_PARAMETER_1 = "parameter1";
    public static final String ALLOWED_PARAMETER_2 = "parameter2";

    static final String pluginName = "StubPlugin";
    String readerName = "StubReader";

    public StubReader(String name) {
        super(pluginName, name);
        readerName = name;
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
    protected void openPhysicalChannel() throws IOReaderException, ChannelStateReaderException {
        if (se != null) {
            se.openPhysicalChannel();
        }
    }

    @Override
    public void closePhysicalChannel() throws IOReaderException {
        if (se != null) {
            se.closePhysicalChannel();
        }
    }

    @Override
    public ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException {
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
    public void setParameter(String name, String value) throws IOReaderException {
        if (name.equals(ALLOWED_PARAMETER_1) || name.equals(ALLOWED_PARAMETER_2)) {
            parameters.put(name, value);
        } else {
            throw new IOReaderException("parameter name not supported : " + name);
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
            throws ChannelStateReaderException {
        return this.processApduRequest(apduRequest);
    }

    final SeResponseSet processSeRequestSetTestProxy(SeRequestSet requestSet)
            throws IOReaderException {
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
        notifyObservers(new ReaderEvent(pluginName, readerName, ReaderEvent.EventType.SE_INSERTED));
    }

    public void removeSe() {
        se = null;
        notifyObservers(new ReaderEvent(pluginName, readerName, ReaderEvent.EventType.SE_REMOVAL));
    }



}
