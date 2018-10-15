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

package org.eclipse.keyple.plugin.android.omapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.exception.KeypleApplicationSelectionException;
import org.eclipse.keyple.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.plugin.AbstractStaticReader;
import org.eclipse.keyple.seproxy.protocol.ContactsProtocols;
import org.eclipse.keyple.util.ByteArrayUtils;
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
    private byte[] openApplication = null;
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
    public void setParameter(String key, String value) {
        Log.w(TAG, "No parameters are supported by AndroidOmapiReader");
        parameters.put(key, value);
    }

    /**
     * Check if a SE is present in this reader. see {@link Reader#isSecureElementPresent()}
     * 
     * @return True if the SE is present, false otherwise
     * @throws KeypleReaderException
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
     * @throws KeypleReaderException
     */
    @Override
    protected byte[][] openLogicalChannelAndSelect(SeRequest.Selector selector,
            Set<Short> successfulSelectionStatusCodes)
            throws KeypleChannelStateException, KeypleApplicationSelectionException {
        byte[][] atrAndFci = new byte[2][];
        byte[] aid = ((SeRequest.AidSelector) selector).getAidToSelect();
        try {

            if (openChannel != null && !openChannel.isClosed() && openApplication != null
                    && openApplication.equals(aid)) {
                Log.i(TAG, "Channel is already open to aid : " + ByteArrayUtils.toHex(aid));

                atrAndFci[0] = openChannel.getSession().getATR();
                atrAndFci[1] = openChannel.getSelectResponse();


            } else {

                Log.i(TAG, "Opening channel to aid : " + ByteArrayUtils.toHex(aid));

                // open physical channel
                Session session = omapiReader.openSession();

                // get ATR from session
                Log.i(TAG, "Retrieveing ATR from session...");
                atrAndFci[0] = session.getATR();

                Log.i(TAG, "Create logical openChannel within the session...");
                openChannel = session.openLogicalChannel(aid);

                // get FCI
                atrAndFci[1] = openChannel.getSelectResponse();

            }
        } catch (IOException e) {
            throw new KeypleChannelStateException(
                    "Error while opening channel, aid :" + ByteArrayUtils.toHex(aid), e.getCause());
        } catch (SecurityException e) {
            throw new KeypleChannelStateException(
                    "Error while opening channel, aid :" + ByteArrayUtils.toHex(aid), e.getCause());
        } catch (NoSuchElementException e) {
            throw new KeypleApplicationSelectionException(
                    "Error while selecting application : " + ByteArrayUtils.toHex(aid), e);
        }

        return atrAndFci;
    }

    /**
     * Close session see {@link Session#close()}
     * 
     * @throws KeypleReaderException
     */
    @Override
    protected void closePhysicalChannel() {
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
     * @throws KeypleReaderException
     */
    @Override
    protected byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
        // Initialization
        Log.d(TAG, "Data Length to be sent to tag : " + apduIn.length);
        Log.d(TAG, "Data in : " + ByteArrayUtils.toHex(apduIn));
        byte[] data = apduIn;
        byte[] dataOut = new byte[0];
        try {
            dataOut = openChannel.transmit(data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new KeypleIOReaderException("Error while transmitting APDU", e);
        }
        byte[] out = dataOut;
        Log.d(TAG, "Data out : " + ByteArrayUtils.toHex(out));
        return out;
    }

    /**
     * The only protocol Fla
     * 
     * @param protocolFlag
     * @return true
     * @throws KeypleReaderException
     */
    @Override
    protected boolean protocolFlagMatches(SeProtocol protocolFlag) {
        return protocolFlag.equals(ContactsProtocols.PROTOCOL_ISO7816_3);
    }
}
