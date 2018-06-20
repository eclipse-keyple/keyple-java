/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.android.omapi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.event.AbstractObservableReader;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.local.AbstractLocalReader;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.Session;
import android.util.Log;

/**
 * TODO although AndroidOmapiReader extends AbstractObservable, may refuse any addition of observer
 */
public class AndroidOmapiReader extends AbstractLocalReader {


    private static final String TAG = AndroidOmapiReader.class.getSimpleName();

    private Reader omapiReader;
    private Channel channel = null;


    protected AndroidOmapiReader(Reader omapiReader) {
        this.omapiReader = omapiReader;
    }

    @Override
    public String getName() {
        return omapiReader.getName();
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IOException {

    }

    @Override
    public boolean isSePresent() throws IOReaderException {
        return omapiReader.isSecureElementPresent();
    }

    @Override
    protected ByteBuffer[] openLogicalChannelAndSelect(ByteBuffer aid) throws IOReaderException {
        ByteBuffer[] atrAndFci = new ByteBuffer[2];

        try {
            Log.i(TAG, "Create session...");

            //Not sure weither to keep a reference to the session or the channel at reader level
            Session session = omapiReader.openSession();

            //get ATR from session
            atrAndFci[0] = ByteBuffer.wrap(session.getATR());

            Log.i(TAG, "Create logical channel within the session...");

            //Not sure weither to keep a reference to the session or the channel at reader level
            channel = session.openLogicalChannel(
                    ByteBufferUtils.toBytes(aid));

            //get FCI
            atrAndFci[1] = ByteBuffer.wrap(channel.getSelectResponse());

        } catch (IOException e) {
            throw new IOReaderException(e.getMessage(), e.getCause());
        }

        return atrAndFci;
    }

    @Override
    protected void closePhysicalChannel() throws IOReaderException {
        //no physical channel in OMAPI
    }

    @Override
    protected ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException {
        // Initialization
        Log.d(TAG, "Data Length to be sent to tag : " + apduIn.limit());
        Log.d(TAG, "Data in : " + ByteBufferUtils.toHex(apduIn));
        byte[] data = ByteBufferUtils.toBytes(apduIn);
        byte[] dataOut = new byte[0];
        try {
            dataOut = channel.transmit(data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ChannelStateReaderException(e);
        }
        ByteBuffer out = ByteBuffer.wrap(dataOut);
        Log.d(TAG, "Data out : " + ByteBufferUtils.toHex(out));
        return out;
    }

    @Override
    protected boolean protocolFlagMatches(SeProtocol protocolFlag) throws IOReaderException {
        return true;
    }
}
