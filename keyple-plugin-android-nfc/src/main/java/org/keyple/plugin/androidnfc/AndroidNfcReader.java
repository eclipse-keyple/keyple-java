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
import org.keyple.seproxy.SeResponse;
import org.keyple.seproxy.exceptions.IOReaderException;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;


/**
 * {@link org.keyple.seproxy.ProxyReader} implementation for the communication
 * with the ISO Card by the @{@link NfcAdapter} of the Android system
 */
public class AndroidNfcReader extends ObservableReader implements NfcAdapter.ReaderCallback {

    private final String mName = "AndroidNfcReader";
    public static final String TAG = "AndroidNfcReader";

    //
    private static IsoDep isoDepTag;
    private static Tag currentTag;

    private ByteBuffer mAidCurrentlySelected = null;

    /**
     * Private constructor
     */
    private AndroidNfcReader() {}

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
     * @return singleton instance of @{@link AndroidNfcReader}
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
                + String.valueOf(seApplicationRequest.getApduRequests().size()));
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        ApduResponse fciResponse = null;


        try {

            if (isSEPresent()) {

                // Checking of the presence of the AID request in requests group
                if ((seApplicationRequest.getAidToSelect() != null)
                        && (mAidCurrentlySelected == null)) {
                    fciResponse = this.connectApplication(seApplicationRequest.getAidToSelect());
                }


                for (ApduRequest apduRequest : seApplicationRequest.getApduRequests()) {
                    Log.i(TAG, getName() + " : Sending : "
                            + ByteBufferUtils.toHex(apduRequest.getBuffer()));

                    apduResponses.add(sendAPDUCommand(apduRequest.getBuffer()));

                }
            } else {
                Log.w(TAG, "SE is not present");

            }
        } catch (IOException e) {
            Log.e(TAG, "Error executing command");
            e.printStackTrace();
            apduResponses.add(new ApduResponse(ByteBuffer.allocate(0), false));
        }

        if (!seApplicationRequest.keepChannelOpen()) {
            disconnectISODEP();
        }


        return new SeResponse(false, fciResponse, apduResponses);
    }


    /**
     * method to connect to the card from the terminal
     *
     * @param aid the AID application
     */
    private ApduResponse connectApplication(ByteBuffer aid) throws IOException {

        Log.i(TAG, "Connecting to application");

        if (aid != null) {
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

            mAidCurrentlySelected = aid;
            return sendAPDUCommand(command);

        } else {
            mAidCurrentlySelected = null;
            return null;
        }
    }

    /**
     * Process data from NFC Intent
     * @param intent : Intent resulting from the scan of a compatible tag
     */
    protected void connectTag(Intent intent) {

        // Inform that a nfc tag has been detected
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        this.connectTag(tag);

    }


    /**
     * Process data from the scanned NFC tag
     @param tag: Tag to connect to
     */
    protected void connectTag(Tag tag) {

        Log.d(TAG, "Processing Tag");
        currentTag = tag;

        try {
            connectISODEP();
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
    private void connectISODEP() throws IOException {
        Log.i(TAG, "Connecting to tag as a Iso Dep : " + printTagId());

        isoDepTag = IsoDep.get(currentTag);
        isoDepTag.connect();

        Log.i(TAG, "Iso Dep tag connected successfully : " + printTagId());

    };


    /**
     * Disconnect the NFC reader from its tag
     */
    private void disconnectISODEP() {
        try {
            mAidCurrentlySelected = null;

            if (isoDepTag != null) {

                isoDepTag.close();
                this.notifyObservers(new ReaderEvent(this, ReaderEvent.EventType.SE_REMOVAL));

                Log.i(TAG, "Disconnected tag : " + printTagId());
            }

        } catch (IOException e) {
            Log.e(TAG, "Disconnecting error");
        } ;

        isoDepTag = null;
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

        if (isSEPresent()) {

            Log.d(TAG, "Sending data to  tag ");
            byte[] data = ByteBufferUtils.toBytes(command);
            byte[] dataOut = isoDepTag.transceive(data);

            Log.i(TAG, getName() + " : Recept : " + dataOut);
            return new ApduResponse(dataOut, true);

        } else {
            Log.d(TAG, "Can not transmit secure Element is not connected");
            return new ApduResponse(ByteBuffer.allocate(0), false);
        }

    }

    private String printTagId() {
        return currentTag != null ? currentTag.getId() + currentTag.toString() : "null";
    }
}
