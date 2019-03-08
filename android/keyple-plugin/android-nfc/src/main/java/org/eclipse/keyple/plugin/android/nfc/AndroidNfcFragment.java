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

import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Add this {@link Fragment} to the Android application Activity {@link Fragment#onCreate(Bundle)}
 * method to enable NFC
 *
 * getFragmentManager().beginTransaction() .add(AndroidNfcFragment.newInstance(),
 * "myFragmentId").commit();
 *
 * By default the plugin only listens to events when your application activity is in the foreground.
 * To activate NFC events while you application is not in the foreground, add the following
 * statements to your activity definition in AndroidManifest.xml
 *
 * <intent-filter> <action android:name="android.nfc.action.TECH_DISCOVERED" /> </intent-filter>
 * <meta-data android:name="android.nfc.action.TECH_DISCOVERED" android:resource="@xml/tech_list" />
 *
 * Create a xml/tech_list.xml file in your res folder with the following content <?xml version="1.0"
 * encoding="utf-8"?> <resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2"> <tech-list>
 * <tech>android.nfc.tech.IsoDep</tech> <tech>android.nfc.tech.NfcA</tech> </tech-list> </resources>
 */
public class AndroidNfcFragment extends Fragment {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidNfcFragment.class);


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
        LOG.debug("onCreate");

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

        if (nfcAdapter == null) {
            LOG.warn("Your device does not support NFC");
        } else {
            if (!nfcAdapter.isEnabled()) {
                LOG.warn("PLease enable NFC to communicate with NFC Elements");
            }
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
     * Enable the Nfc Adapter in reader mode. While the fragment is active NFCAdapter will detect
     * card of type @NFCAdapter.FLAG_READER_NFC_B Android Reader is called to process the
     * communication with the ISO Card Fragment process Intent of ACTION_TECH_DISCOVERED if presents
     */
    @Override
    public void onResume() {
        super.onResume();

        // Process NFC intent i.e ACTION_TECH_DISCOVERED are processed by the reader. Many Intents
        // can be received by the activity, only ACTION_TECH_DISCOVERED are processed
        Intent intent = getActivity().getIntent();
        LOG.debug("Intent type : " + intent.getAction());

        // Enable Reader Mode for NFC Adapter
        try {

            if (intent.getAction() != null
                    && intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED)) {
                LOG.debug("Handle ACTION TECH intent");

                ((AndroidNfcReader) AndroidNfcPlugin.getInstance().getReaders().first())
                        .processIntent(intent);

            } else {
                LOG.debug("Intent is not of type ACTION TECH, do not process");

            }

            ((AndroidNfcReader) SeProxyService.getInstance().getPlugin(AndroidNfcPlugin.PLUGIN_NAME).getReaders()
                    .first()).enableNFCReaderMode(getActivity());

        } catch (KeypleReaderException e) {
            e.printStackTrace();
            LOG.error("KeypleReaders are not ready");
        } catch (KeyplePluginNotFoundException e) {
            e.printStackTrace();
            LOG.error("NFC Plugin not found");
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        LOG.info("on Pause Fragment - Stopping Read Write Mode");

        try {
            // Disable Reader Mode for NFC Adapter
            ((AndroidNfcReader) SeProxyService.getInstance().getPlugins().first().getReaders()
                    .first()).disableNFCReaderMode(getActivity());

        } catch (KeypleReaderException e) {
            e.printStackTrace();
            LOG.error("NFC Reader is not ready");
        }
    }


}
