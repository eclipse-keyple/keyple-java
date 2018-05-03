/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.androidnfc;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ByteBufferUtils;
import org.keyple.seproxy.ObservableReader;
import org.keyple.seproxy.ReaderEvent;
import org.keyple.seproxy.SeRequest;
import org.keyple.seproxy.SeRequestElement;
import org.keyple.seproxy.SeResponse;
import org.keyple.seproxy.SeResponseElement;
import org.keyple.seproxy.exceptions.IOReaderException;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;


/**
 * Implementation of @{@link org.keyple.seproxy.ProxyReader} for the communication with the ISO Card
 * though Android @{@link NfcAdapter}
 */
public class AndroidNfcReader extends ObservableReader implements NfcAdapter.ReaderCallback {

    private final String mName = "AndroidNfcReader";
    private static final String TAG = "AndroidNfcReader";

    //
    private static IsoDep isoDepTag;
    private static Tag currentTag;

    private final List<ByteBuffer> openChannels = new ArrayList<ByteBuffer>();

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
        return this.mName;
    }


    /**
     * Callback function invoked when the @{@link NfcAdapter} detects a @{@link Tag}
     * 
     * @param tag : detected tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {

        Log.i(TAG, "Received Tag Discovered event " + printTagId());
        connectTag(tag);
    }


    @Override
    public boolean isSEPresent() throws IOReaderException {
        return isoDepTag != null && isoDepTag.isConnected();
    }

    /**
     * Transmit {@link SeRequest} to the connected Tag
     * 
     * @param seApplicationRequest the se application request
     * @return {@link SeResponse} : response from the transmitted request
     */
    @Override
    public SeResponse transmit(SeRequest seApplicationRequest) {
        Log.i(TAG, "Calling transmit on Android NFC Reader");
        Log.d(TAG, "Size of APDU Requests : "
                + String.valueOf(seApplicationRequest.getElements().size()));

        List<SeResponseElement> seResponseElements = new ArrayList<SeResponseElement>();

        try {


            if (isSEPresent()) {

                for (SeRequestElement seRequestElement : seApplicationRequest.getElements()) {

                    // init
                    List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
                    ApduResponse fciResponse = null;
                    Boolean channelPreviouslyOpen = true;

                    try {

                        // Checking of the presence of the AID request in requests group
                        ByteBuffer aid = seRequestElement.getAidToSelect();

                        // Open the application channel if not open yet
                        if (!openChannels.contains(aid)) {
                            Log.i(TAG, "Connecting to application : " + aid);
                            channelPreviouslyOpen = false;
                            fciResponse =
                                    this.connectApplication(seRequestElement.getAidToSelect());
                        }

                        // Send all apduRequest
                        for (ApduRequest apduRequest : seRequestElement.getApduRequests()) {
                            Log.i(TAG, getName() + " : Sending : "
                                    + ByteBufferUtils.toHex(apduRequest.getBuffer()));
                            apduResponses.add(sendAPDUCommand(apduRequest.getBuffer()));
                        }

                        // Close channel if asked
                        if (!seRequestElement.keepChannelOpen()) {
                            disconnectApplication(seRequestElement.getAidToSelect());
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Error executing command");
                        e.printStackTrace();
                        apduResponses.add(null);// add empty response
                    }


                    // Add ResponseElements to global SeResponse
                    SeResponseElement out = new SeResponseElement(channelPreviouslyOpen,
                            fciResponse, apduResponses);
                    seResponseElements.add(out);

                }

            } else {
                Log.w(TAG, "SE is not present");

            }
        } catch (IOException e) {
            Log.e(TAG, "Error while reading SE");
            List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
            apduResponses.add(new ApduResponse(ByteBuffer.allocate(0), false));
            SeResponseElement out = new SeResponseElement(false, null, apduResponses);
            seResponseElements.add(out);

        }

        return new SeResponse(seResponseElements);
    }


    /**
     * method to connect to the card from the terminal
     *
     * @param aid the AID application
     */
    private ApduResponse connectApplication(ByteBuffer aid) throws IOException {

        Log.i(TAG, "Connecting to application");
        Log.i(TAG, "AID limit :" + aid.limit());

        ByteBuffer command = ByteBuffer.allocate(aid.limit() + 6);
        command.put((byte) 0x00);
        command.put((byte) 0xA4);
        command.put((byte) 0x04);
        command.put((byte) 0x00);
        command.put((byte) aid.limit());
        command.put(aid);
        command.put((byte) 0x00);
        command.position(0);
        Log.i(TAG, " : Selecting AID " + ByteBufferUtils.toHex(aid));

        // mAidCurrentlySelected = aid;
        openChannels.add(aid);
        return sendAPDUCommand(command);


    }

    /**
     * Process data from NFC Intent
     *
     * @param intent : Intent received and filter by xml tech_list
     */
    protected void connectTag(Intent intent) {

        // Extract Tag from Intent
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        this.connectTag(tag);
    }


    /**
     * Process data from the scanned NFC tag
     */
    protected void connectTag(Tag tag) {

        Log.d(TAG, "Processing Tag");
        currentTag = tag;

        try {
            connectTag();
            notifyObservers(new ReaderEvent(AndroidNfcReader.getInstance(),
                    ReaderEvent.EventType.SE_INSERTED));

        } catch (IOException e) {
            Log.e(TAG, "Error while connecting to Tag ");
            e.printStackTrace();
        }
    }


    /**
     * Logical canal with the tag/card
     *
     */
    private void connectTag() throws IOException {
        Log.i(TAG, "Connecting to tag as a Iso Dep : " + printTagId());

        isoDepTag = IsoDep.get(currentTag);
        isoDepTag.connect();

        Log.i(TAG, "Iso Dep tag connected successfully : " + printTagId());

    }


    /**
     * Disconnect the NFC reader from its tag (physical disconnect)
     */
    private void disconnectTag() {
        try {

            if (isoDepTag != null) {

                isoDepTag.close();
                this.notifyObservers(new ReaderEvent(this, ReaderEvent.EventType.SE_REMOVAL));

                Log.i(TAG, "Disconnected tag : " + printTagId());
            }

        } catch (IOException e) {
            Log.e(TAG, "Disconnecting error");
        }

        isoDepTag = null;
    }

    /**
     * Disconnect from the application
     */
    private void disconnectApplication(ByteBuffer aid) {

        openChannels.remove(aid);

    }

    /**
     * Exchanges of APDU cmds with the ISO tag/card
     *
     * @param command command to send
     * 
     */
    private ApduResponse sendAPDUCommand(ByteBuffer command) throws IOException {
        // Initialization
        long commandLenght = command.limit();
        Log.d(TAG, "Data Length to be sent to ISODEP : " + commandLenght);
        Log.d(TAG, "Max data possible to be transceived by IsoDep : "
                + isoDepTag.getMaxTransceiveLength());

        Log.d(TAG, "Sending data to  tag ");
        byte[] data = ByteBufferUtils.toBytes(command);
        byte[] dataOut = isoDepTag.transceive(data);

        Log.i(TAG, getName() + " : Recept : " + dataOut);
        return new ApduResponse(dataOut, true);


    }

    private String printTagId() {
        return currentTag != null ? currentTag.getId() + currentTag.toString() : "null";
    }
}
