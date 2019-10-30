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

import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleChannelControlException;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.state.*;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;


/**
 * Implementation of {@link AndroidNfcReader} based on keyple core abstract classes {@link AbstractObservableLocalReader}
 * and {@link ThreadedWaitForSeRemoval}
 */
final class AndroidNfcReaderImpl extends AbstractThreadedObservableLocalReader
        implements AndroidNfcReader, NfcAdapter.ReaderCallback  {

    private static final Logger logger = LoggerFactory.getLogger(AndroidNfcReaderImpl.class);

    //timeout to wait for se removal
    static private long WAIT_SE_REMOVAL_TIMEOUT = 50000;

    // Android NFC Adapter
    private NfcAdapter nfcAdapter;

    // keep state between session if required
    private TagProxy tagProxy;

    private final Map<String, String> parameters = new HashMap<String, String>();



    /**
     * Private constructor
     */
    AndroidNfcReaderImpl() {
        super(PLUGIN_NAME, READER_NAME);
        logger.info("Init NFC Reader");
        switchState(getInitState());
    }

    /**
     * Holder of singleton
     */
    private static class SingletonHolder {
        final static AndroidNfcReaderImpl instance = new AndroidNfcReaderImpl();
    }

    /**
     * Access point for the unique instance of singleton
     */
    static AndroidNfcReaderImpl getInstance() {
        return SingletonHolder.instance;
    }


    @Override
    protected AbstractObservableState.MonitoringState getInitState() {
        return AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION;
    }

    @Override
    protected Map<AbstractObservableState.MonitoringState, AbstractObservableState> initStates() {

        logger.info("[{}] initStates => setup states : DefaultWaitForStartDetect, " +
                "DefaultWaitForSeInsertion, DefaultWaitForSeProcessing, ThreadedWaitForSeRemoval", this.getName());

        Map<AbstractObservableState.MonitoringState, AbstractObservableState> states =
                new HashMap<AbstractObservableState.MonitoringState, AbstractObservableState>();

        states.put(AbstractObservableState.MonitoringState.WAIT_FOR_START_DETECTION,
                new DefaultWaitForStartDetect(this));
        states.put(AbstractObservableState.MonitoringState.WAIT_FOR_SE_INSERTION,
                new DefaultWaitForSeInsertion(this));
        states.put(AbstractObservableState.MonitoringState.WAIT_FOR_SE_PROCESSING,
                new DefaultWaitForSeProcessing(this));
        states.put(AbstractObservableState.MonitoringState.WAIT_FOR_SE_REMOVAL,
                new ThreadedWaitForSeRemoval(this, WAIT_SE_REMOVAL_TIMEOUT));
        return states;
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
     * Configure NFC Reader AndroidNfcReaderImpl supports the following parameters : FLAG_READER:
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
        logger.info("AndroidNfcReaderImpl supports the following parameters");
        logger.info(READER_NAME,
                "FLAG_READER_SKIP_NDEF_CHECK:\"int\", FLAG_READER_NO_PLATFORM_SOUNDS:\"int\", FLAG_READER_PRESENCE_CHECK_DELAY:\"int\"");

        Boolean correctParameter = (key.equals(AndroidNfcReader.FLAG_READER_SKIP_NDEF_CHECK)
                && value.equals("1") || value.equals("0"))
                || (key.equals(AndroidNfcReader.FLAG_READER_NO_PLATFORM_SOUNDS) && value.equals("1")
                        || value.equals("0"))
                || (key.equals(AndroidNfcReaderImpl.FLAG_READER_PRESENCE_CHECK_DELAY)
                        && Integer.parseInt(value) > -1);


        if (correctParameter) {
            logger.warn("Adding parameter : " + key + " - " + value);
            parameters.put(key, value);
        } else {
            logger.warn("Unrecognized parameter : " + key);
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
        logger.info("Received Tag Discovered event");
        //nfcAdapter.ignore(tag, 1000, onTagRemoved, Handler(Looper.getMainLooper()));

        try {
            tagProxy = TagProxy.getTagProxy(tag);
            onEvent(InternalEvent.SE_INSERTED);

        } catch (KeypleReaderException e) {
            e.printStackTrace();
        }





        /*

        switch (monitoringState) {
            case WAIT_FOR_SE_INSERTION:
            try {
                tagProxy = TagProxy.getTagProxy(tag);
                if (processSeInserted()) {
                    // Note: the notification to the application was made by
                    // processSeInserted
                    // We'll wait for the end of its processing
                    monitoringState = MonitoringState.WAIT_FOR_SE_PROCESSING;
                } else {
                    // An unexpected SE has been detected, we wait for its
                    // removal
                    monitoringState = MonitoringState.WAIT_FOR_SE_REMOVAL;
                }
            } catch (KeypleReaderException e) {
                // print and do nothing
                e.printStackTrace();
                logger.error(e.getLocalizedMessage());
            }
            break;
            case WAIT_FOR_SE_PROCESSING:
            case WAIT_FOR_SE_REMOVAL:
            case WAIT_FOR_START_DETECTION:
                throw new IllegalStateException("Unexpected tag discovered event while being processing a previously discovered tag.");
        }
        */
    }

    /**
     * Notification by the application when the SE has been processed
     * <p>The ChannelControl parameter indicates the action to be taken: continue searching for SEs or stop searching.
     */
//    @Override
//    public final void startRemovalSequence() {
//        logger.info("Notification received from the application. PollingMode = {}", currentPollingMode);
//        onEvent(InternalEvent.SE_PROCESSED);
//
//        /*
//        switch (monitoringState) {
//            case WAIT_FOR_SE_INSERTION:
//                break;
//            case WAIT_FOR_SE_PROCESSING:
//                if(currentPollingMode == PollingMode.REPEATING) {
//                    monitoringState = MonitoringState.WAIT_FOR_SE_REMOVAL;
//                } else {
//                    //
//                    monitoringState = MonitoringState.WAIT_FOR_START_DETECTION;
//                }
//                break;
//            case WAIT_FOR_SE_REMOVAL:
//                break;
//            case WAIT_FOR_START_DETECTION:
//                break;
//        }
//        */
//    }



    /**
     * {@link NfcAdapter} Tag Removal Notification
    @Override
    public void onTagRemoved() {
        logger.info("Received Tag Removed event");
        onEvent(InternalEvent.SE_REMOVED);


//        switch (monitoringState) {
//            case WAIT_FOR_SE_INSERTION:
//                // do nothing, stay in the same state
//                break;
//            case WAIT_FOR_SE_PROCESSING:
//            case WAIT_FOR_SE_REMOVAL:
//                // close channels and notifies the event
//                processSeRemoved();
//                // go back to the "wait for SE insertion" state
//                monitoringState = MonitoringState.WAIT_FOR_SE_INSERTION;
//                break;
//            case WAIT_FOR_START_DETECTION:
//                throw new IllegalStateException("Unexpected tag removed event while the detection is not started.");
//        }

    }
     */




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
        logger.debug("ATR : " + Arrays.toString(atr));
        return atr != null && atr.length > 0 ? atr : null;
    }

    @Override
    protected boolean isPhysicalChannelOpen() {
        return tagProxy != null && tagProxy.isConnected();
    }

    @Override
    protected void openPhysicalChannel() throws KeypleChannelControlException {
        if (!checkSePresence()) {
            try {
                logger.debug("Connect to tag..");
                tagProxy.connect();
                logger.info("Tag connected successfully : " + printTagId());

            } catch (IOException e) {
                logger.error("Error while connecting to Tag ");
                e.printStackTrace();
                throw new KeypleChannelControlException("Error while opening physical channel", e);
            }
        } else {
            logger.info("Tag is already connected to : " + printTagId());
        }
    }

    @Override
    protected void closePhysicalChannel() throws KeypleChannelControlException {
        try {
            if (tagProxy != null) {
                tagProxy.close();
                /*
                done in method AbstractObservableLocalReader#processSeRemoved()
                notifyObservers(new ReaderEvent(PLUGIN_NAME, READER_NAME,
                        ReaderEvent.EventType.SE_REMOVED, null));
                */
                logger.info("Disconnected tag : " + printTagId());
            }else{
                logger.info("Tag is already disconnected");
            }
        } catch (IOException e) {
            logger.error("Disconnecting error");
            throw new KeypleChannelControlException("Error while closing physical channel", e);
        }
        tagProxy = null;
    }

    @Override
    protected byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
        // Initialization
        logger.debug("Send data to card : " + apduIn.length + " bytes");
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
        logger.debug("Receive data from card : " + ByteArrayUtil.toHex(dataOut));
        return dataOut;
    }

    @Override
    protected boolean protocolFlagMatches(SeProtocol protocolFlag) {
        return protocolFlag == null || protocolsMap.containsKey(protocolFlag)
                && protocolsMap.get(protocolFlag).equals(tagProxy.getTech());
    }

    /**
     * Process data from NFC Intent
     *
     * @param intent : Intent received and filterByProtocol by xml tech_list
     */
    @Override
    public void processIntent(Intent intent) {

        // Extract Tag from Intent
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        this.onTagDiscovered(tag);
    }

    @Override
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

        String ndef = parameters.get(AndroidNfcReaderImpl.FLAG_READER_SKIP_NDEF_CHECK);
        String nosounds = parameters.get(AndroidNfcReaderImpl.FLAG_READER_NO_PLATFORM_SOUNDS);

        if (ndef != null && ndef.equals("1")) {
            flags = flags | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
        }

        if (nosounds != null && nosounds.equals("1")) {
            flags = flags | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS;
        }

        // Build flags list for reader mode
        for (SeProtocol seProtocol : this.protocolsMap.keySet()) {
            if (SeCommonProtocols.PROTOCOL_ISO14443_4 == seProtocol) {
                flags = flags | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_NFC_A;

            } else if (seProtocol == SeCommonProtocols.PROTOCOL_MIFARE_UL
                    || seProtocol == SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC) {
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
        if (parameters.containsKey(AndroidNfcReaderImpl.FLAG_READER_PRESENCE_CHECK_DELAY)) {
            Integer delay = Integer
                    .parseInt(parameters.get(AndroidNfcReaderImpl.FLAG_READER_PRESENCE_CHECK_DELAY));
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


        logger.info("Enabling Read Write Mode with flags : " + flags + " and options : "
                + options.toString());

        // Reader mode for NFC reader allows to listen to NFC events without the Intent mechanism.
        // It is active only when the activity thus the fragment is active.
        nfcAdapter.enableReaderMode(activity, this, flags, options);
    }

    void disableNFCReaderMode(Activity activity) {
        nfcAdapter.disableReaderMode(activity);

    }
}
