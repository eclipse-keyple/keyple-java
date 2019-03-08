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
package org.eclipse.keyple.plugin.android.nfc;


import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleChannelStateException;
import org.eclipse.keyple.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractSelectionLocalReader;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.Protocol;
import org.eclipse.keyple.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;


/**
 * Implementation of {@link org.eclipse.keyple.seproxy.SeReader} to communicate with NFC Tag though
 * Android {@link NfcAdapter}
 *
 * Configure NFCAdapter Protocols with {@link AndroidNfcReader#setParameter(String, String)}
 *
 *
 */
public final class AndroidNfcReader extends AbstractSelectionLocalReader
        implements NfcAdapter.ReaderCallback {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidNfcReader.class);

    static final String READER_NAME = "AndroidNfcReader";
    static final String PLUGIN_NAME = "AndroidNfcPlugin";


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
        super(PLUGIN_NAME, READER_NAME);
        LOG.info("Init singleton NFC Reader");
    }

    @Override
    protected void startObservation() {

    }

    @Override
    protected void stopObservation() {

    }

    /**
     * Holder of singleton
     */
    private static class SingletonHolder {
        final static AndroidNfcReader instance = new AndroidNfcReader();
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
    public void setParameter(String key, String value) throws IllegalArgumentException {
        LOG.info("AndroidNfcReader supports the following parameters");
        LOG.info(READER_NAME,
                "FLAG_READER_SKIP_NDEF_CHECK:\"int\", FLAG_READER_NO_PLATFORM_SOUNDS:\"int\", FLAG_READER_PRESENCE_CHECK_DELAY:\"int\"");

        Boolean correctParameter = (key.equals(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK)
                && value.equals("1") || value.equals("0"))
                || (key.equals(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS) && value.equals("1")
                        || value.equals("0"))
                || (key.equals(AndroidNfcReader.FLAG_READER_PRESENCE_CHECK_DELAY)
                        && Integer.parseInt(value) > -1);


        if (correctParameter) {
            LOG.warn("Adding parameter : " + key + " - " + value);
            parameters.put(key, value);
        } else {
            LOG.warn("Unrecognized parameter : " + key);
            throw new IllegalArgumentException("Unrecognized parameter " + key + " : " + value);
        }

    }

    /**
     * The transmission mode is always CONTACTLESS in a NFC reader
     * 
     * @return the current transmission mode
     */
    @Override
    public TransmissionMode getTransmissionMode() {
        return TransmissionMode.CONTACTLESS;
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
        LOG.info("Received Tag Discovered event");
        try {
            tagProxy = TagProxy.getTagProxy(tag);
            cardInserted();
        } catch (KeypleReaderException e) {
            // print and do nothing
            e.printStackTrace();
            LOG.error(e.getLocalizedMessage());
        }
    }

    /**
     *
     * @return true if a SE is present
     */
    @Override
    protected boolean checkSePresence() {
        return tagProxy != null && tagProxy.isConnected();
        // TODO: the right implementation is:
        // return tagProxy != null;
        // To be updated when the onTagRemoved will be available
    }

    @Override
    protected byte[] getATR() {
        byte[] atr = tagProxy.getATR();
        LOG.debug("ATR : " + Arrays.toString(atr));
        return atr != null && atr.length > 0 ? atr : null;
    }

    @Override
    protected boolean isPhysicalChannelOpen() {
        return tagProxy != null && tagProxy.isConnected();
    }

    @Override
    protected void openPhysicalChannel() throws KeypleChannelStateException {
        if (!checkSePresence()) {
            try {
                LOG.debug("Connect to tag..");
                tagProxy.connect();
                LOG.info("Tag connected successfully : " + printTagId());

            } catch (IOException e) {
                LOG.error("Error while connecting to Tag ");
                e.printStackTrace();
                throw new KeypleChannelStateException("Error while opening physical channel", e);
            }
        } else {
            LOG.info("Tag is already connected to : " + printTagId());
        }
    }

    @Override
    protected void closePhysicalChannel() throws KeypleChannelStateException {
        try {
            if (tagProxy != null) {
                tagProxy.close();
                notifyObservers(new ReaderEvent(PLUGIN_NAME, READER_NAME,
                        ReaderEvent.EventType.SE_REMOVAL, null));
                LOG.info("Disconnected tag : " + printTagId());
            }
        } catch (IOException e) {
            LOG.error("Disconnecting error");
            throw new KeypleChannelStateException("Error while closing physical channel", e);
        }
        tagProxy = null;
    }


    @Override
    protected byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
        // Initialization
        LOG.debug("Send " + apduIn.length + " bytes to tag : " + ByteArrayUtils.toHex(apduIn));
        byte[] dataOut = null;
        try {
            dataOut = tagProxy.transceive(apduIn);
            if (dataOut == null || dataOut.length < 2) {
                throw new KeypleIOReaderException(
                        "Error while transmitting APDU, invalid out data buffer");
            }
        } catch (IOException e) {
            throw new KeypleIOReaderException("Error while transmitting APDU", e);
        }
        LOG.debug("Data out : " + ByteArrayUtils.toHex(dataOut));
        return dataOut;
    }


    @Override
    protected boolean protocolFlagMatches(SeProtocol protocolFlag) {
        return protocolFlag.equals(Protocol.ANY) || protocolsMap.containsKey(protocolFlag)
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

    public String printTagId() {

        if (tagProxy != null && tagProxy.getTag() != null) {
            StringBuilder techList = new StringBuilder();

            // build a user friendly TechList
            String[] techs = tagProxy.getTag().getTechList();
            for (int i = 0; i < techs.length; i++) {
                // append a userfriendly Tech
                techList.append(techs[i].replace("android.nfc.tech.", ""));
                // if not last tech, append separator
                if (i + 1 < techs.length)
                    techList.append(", ");
            }



            // build a hexa TechId
            StringBuilder tagId = new StringBuilder();
            for (byte b : tagProxy.getTag().getId()) {
                tagId.append(String.format("%02X ", b));
            }

            return tagId + " - " + techList;
        } else {
            return "no-tag";
        }
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


        LOG.info("Enabling Read Write Mode with flags : " + flags + " and options : "
                + options.toString());

        // Reader mode for NFC reader allows to listen to NFC events without the Intent mechanism.
        // It is active only when the activity thus the fragment is active.
        nfcAdapter.enableReaderMode(activity, this, flags, options);
    }

    void disableNFCReaderMode(Activity activity) {
        nfcAdapter.disableReaderMode(activity);

    }
}
