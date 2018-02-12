/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.androidnfc;


import static org.keyple.plugin.androidnfc.Tools.byteArrayToSHex;
import java.util.ArrayList;
import java.util.List;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.SeRequest;
import org.keyple.seproxy.SeResponse;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

/**
 * Created by ixxi on 15/01/2018.
 */

protected class AndroidNFCReader implements ProxyReader {
    // must be a singleton

    private static boolean mTagDiscovered;

    // NFC
    private static NfcAdapter mNfcAdapter;
    private static IsoDep mTagISO;
    private static Tag mNfcCurrentTag;
    private String mName;

    public static final String TAG = "AndroidNFCReader";

    /**
     * Private constructor
     */
    private AndroidNFCReader() {}

    /**
     * initialization to determine the reader.
     *
     * @param nfcAdapter the instance of nfc adapter
     * @param name the name of nfc reader
     */
    public void init(NfcAdapter nfcAdapter, String name) {
        this.mNfcAdapter = nfcAdapter;
        this.mName = name;
        this.mTagDiscovered = false;
        Log.i(TAG, "AndroidNFCReader constructor");
    }

    /**
     * Holder of singleton
     */
    private static class SingletonHolder {
        /**
         * Unique instance no-preinitialized
         */
        private final static AndroidNFCReader instance = new AndroidNFCReader();
    }


    /**
     * Access point for the unique instance of singleton
     */
    public static AndroidNFCReader getInstance() {
        return SingletonHolder.instance;
    }


    @Override
    public String getName() {
        return this.mName;
    }

    @Override
    public SeResponse transmit(SeRequest seApplicationRequest) {
        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();
        ApduResponse fciResponse = null;

        byte[] byteArrayDataOut = null;
        List<Byte> listByteArraysDataOut = new ArrayList<Byte>();
        long[] lenListByteArraysDataOut = new long[1];


        byte[] statusWord = new byte[2];

        if (isSEPresent()) {
            if (seApplicationRequest.getAidToSelect() != null) {
                fciResponse = this.connect(seApplicationRequest.getAidToSelect());

            }
            for (ApduRequest apduRequest : seApplicationRequest.getApduRequests()) {
                Log.i(TAG, getName() + " : Sending : " + byteArrayToSHex(apduRequest.getbytes()));
                try {
                    sendReceiveAPDU(apduRequest.getbytes(), apduRequest.getbytes().length,
                            listByteArraysDataOut, lenListByteArraysDataOut, statusWord);

                    Byte[] Bytes =
                            listByteArraysDataOut.toArray(new Byte[listByteArraysDataOut.size()]);
                    byteArrayDataOut = new byte[listByteArraysDataOut.size()];
                    int j = 0;
                    for (Byte b : Bytes)
                        byteArrayDataOut[j++] = b.byteValue();

                    Log.i(TAG,
                            getName() + " : Recept : " + byteArrayToSHex(byteArrayDataOut)
                                    + " statusCode : " + byteArrayToSHex(statusWord)
                                    + ",\n longueur:" + lenListByteArraysDataOut[0]);

                    // getResponse in case 4 type commmand
                    hackCase4AndGetResponse(apduRequest.isCase4(), statusWord,
                            listByteArraysDataOut/* , channel */);

                    apduResponseList.add(new ApduResponse(byteArrayDataOut, true, statusWord));

                } /*
                   * catch (CardException e) { throw new
                   * ChannelStateReaderException(e.getMessage()); }
                   */ catch (NullPointerException e) {
                    Log.e(TAG, getName() + " : Error executing command", e);
                    apduResponseList.add(new ApduResponse(null, false, null));
                    break;
                }
            }

            if (!seApplicationRequest.askKeepChannelOpen()) {
                Log.i(TAG, "disconnect");
                this.disconnect();
            }
        }

        return new SeResponse(false, fciResponse, apduResponseList);
    }

    @Override
    public boolean isSEPresent()/* throws ReaderException */ {
        boolean sePresent = false;
        if (mTagISO != null)
            sePresent = true;
        return sePresent;

    }

    /**
     * GetResponse in the case of type 4 command.
     *
     * @param isCase4 command of type 4
     * @param statusCode code status of the getResponse
     * @param responseData data of the getResponse
     */
    private void hackCase4AndGetResponse(boolean isCase4, byte[] statusCode,
            List<Byte> responseData/*
                                    * , CardChannel channel
                                    */) /* throws CardException */ {


        byte[] hackSW = new byte[2];
        long[] hackLenOUT = new long[1];

        if (isCase4 && statusCode[0] == (byte) 0x61) {
            byte[] command = new byte[5];
            command[0] = (byte) 0x00;
            command[1] = (byte) 0xC0;
            command[2] = (byte) 0x00;
            switch (command[3] = (byte) 0x00) {
            }
            command[4] = statusCode[1];
            Log.i(TAG, " Send GetResponse : " + byteArrayToSHex(command));

            sendReceiveAPDU(command, command.length, responseData, hackLenOUT, statusCode);

        }
    }


    /**
     * method to connect to the card from the terminal
     *
     * @param aid the AID application
     */
    private ApduResponse connect(byte[] aid) /* throws ChannelStateReaderException */ {

        long[] connectLenOut = new long[1];
        byte[] connectDataOut = null;
        byte[] connectStatusWord = new byte[2];
        List<Byte> listByteArrays = new ArrayList<Byte>();

        if (aid != null) {
            // generate select application command
            byte[] command = new byte[aid.length + 5];
            command[0] = (byte) 0x00;
            command[1] = (byte) 0xA4;
            command[2] = (byte) 0x04;
            command[3] = (byte) 0x00;
            command[4] = Byte.decode("" + aid.length);
            System.arraycopy(aid, 0, command, 5, aid.length);
            Log.i(TAG, getName() + " : Send AID : " + Tools.byteArrayToSHex(command));

            sendReceiveAPDU(command, command.length, listByteArrays, connectLenOut,
                    connectStatusWord);
            Byte[] Bytes = listByteArrays.toArray(new Byte[listByteArrays.size()]);
            connectDataOut = new byte[listByteArrays.size()];
            int j = 0;
            for (Byte b : Bytes)
                connectDataOut[j++] = b.byteValue();

            Log.i(TAG, getName() + " : Recept : " + byteArrayToSHex(connectDataOut));
            ApduResponse fciResponse = new ApduResponse(connectDataOut, true, connectStatusWord);// new
                                                                                                 // byte[]
                                                                                                 // {
                                                                                                 // (byte)
                                                                                                 // 0x90,
                                                                                                 // (byte)
                                                                                                 // 0x00
                                                                                                 // });
            return fciResponse;
        }
        return null;
    }

    /**
     * Process data from the scanned NFC tag
     *
     * @param intent
     */
    public static void NewIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            final String action = intent.getAction();
            if (action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
                // Inform that a nfc tag has been detected
                mNfcCurrentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                mTagDiscovered = true;
            }
        }
    }

    /**
     * Search of the tag/card
     *
     * @param bCOM used protocol
     * 
     */
    public boolean searchCard(byte[] bCOM, byte[] dataOut, long[] lenDataOut) {
        boolean bRet = false;

        // Initialization
        bCOM[0] = CSC_Protocol.NoCard;
        lenDataOut[0] = 0;

        // Buffer Cleaning
        for (int i = 0; i < dataOut.length; i++)
            dataOut[i] = 0;

        // Card detection
        if (mTagDiscovered == true) {
            mTagDiscovered = false;
            mTagISO = null;


            String myTagTechno[] = mNfcCurrentTag.getTechList();
            if (myTagTechno[0].matches(String.format("android.nfc.tech.IsoDep")) == true) {
                if (myTagTechno[1]
                        .matches(String.format("android.nfc.tech.MifareClassic")) == true) {
                    if (myTagTechno[2].matches(String.format("android.nfc.tech.NfcA")) == true) {
                        bCOM[0] = CSC_Protocol.ISOA;
                    }
                } else if (myTagTechno[1].matches(String.format("android.nfc.tech.NfcA")) == true) {
                    bCOM[0] = CSC_Protocol.ISOA;
                } else if (myTagTechno[1].matches(String.format("android.nfc.tech.NfcB")) == true) {
                    bCOM[0] = CSC_Protocol.ISOB;
                }

                mTagISO = IsoDep.get(mNfcCurrentTag);
                if (mTagISO == null) {
                    mTagDiscovered = false;
                    Log.e("TAG", "Errors in NFC communication !");
                    return false;
                }

                // CID
                dataOut[(int) lenDataOut[0]++] = 0x01;

                // Serial number length
                dataOut[(int) lenDataOut[0]++] = (byte) mNfcCurrentTag.getId().length;

                // Serial number
                byte[] buffUID = mNfcCurrentTag.getId();
                for (int i = 0; i < mNfcCurrentTag.getId().length; i++)
                    dataOut[(int) lenDataOut[0]++] = buffUID[i];

                // Historical bytes length
                byte histLength = 0;
                try {
                    histLength = (byte) mTagISO.getHistoricalBytes().length;
                } catch (Exception e) {
                } ;
                dataOut[(int) lenDataOut[0]++] = (byte) (histLength + 8);
                lenDataOut[0] += 8;

                // Historical bytes
                byte[] hist = mTagISO.getHistoricalBytes();
                for (int i = 0; i < histLength; i++)
                    dataOut[(int) lenDataOut[0]++] = hist[i];
            } else {
                Log.i(TAG, "The protocol " + myTagTechno[0] + " has to be implemented.");
                bRet = false;
            }

            // Connection with the tag
            if (mTagISO != null) {
                try {
                    mTagISO.connect();
                    Log.i(TAG, "Connected tag");
                    bRet = true;
                } catch (Exception e) {
                    Log.e(TAG, "Failed to communicate with NFC tag:"
                            + (e.getMessage() != null ? e.getMessage() : "-"));
                } ;
            }

        }

        if (bCOM[0] == CSC_Protocol.NoCard) {
            bRet = false;
        }

        // En cas d'erreur ou de non détection
        if (bRet == false) {
            mTagDiscovered = false;
            bCOM[0] = CSC_Protocol.NoCard;
            lenDataOut[0] = 0;
            mTagISO = null;
        }
        return bRet;
    }

    /**
     * Disconnect the NFC reader
     */
    public void disconnect() {
        try {
            if (mTagISO != null)
                mTagISO.close();

        } catch (Exception e) {
            Log.e(TAG, "Disconnecting error");
        } ;
        mTagISO = null;
    }

    /**
     * Exchanges of APDU cmds with the ISO tag/card
     *
     * @param dataIn command to send
     * @param lenDataIn length of the command
     * @param listByteDataOut received response
     * @param lenDataOut length of the response
     * @param statusWord status word of the response
     */
    public boolean sendReceiveAPDU(byte[] dataIn, long lenDataIn, List<Byte> listByteDataOut,
            long[] lenDataOut, byte[] statusWord) {
        int i;
        boolean bRet = false;

        // Initialization
        lenDataOut[0] = 0;
        statusWord[0] = 0;


        if (mTagISO != null) {
            if (mTagISO.getMaxTransceiveLength() < lenDataIn) {
                bRet = false;
            } else {
                lenDataOut[0] = 0;
                byte[] dataBuffIn = new byte[(int) lenDataIn];
                for (i = 0; i < lenDataIn; i++)
                    dataBuffIn[i] = (byte) dataIn[i];

                try {
                    byte[] recv = mTagISO.transceive(dataBuffIn);
                    lenDataOut[0] = recv.length;
                    for (i = 0; i < lenDataOut[0]; i++)
                        listByteDataOut.add(recv[i]);
                    bRet = true;
                } catch (Exception e) {
                    Log.e(TAG, "Error during SendReceiveAPDU: " + e.getMessage());
                    bRet = false;
                }
            }
        } else
            bRet = false;

        if ((bRet == true) && (lenDataOut[0] >= (long) 2)) {
            statusWord[0] = listByteDataOut.get((int) lenDataOut[0] - 2);
            statusWord[1] = listByteDataOut.get((int) lenDataOut[0] - 1);

        }
        return bRet;
    }
}
