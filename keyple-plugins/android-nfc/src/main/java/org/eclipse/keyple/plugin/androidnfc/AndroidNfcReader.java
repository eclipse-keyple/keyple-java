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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.ChannelStateReaderException;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.exception.InvalidMessageException;
import org.eclipse.keyple.seproxy.plugin.AbstractLocalReader;
import org.eclipse.keyple.util.ByteBufferUtils;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.TagTechnology;
import android.os.Bundle;
import android.util.Log;


/**
 * Implementation of {@link org.eclipse.keyple.seproxy.ProxyReader} for the communication with the
 * NFC Tag though Android {@link NfcAdapter}
 */
public class AndroidNfcReader extends AbstractLocalReader implements NfcAdapter.ReaderCallback {

    private static final String TAG = "AndroidNfcReader";

    //Android NFC Adapter
    private NfcAdapter nfcAdapter;
    private Activity activity;

    // keep state between session if required
    private TagProxy tagProxy;

    /**
     * Private constructor
     */
    private AndroidNfcReader() {
        Log.i(TAG, "Instanciate singleton NFC Reader");
    }

    /**
     * Holder of singleton
     */
    private static class SingletonHolder {
        /**
         * Unique instance no-preinitialized
         */
        private final static AndroidNfcReader instance = new AndroidNfcReader();
    }

    /**
     * Access point for the unique instance of singleton
     */
    protected static AndroidNfcReader getInstance() {
        return SingletonHolder.instance;
    }

    @Override
    public String getName() {
        return "AndroidNfcReader";
    }

    @Override
    public Map<String, String> getParameters() {
        return new HashMap<String, String>();
    }

    @Override
    public void setParameter(String key, String value) throws IOException {
        Log.w(TAG, "AndroidNfcReader does not support parameters");
    }

    /**
     * Callback function invoked by @{@link NfcAdapter} when a @{@link Tag} is discovered A
     * TagTransciever is created based on the Tag technology see
     * {@link TagProxy#getTagProxy(Tag)} Do not call this function directly.
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
        return tagProxy  != null && tagProxy.isConnected();
    }

    private void openPhysicalChannel() throws IOReaderException {

        if (!isSePresent()) {
            try {
                tagProxy.connect();
                Log.i(TAG, "Tag connected successfully : " + printTagId());

            } catch (IOException e) {
                Log.e(TAG, "Error while connecting to Tag ");
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Tag is already connected to : " + printTagId());
        }
    }

    @Override
    public ByteBuffer openLogicalChannelAndSelect(ByteBuffer aid) throws IOReaderException {
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

        if (aid != null) {
            Log.i(TAG,"Connecting to card - aid : " + ByteBufferUtils.toHex(aid));
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
                return fciResponse.getBuffer();

            } catch (ChannelStateReaderException e1) {

                throw new ChannelStateReaderException(e1);

            }
        } else {
            return ByteBuffer.wrap(new byte[] {(byte) 0x90, 0x00});
        }
    }

    @Override
    public void closePhysicalChannel() throws IOReaderException {
        try {
            if (tagProxy  != null) {
                tagProxy .close();
                this.notifyObservers(ReaderEvent.SE_REMOVAL);
                Log.i(TAG, "Disconnected tag : " + printTagId());
            }
        } catch (IOException e) {
            Log.e(TAG, "Disconnecting error");
        }
        tagProxy  = null;
    }


    @Override
    public ByteBuffer transmitApdu(ByteBuffer apduIn) throws ChannelStateReaderException {
        // Initialization
        long commandLenght = apduIn.limit();
        Log.d(TAG, "Data Length to be sent to tag : " + commandLenght);
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
    public boolean protocolFlagMatches(SeProtocol protocolFlag) throws InvalidMessageException {
        return protocolsMap.containsKey(protocolFlag) &&
                protocolsMap.get(protocolFlag).equals(tagProxy.getTech());
    }

    /**
     * Process data from NFC Intent
     *
     * @param intent : Intent received and filterByProtocol by xml tech_list
     */
    protected void processIntent(Intent intent) {

        // Extract Tag from Intent
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        this.onTagDiscovered(tag);
    }

    private String printTagId() {
        return tagProxy != null && tagProxy.getTag() != null
                ? tagProxy.getTag().getId() + tagProxy.getTag().toString()
                : "null";
    }

    protected void enableNFCReaderMode(Activity _activity){
        activity = _activity;
        if(nfcAdapter==null){
            nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        }
        // Reader mode for NFC reader allows to listen to NFC events without the Intent mecanism.
        // It is active only when the activity thus the fragment is active.
        Log.i(TAG, "Enabling Read Write Mode");
        Bundle options = new Bundle();
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 5000);

        // TODO : parametrize this
        nfcAdapter.enableReaderMode(activity,
                this,
                NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B
                        | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                options);
    }

    protected void disableNFCReaderMode(){
        nfcAdapter.disableReaderMode(activity);
    }

}
