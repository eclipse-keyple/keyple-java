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
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleApplicationSelectionException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.plugin.AbstractStaticReader;
import org.eclipse.keyple.seproxy.protocol.ContactsProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.Session;
import android.util.Log;

/**
 * Communicates with Android readers throught the Open Mobile API see {@link Reader} Instances of
 * this class represent SE readers supported by this device. These readers can be physical devices
 * or virtual devices. They can be removable or not. They can contain one SE that can or cannot be
 * removed.
 */
public class AndroidOmapiReader extends AbstractStaticReader {


    private static final String TAG = AndroidOmapiReader.class.getSimpleName();

    private Reader omapiReader;
    private Channel openChannel = null;
    private ByteBuffer openApplication = null;
    private Map<String, String> parameters = new HashMap<String, String>();

    protected AndroidOmapiReader(String pluginName, Reader omapiReader, String readerName) {
        super(pluginName, readerName);
        this.omapiReader = omapiReader;
    }


    @Override
    public Map<String, String> getParameters() {
        Log.w(TAG, "No parameters are supported by AndroidOmapiReader");
        return parameters;
    }

    @Override
    public void setParameter(String key, String value) throws IOException {
        Log.w(TAG, "No parameters are supported by AndroidOmapiReader");
        parameters.put(key, value);
    }

    /**
     * Check if a SE is present in this reader. see {@link Reader#isSecureElementPresent()}
     * 
     * @return True if the SE is present, false otherwise
     * @throws IOReaderException
     */
    @Override
    public boolean isSePresent() throws NoStackTraceThrowable {
        return omapiReader.isSecureElementPresent();
    }

    /**
     * Open a Channel to the application AID if not open yet. see {@link Reader#openSession()} see
     * {@link Session#openLogicalChannel(byte[])}
     * 
     * @param selector: AID of the application to select
     * @return Array : index[0] : ATR and index[1] :FCI
     * @throws IOReaderException
     */
    @Override
    protected ByteBuffer[] openLogicalChannelAndSelect(SeRequest.Selector selector,
            Set<Short> successfulSelectionStatusCodes)
            throws IOReaderException, KeypleApplicationSelectionException {
        ByteBuffer[] atrAndFci = new ByteBuffer[2];
        ByteBuffer aid = ((SeRequest.AidSelector) selector).getAidToSelect();
        try {

            if (openChannel != null && !openChannel.isClosed() && openApplication != null
                    && openApplication.equals(aid)) {
                Log.i(TAG, "Channel is already open to aid : " + ByteBufferUtils.toHex(aid));

                atrAndFci[0] = ByteBuffer.wrap(openChannel.getSession().getATR());
                atrAndFci[1] = ByteBuffer.wrap(openChannel.getSelectResponse());


            } else {

                Log.i(TAG, "Opening channel to aid : " + ByteBufferUtils.toHex(aid));

                // open physical channel
                Session session = omapiReader.openSession();

                // get ATR from session
                Log.i(TAG, "Retrieveing ATR from session...");
                atrAndFci[0] = session.getATR() != null ? ByteBuffer.wrap(session.getATR()) : null;

                Log.i(TAG, "Create logical openChannel within the session...");
                openChannel = session.openLogicalChannel(ByteBufferUtils.toBytes(aid));

                // get FCI
                atrAndFci[1] = ByteBuffer.wrap(openChannel.getSelectResponse());

            }
        } catch (IOException e) {
            throw new IOReaderException(e.getMessage(), e.getCause());
        } catch (SecurityException e) {
            throw new IOReaderException(e.getMessage(), e.getCause());
        } catch (NoSuchElementException e) {
            throw new KeypleApplicationSelectionException(e.getMessage());
        }

        return atrAndFci;
    }

    /**
     * Close session see {@link Session#close()}
     * 
     * @throws IOReaderException
     */
    @Override
    protected void closePhysicalChannel() throws IOReaderException {
        // close physical channel if exists
        if (openApplication != null) {
            openChannel.getSession().close();
            openChannel = null;
            openApplication = null;
        }
    }

    /**
     * Transmit an APDU command (as per ISO/IEC 7816) to the SE see {@link Channel#transmit(byte[])}
     * 
     * @param apduIn byte buffer containing the ingoing data
     * @return
     * @throws ChannelStateReaderException
     */
    @Override
    protected ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException {
        // Initialization
        Log.d(TAG, "Data Length to be sent to tag : " + apduIn.limit());
        Log.d(TAG, "Data in : " + ByteBufferUtils.toHex(apduIn));
        byte[] data = ByteBufferUtils.toBytes(apduIn);
        byte[] dataOut = new byte[0];
        try {
            dataOut = openChannel.transmit(data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ChannelStateReaderException(e);
        }
        ByteBuffer out = ByteBuffer.wrap(dataOut);
        Log.d(TAG, "Data out : " + ByteBufferUtils.toHex(out));
        return out;
    }

    /**
     * The only protocol Fla
     * 
     * @param protocolFlag
     * @return true
     * @throws IOReaderException
     */
    @Override
    protected boolean protocolFlagMatches(SeProtocol protocolFlag) throws IOReaderException {
        return protocolFlag.equals(ContactsProtocols.PROTOCOL_ISO7816_3);
    }
}
