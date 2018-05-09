/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.androidnfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * UI-less fragment to enable NFC in an Activity Include this fragment to enable NFC Android Keyples
 * capabilities to your Activity
 *
 */
public class AndroidNfcFragment extends Fragment {


    private static final String TAG = AndroidNfcFragment.class.getSimpleName();

    private NfcAdapter nfcAdapter;


    public AndroidNfcFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @return A new instance of fragment NFCFragment.
     */
    public static AndroidNfcFragment newInstance() {
        AndroidNfcFragment fragment = new AndroidNfcFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Checking of the NFC support on the Android device
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Must be set to true
        Log.d(TAG, "onCreate");

        nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

        if (nfcAdapter == null) {
            Log.w(TAG, "Your device does not support NFC");
        }

        if (!nfcAdapter.isEnabled()) {
            Log.w(TAG, "PLease enable NFC to communicate with NFC Elements");
        }


    }

    /**
     * This fragment does not include UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return null;
    }


    /**
     *
     * Enable the Nfc Adapter in reader mode while the fragment is active NFCAdapter will detect ISO
     * card of type @NFCAdapter.FLAG_READER_NFC_B Android Reader is called to process the
     * communication with the ISO Card Fragment process Intent of ACTION_TECH_DISCOVERED if presents
     */
    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume Fragment");

        // if the fragment was created following an intent of NfcAdapter.ACTION_TECH_DISCOVERED TAG
        Intent intent = getActivity().getIntent();
        Log.d(TAG, "Intent : " + intent.getAction());
        if (intent.getAction() != null
                && intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
            // handle intent
            Log.d(TAG, "Handle ACTION TECH intent");

            ((AndroidNfcReader) AndroidNfcPlugin.getInstance().getReaders().get(0))
                    .processIntent(intent);


        } else {
            Log.d(TAG, "Intent is not of type ACTION TECH, do not process");

        }


        Bundle options = new Bundle();
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 5000);



        Log.i(TAG, "Enabling Read Write Mode");

        nfcAdapter.enableReaderMode(getActivity(),
                ((AndroidNfcReader) AndroidNfcPlugin.getInstance().getReaders().get(0)),
                NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, options);



    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "on Pause Fragment - Stopping Read Write Mode");
        nfcAdapter.disableReaderMode(getActivity());

    }


}
