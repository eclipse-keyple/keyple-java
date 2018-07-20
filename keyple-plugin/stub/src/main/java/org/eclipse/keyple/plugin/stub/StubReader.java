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
import java.util.Set;

import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.InvalidMessageException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.exception.SelectApplicationException;
import org.eclipse.keyple.seproxy.plugin.AbstractLocalReader;
import org.eclipse.keyple.seproxy.plugin.AbstractSelectionLocalReader;
import org.eclipse.keyple.seproxy.plugin.AbstractThreadedLocalReader;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class StubReader extends AbstractSelectionLocalReader {

    private static final ILogger logger = SLoggerFactory.getLogger(StubReader.class);

    private StubSecureElement se;

    private Map<String, String> parameters = new HashMap<String, String>();

    public static final String ALLOWED_PARAMETER_1 = "parameter1";
    public static final String ALLOWED_PARAMETER_2 = "parameter2";

    static final String pluginName = "stubPlugin";
    static final String readerName = "stubReader";

    public StubReader() {
        super(pluginName, readerName);
    }


    @Override
    protected ByteBuffer getATR() {
        return se.getATR();
    }

    @Override
    protected boolean isPhysicalChannelOpen() {
        return se.isPhysicalChannelOpen();
    }

    @Override
    protected void openPhysicalChannel() throws IOReaderException, ChannelStateReaderException {
        se.openPhysicalChannel();
    }


    @Override
    public void closePhysicalChannel() throws IOReaderException {
        se.closePhysicalChannel();
    }

    @Override
    public ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException {
        return se.transmitApdu(apduIn);
    }

    @Override
    public boolean protocolFlagMatches(SeProtocol protocolFlag) throws InvalidMessageException {
        return protocolFlag.equals(se.getSeProcotol());
    }

    @Override
    public boolean isSePresent() throws NoStackTraceThrowable {
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



    public void insertSe(StubSecureElement _se){
        se = _se;
        notifyObservers(new ReaderEvent(pluginName,readerName, ReaderEvent.EventType.SE_INSERTED));
    }

    public void removeSe(StubSecureElement se){
        se = null;
        notifyObservers(new ReaderEvent(pluginName,readerName, ReaderEvent.EventType.SE_REMOVAL));
    }

}
