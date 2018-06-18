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
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.InvalidMessageException;
import org.eclipse.keyple.seproxy.local.AbstractLocalReader;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSettings;
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
 * Configure NFCAdapter Protocols with
 * {@link AndroidNfcReader#addSeProtocolSetting(SeProtocolSettings)} and
 * {@link AndroidNfcReader#setParameter(String, String)}
 *
 *
 */
public class AndroidNfcReader extends AbstractLocalReader implements NfcAdapter.ReaderCallback {

    private static final String TAG = "AndroidNfcReader";

    // Android NFC Adapter
    private NfcAdapter nfcAdapter;

    // keep state between session if required
    private TagProxy tagProxy;

    // flags for NFCAdapter
    private int flags = 0;
    private Bundle options = new Bundle();

    private Map<String, String> parameters = new HashMap<String, String>();

    /**
     * Private constructor
     */
    private AndroidNfcReader() {
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

    @Override
    public String getName() {
        return "AndroidNfcReader";
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
                "FLAG_READER: SKIP_NDEF_CHECK, NO_PLATFORM_SOUNDS, EXTRA_READER_PRESENCE_CHECK_DELAY:\"int\"");

        Boolean correctParameter = true;

        if (key.equals("FLAG_READER")) {
            if (value.equals("SKIP_NDEF_CHECK")) {
                flags = flags | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
            } else if (value.equals("NO_PLATFORM_SOUNDS")) {
                flags = flags | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS;
            } else {
                Log.w(TAG, "Unknown value for parameter : " + key + " " + value);
                correctParameter = false;
            }
        } else if (key.equals("READER_PRESENCE_CHECK_DELAY")) {
            Integer timeout = Integer.parseInt(value);
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, timeout);
        } else {
            Log.w(TAG, "Unknown key for parameter : " + key);
            correctParameter = false;
        }

        if (correctParameter) {
            parameters.put(key, value);
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
            notifyObservers(ReaderEvent.SE_INSERTED);

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

    private void openPhysicalChannel() throws IOReaderException {

        if (!isSePresent()) {
            try {
                tagProxy.connect();
                Log.i(TAG, "Tag connected successfully : " + printTagId());

            } catch (IOException e) {
                Log.e(TAG, "Error while connecting to Tag ");
                e.printStackTrace();
                throw new IOReaderException(e);
            }
        } else {
            Log.i(TAG, "Tag is already connected to : " + printTagId());
        }
    }

    @Override
    protected ByteBuffer[] openLogicalChannelAndSelect(ByteBuffer aid) throws IOReaderException {
        ByteBuffer[] atrAndFci = new ByteBuffer[2];

        if (!isLogicalChannelOpen()) {
            // init of the physical SE channel: if not yet established, opening of a new physical
            // channel
            if (!isSePresent()) {
                openPhysicalChannel();
            }
            if (!isSePresent()) {
                throw new ChannelStateReaderException("Fail to open physical channel.");
            }
        }

        // Contact-less cards do not have an ATR, add a dummy ATR
        atrAndFci[0] = ByteBuffer.wrap(new byte[] {(byte) 0x90, 0x00});

        if (aid != null) {
            Log.i(TAG, "Connecting to card " + ByteBufferUtils.toHex(aid));
            try {
                // build a get response command
                // the actual length expected by the SE in the get response command is handled in
                // transmitApdu
                ByteBuffer selectApplicationCommand = ByteBufferUtils
                        .fromHex("00A40400" + String.format("%02X", (byte) aid.limit())
                                + ByteBufferUtils.toHex(aid) + "00");

                // we use here processApduRequest to manage case 4 hack
                ApduResponse fciResponse =
                        processApduRequest(new ApduRequest(selectApplicationCommand, true));

                // add FCI
                atrAndFci[1] = fciResponse.getBytes();

            } catch (ChannelStateReaderException e1) {

                throw new ChannelStateReaderException(e1);

            }
        }
        return atrAndFci;
    }

    @Override
    protected void closePhysicalChannel() throws IOReaderException {
        try {
            if (tagProxy != null) {
                tagProxy.close();
                this.notifyObservers(ReaderEvent.SE_REMOVAL);
                Log.i(TAG, "Disconnected tag : " + printTagId());
            }
        } catch (IOException e) {
            Log.e(TAG, "Disconnecting error");
        }
        tagProxy = null;
    }


    @Override
    protected ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException {
        // Initialization
        Log.d(TAG, "Data Length to be sent to tag : " + apduIn.limit());
        Log.d(TAG, "Data in : " + ByteBufferUtils.toHex(apduIn));
        byte[] data = ByteBufferUtils.toBytes(apduIn);
        byte[] dataOut = new byte[0];
        try {
            dataOut = tagProxy.transceive(data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ChannelStateReaderException(e);
        }
        ByteBuffer out = ByteBuffer.wrap(dataOut);
        Log.d(TAG, "Data out : " + ByteBufferUtils.toHex(out));
        return out;
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

    private String printTagId() {
        return tagProxy != null && tagProxy.getTag() != null
                ? Arrays.toString(tagProxy.getTag().getId()) + tagProxy.getTag().toString()
                : "null";
    }

    void enableNFCReaderMode(Activity activity) {
        if (nfcAdapter == null) {
            nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        }

        // Build flags list for reader mode
        for (SeProtocol seProtocol : this.protocolsMap.keySet()) {
            if (seProtocol.equals(ContactlessProtocols.PROTOCOL_ISO14443_4)) {
                flags = flags | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_NFC_A;
            } else if (seProtocol.equals(ContactlessProtocols.PROTOCOL_MIFARE_UL)) {
                flags = flags | NfcAdapter.FLAG_READER_NFC_A;
            } else if (seProtocol.equals(ContactlessProtocols.PROTOCOL_MIFARE_CLASSIC)) {
                flags = flags | NfcAdapter.FLAG_READER_NFC_A;
            }
        }

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
