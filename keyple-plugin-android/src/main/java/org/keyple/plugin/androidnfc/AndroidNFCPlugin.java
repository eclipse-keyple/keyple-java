/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.androidnfc;

import java.util.ArrayList;
import java.util.List;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.ReadersPlugin;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by ixxi on 15/01/2018.
 */

public class AndroidNFCPlugin implements ReadersPlugin {

    public static final byte NOReader = 0x00;
    public static final byte ReaderNFC = 0x01;

    private static boolean mTagDiscovered;
    private static byte mReader = NOReader;
    private static Activity mCurrentActivity;
    private static Context mCurrentContext;

    // NFC
    private static NfcAdapter mNfcAdapter;
    private static PendingIntent mPendingIntent;
    private static IntentFilter[] mNfcIntentFiltersArray;
    private static String[][] mNfcTechListsArray;

    private List<ProxyReader> readers = new ArrayList<ProxyReader>();

    public static final String TAG = "AndroidNFCPlugin";

    @Override
    public String getName() {
        return "AndroidNFCPlugin";
    }

    @Override
    public List<ProxyReader> getReaders() {
        return readers;
    }

    /**
     * Gets the type of reader .
     *
     * @return the type of reader(NO_READER, READER_NFC).
     */
    public byte getTypeReader() {
        return mReader;
    }

    /**
     * the constructor to determine the plugin.
     *
     * @param myActivity the activity associated to the NFC reader
     * @param myContext the context asssociated to the NFC reader
     */
    public AndroidNFCPlugin(Activity myActivity, Context myContext) { // TODO - to define it as a
                                                                      // singleton
        mReader = NOReader;
        mCurrentActivity = myActivity;
        mCurrentContext = myContext;
        mTagDiscovered = false;

        // NFC present on this phone ?
        mNfcAdapter = NfcAdapter.getDefaultAdapter(myContext);

        // NFC Plugin has to have 0 or 1 reader
        if (mNfcAdapter == null) {
            // NFC plugin has no reader
        } else {
            // NFC enabled ?
            if (!mNfcAdapter.isEnabled()) {
                // Open Wireless Settings to activate NFC
                Intent myIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                try {
                    mCurrentActivity.startActivity(myIntent);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }

                // Wait for NFC Enabled...
                while (!mNfcAdapter.isEnabled())
                    Tools.sleepThread(100);
            }

            // NFC - Start waiting for chip
            mPendingIntent = PendingIntent.getActivity(mCurrentContext, 0,
                    new Intent(mCurrentContext, mCurrentActivity.getClass())
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    0);

            final IntentFilter nfcintentFilter =
                    new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
            try {
                nfcintentFilter.addDataType("*/*");
            } catch (IntentFilter.MalformedMimeTypeException e) {
                throw new RuntimeException("fail", e);
            }
            mNfcIntentFiltersArray = new IntentFilter[] {nfcintentFilter};

            // List of techno (IsoDep, Mifare).(List to extend with other techno : Mifare UL...)
            mNfcTechListsArray = new String[][] {new String[] {IsoDep.class.getName()},
                    new String[] {MifareClassic.class.getName()}};

            // Creation of the NFC plugin
            AndroidNFCReader myNFCReaderInstance = AndroidNFCReader.getInstance();
            myNFCReaderInstance.init(mNfcAdapter, "NFCAndroid");
            readers.add(myNFCReaderInstance);

            // NFC Plugin has 1 reader
            mReader = ReaderNFC;
        }

    }

    /**
     * the activity comes to the foreground
     */
    public static void Resume() {
        if (mReader == ReaderNFC) {
            mNfcAdapter.enableForegroundDispatch(mCurrentActivity, mPendingIntent,
                    mNfcIntentFiltersArray, mNfcTechListsArray);
        }
    }

    /**
     * the current activity is left
     */
    public static void Pause() {
        if (mReader == ReaderNFC)
            mNfcAdapter.disableForegroundDispatch(mCurrentActivity);
    }


}
