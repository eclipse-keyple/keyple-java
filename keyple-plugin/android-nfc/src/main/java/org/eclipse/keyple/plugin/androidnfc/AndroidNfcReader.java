/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.androidnfc;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.InvalidMessageException;
import org.eclipse.keyple.seproxy.plugin.AbstractSelectionLocalReader;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;


/**
 * Implementation of {@link org.eclipse.keyple.seproxy.ProxyReader} to communicate with NFC Tag
 * though Android {@link NfcAdapter}
 *
 * Configure NFCAdapter Protocols with {@link AndroidNfcReader#setParameter(String, String)}
 *
 *
 */
public class AndroidNfcReader extends AbstractSelectionLocalReader
        implements NfcAdapter.ReaderCallback {

    static final String TAG = "AndroidNfcReader";
    static final String PLUGIN_NAME = "AndroidNFCPlugin";


    public static final String FLAG_READER_SKIP_NDEF_CHECK = "FLAG_READER_SKIP_NDEF_CHECK";
    public static final String FLAG_READER_NO_PLATFORM_SOUNDS = "FLAG_READER_NO_PLATFORM_SOUNDS";
    public static final String FLAG_READER_PRESENCE_CHECK_DELAY =
            "FLAG_READER_PRESENCE_CHECK_DELAY";


    // Android NFC Adapter
    private NfcAdapter nfcAdapter;

    // keep state between session if required
    private TagProxy tagProxy;

    // flags for NFCAdapter
    // private int flags = 0;
    private final Map<String, String> parameters = new HashMap<String, String>();

    /**
     * Private constructor
     */
    AndroidNfcReader() {
        super(PLUGIN_NAME, TAG);
        Log.i(TAG, "Init singleton NFC Reader");
    }

    /**
     * Holder of singleton
     */
    private static class SingletonHolder {
        private final static AndroidNfcReader instance = new AndroidNfcReader();
    }

    /**
     * Access point for the unique instance of singleton
     */
    static AndroidNfcReader getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Get Reader parameters
     *
     * @return parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }


    /**
     * Configure NFC Reader AndroidNfcReader supports the following parameters : FLAG_READER:
     * SKIP_NDEF_CHECK (skip NDEF check when a smartcard is detected) FLAG_READER:
     * NO_PLATFORM_SOUNDS (disable device sound when nfc smartcard is detected)
     * EXTRA_READER_PRESENCE_CHECK_DELAY: "Int" (see @NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY)
     *
     * @param key the parameter key
     * @param value the parameter value
     * @throws IOException
     */
    @Override
    public void setParameter(String key, String value) throws IOException {
        Log.i(TAG, "AndroidNfcReader supports the following parameters");
        Log.i(TAG,
                "FLAG_READER_SKIP_NDEF_CHECK:\"int\", FLAG_READER_NO_PLATFORM_SOUNDS:\"int\", FLAG_READER_PRESENCE_CHECK_DELAY:\"int\"");

        Boolean correctParameter = (key.equals(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK)
                && value.equals("1") || value.equals("0"))
                || (key.equals(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS) && value.equals("1")
                        || value.equals("0"))
                || (key.equals(AndroidNfcReader.FLAG_READER_PRESENCE_CHECK_DELAY)
                        && Integer.parseInt(value) > -1);


        if (correctParameter) {
            Log.w(TAG, "Adding parameter : " + key + " - " + value);
            parameters.put(key, value);
        } else {

            Log.w(TAG, "Unrecognized parameter : " + key);
            throw new IOException("Unrecognized  parameter");
        }

    }

    /**
     * Callback function invoked by @{@link NfcAdapter} when a @{@link Tag} is discovered A
     * TagTransceiver is created based on the Tag technology see {@link TagProxy#getTagProxy(Tag)}
     * Do not call this function directly.
     *
     * @param tag : detected tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {
        Log.i(TAG, "Received Tag Discovered event");
        try {
            tagProxy = TagProxy.getTagProxy(tag);
            notifyObservers(new ReaderEvent(PLUGIN_NAME, TAG, ReaderEvent.EventType.SE_INSERTED));

        } catch (IOReaderException e) {
            // print and do nothing
            e.printStackTrace();
            Log.e(TAG, e.getLocalizedMessage());
        }

    }

    @Override
    public boolean isSePresent() {
        return tagProxy != null && tagProxy.isConnected();
    }

    @Override
    protected ByteBuffer getATR() {
        byte[] atr = tagProxy.getATR();
        Log.d(TAG, "ATR : " + Arrays.toString(atr));
        return atr != null && atr.length > 0 ? ByteBuffer.wrap(atr) : null;
    }

    @Override
    protected boolean isPhysicalChannelOpen() {

        return tagProxy != null && tagProxy.isConnected();
    }

    @Override
    protected void openPhysicalChannel() throws ChannelStateReaderException {

        if (!isSePresent()) {
            try {
                tagProxy.connect();
                Log.i(TAG, "Tag connected successfully : " + printTagId());

            } catch (IOException e) {
                Log.e(TAG, "Error while connecting to Tag ");
                e.printStackTrace();
                throw new ChannelStateReaderException(e);
            }
        } else {
            Log.i(TAG, "Tag is already connected to : " + printTagId());
        }
    }

    @Override
    protected void closePhysicalChannel() throws ChannelStateReaderException {
        try {
            if (tagProxy != null) {
                tagProxy.close();
                notifyObservers(
                        new ReaderEvent(PLUGIN_NAME, TAG, ReaderEvent.EventType.SE_REMOVAL));
                Log.i(TAG, "Disconnected tag : " + printTagId());
            }
        } catch (IOException e) {
            Log.e(TAG, "Disconnecting error");
            throw new ChannelStateReaderException(e);
        }
        tagProxy = null;
    }


    @Override
    protected ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException {
        // Initialization
        Log.d(TAG, "Data Length to be sent to tag : " + apduIn.limit());
        Log.d(TAG, "Data in : " + ByteBufferUtils.toHex(apduIn));
        byte[] data = ByteBufferUtils.toBytes(apduIn);
        byte[] dataOut;
        try {
            dataOut = tagProxy.transceive(data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ChannelStateReaderException(e);
        }
        Log.d(TAG, "Data out : " + Arrays.toString(dataOut));
        return ByteBuffer.wrap(dataOut);
    }


    @Override
    protected boolean protocolFlagMatches(SeProtocol protocolFlag) throws InvalidMessageException {
        return protocolsMap.containsKey(protocolFlag)
                && protocolsMap.get(protocolFlag).equals(tagProxy.getTech());
    }

    /**
     * Process data from NFC Intent
     *
     * @param intent : Intent received and filterByProtocol by xml tech_list
     */
    void processIntent(Intent intent) {

        // Extract Tag from Intent
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        this.onTagDiscovered(tag);
    }

    String printTagId() {
        return tagProxy != null && tagProxy.getTag() != null
                ? Arrays.toString(tagProxy.getTag().getId()) + tagProxy.getTag().toString()
                : "null";
    }

    /**
     * Build Reader Mode flags Integer from parameters
     * 
     * @return flags Integer
     */
    int getFlags() {

        int flags = 0;

        String ndef = parameters.get(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK);
        String nosounds = parameters.get(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS);

        if (ndef != null && ndef.equals("1")) {
            flags = flags | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
        }

        if (nosounds != null && nosounds.equals("1")) {
            flags = flags | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS;
        }

        // Build flags list for reader mode
        for (SeProtocol seProtocol : this.protocolsMap.keySet()) {
            if (ContactlessProtocols.PROTOCOL_ISO14443_4 == seProtocol) {
                flags = flags | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_NFC_A;

            } else if (seProtocol == ContactlessProtocols.PROTOCOL_MIFARE_UL
                    || seProtocol == ContactlessProtocols.PROTOCOL_MIFARE_CLASSIC) {
                flags = flags | NfcAdapter.FLAG_READER_NFC_A;
            }
        }

        return flags;
    }

    /**
     * Build Reader Mode options Bundle from parameters
     * 
     * @return options
     */
    Bundle getOptions() {
        Bundle options = new Bundle(1);
        if (parameters.containsKey(AndroidNfcReader.FLAG_READER_PRESENCE_CHECK_DELAY)) {
            Integer delay = Integer
                    .parseInt(parameters.get(AndroidNfcReader.FLAG_READER_PRESENCE_CHECK_DELAY));
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, delay.intValue());
        }
        return options;
    }

    void enableNFCReaderMode(Activity activity) {
        if (nfcAdapter == null) {
            nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        }

        int flags = getFlags();

        Bundle options = getOptions();


        Log.i(TAG, "Enabling Read Write Mode with flags : " + flags + " and options : "
                + options.toString());

        // Reader mode for NFC reader allows to listen to NFC events without the Intent mechanism.
        // It is active only when the activity thus the fragment is active.
        nfcAdapter.enableReaderMode(activity, this, flags, options);
    }

    void disableNFCReaderMode(Activity activity) {
        nfcAdapter.disableReaderMode(activity);

    }



}
