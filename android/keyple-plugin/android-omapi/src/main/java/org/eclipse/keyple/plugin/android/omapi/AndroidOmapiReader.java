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
package org.eclipse.keyple.plugin.android.omapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.exception.KeypleApplicationSelectionException;
import org.eclipse.keyple.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.NoStackTraceThrowable;
import org.eclipse.keyple.seproxy.message.*;
import org.eclipse.keyple.seproxy.plugin.AbstractStaticReader;
import org.eclipse.keyple.seproxy.protocol.ContactsProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Log;

/**
 * Communicates with Android readers throught the Open Mobile API see {@link Reader} Instances of
 * this class represent SE readers supported by this device. These readers can be physical devices
 * or virtual devices. They can be removable or not. They can contain one SE that can or cannot be
 * removed.
 */
public final class AndroidOmapiReader extends AbstractStaticReader {

    private static final Logger logger =
            LoggerFactory.getLogger(AndroidOmapiReader.class);

    private static final String TAG = AndroidOmapiReader.class.getSimpleName();

    private Reader omapiReader;
    private Session session = null;
    private Channel openChannel = null;
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
     * The transmission mode is always CONTACTS in an OMAPI reader
     *
     * @return the current transmission mode
     */
    @Override
    public TransmissionMode getTransmissionMode() {
        return TransmissionMode.CONTACTS;
    }

    /**
     * Check if a SE is present in this reader. see {@link Reader#isSecureElementPresent()}
     * 
     * @return True if the SE is present, false otherwise
     * @throws KeypleReaderException
     */
    @Override
    protected boolean checkSePresence() throws NoStackTraceThrowable {
        return omapiReader.isSecureElementPresent();
    }

    /**
     * Get the SE Answer To Reset
     * @return a byte array containing the ATR or null if no session was available
     */
    @Override
    protected byte[] getATR() {
        if(session != null) {
            Log.i(TAG, "Retrieveing ATR from session...");
            return session.getATR();
        }
        else {
            return null;
        }
    }

    /**
     * Operate a logical channel opening.
     * <p>
     * The channel opening is done according to the AidSelector and AtrFilter combination.
     *
     * @param seSelector the selection data
     * @return the SelectionStatus
     * @throws KeypleIOReaderException if an IOException occurs
     */
    @Override
    protected final SelectionStatus openLogicalChannel(SeSelector seSelector)
            throws KeypleIOReaderException, KeypleChannelStateException, KeypleApplicationSelectionException {
        ApduResponse fciResponse;
        byte[] atr = getATR();
        boolean selectionHasMatched = true;
        SelectionStatus selectionStatus;

        /** Perform ATR filtering if requested */
        if (seSelector.getAtrFilter() != null) {
            if (atr == null) {
                throw new KeypleIOReaderException("Didn't get an ATR from the SE.");
            }

            if (logger.isTraceEnabled()) {
                logger.trace("[{}] openLogicalChannel => ATR: {}", this.getName(),
                        ByteArrayUtils.toHex(atr));
            }
            if (!seSelector.getAtrFilter().atrMatches(atr)) {
                logger.trace("[{}] openLogicalChannel => ATR didn't match. SELECTOR = {}",
                        this.getName(), seSelector);
                selectionHasMatched = false;
            }
            try {
                openChannel = session.openBasicChannel(null);
            } catch (IOException e) {
                e.printStackTrace();
                throw new KeypleIOReaderException("IOException while opening basic channel.");
            } catch (SecurityException e) {
                throw new KeypleChannelStateException("Error while opening basic channel, SE_SELECTOR = " +  seSelector.toString(), e.getCause());
            }

            if (openChannel == null) {
                throw new KeypleIOReaderException("Failed to open a basic channel.");
            }
        }

        /**
         * Perform application selection if requested and if ATR filtering matched or was not
         * requested
         */
        if (selectionHasMatched && seSelector.getAidSelector() != null) {
            final SeSelector.AidSelector aidSelector = seSelector.getAidSelector();
            final byte aid[] = aidSelector.getAidToSelect();
            if (aid == null) {
                throw new IllegalArgumentException("AID must not be null for an AidSelector.");
            }
            if (logger.isTraceEnabled()) {
                logger.trace("[{}] openLogicalChannel => Select Application with AID = {}",
                        this.getName(), ByteArrayUtils.toHex(aid));
            }
            try {
                openChannel = session.openLogicalChannel(aid);
            } catch (IOException e) {
                e.printStackTrace();
                throw new KeypleIOReaderException("IOException while opening logical channel.");
            } catch (NoSuchElementException e) {
                throw new KeypleApplicationSelectionException(
                        "Error while selecting application : " + ByteArrayUtils.toHex(aid), e);
            } catch (SecurityException e) {
                throw new KeypleChannelStateException("Error while opening logical channel, aid :" + ByteArrayUtils.toHex(aid), e.getCause());
            }

            if (openChannel == null) {
                throw new KeypleIOReaderException("Failed to open a logical channel.");
            }

            /* get the FCI and build an ApduResponse */
            fciResponse = new ApduResponse(openChannel.getSelectResponse(), aidSelector.getSuccessfulSelectionStatusCodes());

            if (!fciResponse.isSuccessful()) {
                logger.trace(
                        "[{}] openLogicalChannel => Application Selection failed. SELECTOR = {}",
                        this.getName(), aidSelector);
            }
            /*
             * The ATR filtering matched or was not requested. The selection status is determined by
             * the answer to the select application command.
             */
            selectionStatus = new SelectionStatus(new AnswerToReset(atr), fciResponse,
                    fciResponse.isSuccessful());
        } else {
            /*
             * The ATR filtering didn't match or no AidSelector was provided. The selection status
             * is determined by the ATR filtering.
             */
            selectionStatus = new SelectionStatus(new AnswerToReset(atr),
                    new ApduResponse(null, null), selectionHasMatched);
        }
        return selectionStatus;
    }

    @Override
    public boolean isPhysicalChannelOpen() {
        if(session != null && !session.isClosed()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void openPhysicalChannel() throws KeypleChannelStateException {
        try {
            session = omapiReader.openSession();
        } catch (IOException e) {
            e.printStackTrace();
            throw new KeypleChannelStateException("IOException while opening physical channel.");
        }
    }


    /**
     * Close session see {@link Session#close()}
     * 
     * @throws KeypleReaderException
     */
    @Override
    protected void closePhysicalChannel() {
        // close physical channel if exists
        if (openChannel != null) {
            openChannel.getSession().close();
            openChannel = null;
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
