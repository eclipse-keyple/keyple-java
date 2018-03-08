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
 * Created by ixxi on 15/01/2018.
 */

public class AndroidNfcReader extends ObservableReader implements NfcAdapter.ReaderCallback {

    private final String mName = "AndroidNfcReader";
    public static final String TAG = "AndroidNfcReader";
    public static String ISO_DEP = "android.nfc.tech.IsoDep";

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
     */
    public static AndroidNfcReader getInstance() {
        return SingletonHolder.instance;
    }


    @Override
    public String getName() {
        return this.mName;
    }



    @Override
    public void onTagDiscovered(Tag tag) {

        Log.i(TAG, "Received Tag Discovered event " + tag.getId());

        processTag(tag);
    }


    @Override
    public boolean isSEPresent() throws IOReaderException {// TODO
        return false;
    }

    @Override
    public SeResponse transmit(SeRequest seApplicationRequest) {
        Log.i(TAG, "Calling transmit on Android NFC Reader");
        Log.d(TAG, "Size of APDU Requests : "
                + String.valueOf(seApplicationRequest.getApduRequests().size()));
        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();
        ApduResponse fciResponse = null;

        // Check if SE is present
        // Log.d(TAG, "Secure Element (tag) is present");

        // Checking of the presence of the AID request in requests group

        try {


            if ((seApplicationRequest.getAidToSelect() != null)
                    && (mAidCurrentlySelected == null)) {
                fciResponse = this.connectApplication(seApplicationRequest.getAidToSelect());
            }


            for (ApduRequest apduRequest : seApplicationRequest.getApduRequests()) {
                Log.i(TAG, getName() + " : Sending : "
                        + Tools.byteArrayToSHex(apduRequest.getBytes()));

                IsoDepResponse res = sendAPDUCommand(apduRequest.getBuffer());


                Log.i(TAG, getName() + " : Recept : " + res.getResponseBuffer().toString()
                        + " statusCode : " + res.getStatusWord().toString());

                apduResponses.add(new ApduResponse(res.getResponseBuffer(), true));

            }
        } catch (IOException e) {
            Log.e(TAG, "Error executing command");
            e.printStackTrace();
            apduResponses.add(new ApduResponse(ByteBuffer.allocate(0), false));
        }

        if (!seApplicationRequest.askKeepChannelOpen()) {
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

        byte[] connectDataOut = null;

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

            IsoDepResponse res = sendAPDUCommand(command);

            Log.i(TAG, getName() + " : Recept : " + Tools.byteArrayToSHex(connectDataOut));
            ApduResponse fciResponse = new ApduResponse(res.getResponseBuffer(), true);

            mAidCurrentlySelected = aid;
            return fciResponse;
        } else {
            return null;
        }
    }

    /**
     * Process data from NFC Intent
     *
     * @param intent
     */
    public void processIntent(Intent intent) {

        // Inform that a nfc tag has been detected
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        this.processTag(tag);

    }


    /**
     * Process data from the scanned NFC tag
     *
     * @param tag
     */
    public void processTag(Tag tag) {

        Log.d(TAG, "Processing Tag");

        currentTag = tag;

        try {
            connectISODEP();
            notifyObservers(new ReaderEvent(AndroidNfcReader.getInstance(),
                    ReaderEvent.EventType.SE_INSERTED));
        } catch (IOException e) {
            Log.e(TAG, "Error while connecting to Tag " + tag.getId());
            e.printStackTrace();
        }


    }


    /**
     * Logical canal with the tag/card
     *
     */
    private void connectISODEP() throws IOException {
        Log.i(TAG, "Connecting to tag as a Iso Dep");

        isoDepTag = null;
        isoDepTag = IsoDep.get(currentTag);

        isoDepTag.connect();
        Log.i(TAG, "Iso Dep tag connected successfully");

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

                Log.i(TAG, "Disconnected tag");
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
    private IsoDepResponse sendAPDUCommand(ByteBuffer command) throws IOException {
        IsoDepResponse res = null;
        // Initialization

        long commandLenght = command.limit();
        Log.d(TAG, "Data Length to be sent to ISODEP : " + commandLenght);
        Log.d(TAG, "Max data possible to be transceived by IsoDep : "
                + isoDepTag.getMaxTransceiveLength());


        Log.d(TAG, "Sending data to  tag ");
        byte[] data = ByteBufferUtils.toBytes(command);
        byte[] dataOut = isoDepTag.transceive(data);
        res = new IsoDepResponse(dataOut);



        return res;
    }


    /**
     * * responseBuffer received response lenDataOut length of the response statusWord status word
     * of the response
     */
    private class IsoDepResponse {
        ByteBuffer responseBuffer;
        int lenDataOut;
        ByteBuffer statusWord;


        public IsoDepResponse(byte[] res) {
            this.statusWord = ByteBuffer.allocate(2);
            this.lenDataOut = res.length;
            this.responseBuffer = ByteBuffer.wrap(res);

            this.statusWord.put(responseBuffer.get(lenDataOut - 2));
            this.statusWord.put(responseBuffer.get(lenDataOut - 1));

        }

        public ByteBuffer getStatusWord() {
            return statusWord;
        }

        public ByteBuffer getResponseBuffer() {
            return responseBuffer;
        }

    }
}
