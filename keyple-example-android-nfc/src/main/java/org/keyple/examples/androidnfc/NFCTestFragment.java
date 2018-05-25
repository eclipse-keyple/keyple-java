/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.examples.androidnfc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.example.common.AbstractLogicManager;
import org.keyple.example.common.IsodepCardAccessManager;
import org.keyple.example.common.KeepOpenAbortTestManager;
import org.keyple.example.common.KeepOpenCardTimeoutManager;
import org.keyple.example.common.MifareClassicCardAccessManager;
import org.keyple.example.common.MifareUltralightCardAccessManager;
import org.keyple.example.common.MultiNFCCardAccessManager;
import org.keyple.plugin.androidnfc.AndroidNfcFragment;
import org.keyple.plugin.androidnfc.AndroidNfcPlugin;
import org.keyple.seproxy.AbstractObservableReader;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.ReaderEvent;
import org.keyple.seproxy.ReadersPlugin;
import org.keyple.seproxy.SeProxyService;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.util.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;


public class NFCTestFragment extends Fragment
        implements Observable.Observer<AbstractLogicManager.Event> {


    private static final String TAG = NFCTestFragment.class.getSimpleName();

    private static final String TAG_NFC_ANDROID_FRAGMENT =
            "org.keyple.plugin.androidnfc.AndroidNfcFragment";


    // APDU Commands Test Logic
    private AbstractLogicManager cardAccessManager;

    // UI
    private TextView mText;


    public static NFCTestFragment newInstance() {
        return new NFCTestFragment();
    }

    /**
     * Initialize SEProxy with Keyple Android NFC Plugin Add this view to the list of Observer
     * of @{@link ProxyReader}
     * 
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize SEProxy with Android Plugin
        Log.d(TAG, "Initialize SEProxy with Android Plugin");
        SeProxyService seProxyService = SeProxyService.getInstance();
        List<ReadersPlugin> plugins = new ArrayList<ReadersPlugin>();
        plugins.add(AndroidNfcPlugin.getInstance());
        seProxyService.setPlugins(plugins);

        // add NFC Fragment to activity in order to communicate with Android Plugin
        Log.d(TAG, "Add Keyple NFC Fragment to activity in order to "
                + "communicate with Android Plugin");
        Fragment nfcFragment = AndroidNfcFragment.newInstance();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragtrans = fm.beginTransaction();
        fragtrans.add(nfcFragment, TAG_NFC_ANDROID_FRAGMENT);
        fragtrans.commit();


        try {
            // define task as an observer for ReaderEvents
            Log.d(TAG, "Define this view as an observer for ReaderEvents");
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);
            ((AbstractObservableReader) reader).addObserver(this);

            initIsodepTest();

        } catch (IOReaderException e) {
            e.printStackTrace();
        }
    }


    /**
     * Initialize UI for NFC Test view
     * 
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_nfc_test, container, false);

        mText = view.findViewById(R.id.text);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected

                switch (checkedId) {
                    case R.id.isoDepTest:
                        clearText();
                        mText.setText(
                                "When a smartcard is detected, a set of 3 basic commands will be sent, protocolFlag is set to Isodep");
                        initIsodepTest();
                        break;


                    case R.id.mifareClassicTest:
                        clearText();
                        mText.setText(
                                "When a smartcard is detected, a set of 3 basic commands will be sent, protocolFlag is set to mifareClassic");
                        initMiFareTest();
                        break;

                    case R.id.mifareLightTest:
                        clearText();
                        mText.setText(
                                "When a smartcard is detected, a set of 3 basic commands will be sent, protocolFlag is set to MifareUltralight");
                        initMifareUltralightTest();
                        break;

                    case R.id.multiNFC:
                        clearText();
                        mText.setText(
                                "When a smartcard is detected, 2 sets of 3 basic commands will be sent, protocolFlag is set to Isodep+MifareUltralight");
                        initMultiNFCTest();
                        break;


                    case R.id.keepChannelTimeout:
                        clearText();
                        mText.setText(
                                "When a smartcard is detected,  a set of 3 basic commands will be sent, then 3 seconds later, commands will be sent again");
                        initKeepChannelTimeoutTest();
                        break;

                    case R.id.keepChannelAbortButton:
                        clearText();
                        mText.setText(
                                "When a smartcard is detected,  2 sets of 3 basic commands will be sent, the first one will keep the channel open, the second will be aborted");
                        initKeepChannelAbortTest();
                        break;
                }
            }
        });

        return view;


    }


    /**
     * Init Isodep basic test suite
     */
    private void initIsodepTest() {

        try {

            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);

            cardAccessManager = new IsodepCardAccessManager();
            ((IsodepCardAccessManager) cardAccessManager).setPoReader(reader);

            cardAccessManager.getObservable().addObserver(this);
        } catch (IOReaderException e) {
            e.printStackTrace();
        }

    }


    /**
     * Init miFare basic test suite
     */
    private void initMiFareTest() {

        try {

            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);

            cardAccessManager = new MifareClassicCardAccessManager();
            ((MifareClassicCardAccessManager) cardAccessManager).setPoReader(reader);

            cardAccessManager.getObservable().addObserver(this);
        } catch (IOReaderException e) {
            e.printStackTrace();
        }

    }

    /**
     * Init MifareUltralight basic test suite
     */
    private void initMifareUltralightTest() {

        try {

            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);

            cardAccessManager = new MifareUltralightCardAccessManager();
            ((MifareUltralightCardAccessManager) cardAccessManager).setPoReader(reader);

            cardAccessManager.getObservable().addObserver(this);
        } catch (IOReaderException e) {
            e.printStackTrace();
        }

    }

    /**
     * Init miFare basic test suite
     */
    private void initMultiNFCTest() {

        try {

            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);

            cardAccessManager = new MultiNFCCardAccessManager();
            ((MultiNFCCardAccessManager) cardAccessManager).setPoReader(reader);

            cardAccessManager.getObservable().addObserver(this);
        } catch (IOReaderException e) {
            e.printStackTrace();
        }

    }


    /**
     * Init advanced test suite (keep channel open between two sets of commands)
     */
    private void initKeepChannelTimeoutTest() {

        try {

            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);

            cardAccessManager = new KeepOpenCardTimeoutManager();
            ((KeepOpenCardTimeoutManager) cardAccessManager).setPoReader(reader);

            cardAccessManager.getObservable().addObserver(this);
        } catch (IOReaderException e) {
            e.printStackTrace();
        }

    }

    /**
     * Init advanced test suite (keep channel open into two sets of commands)
     */
    private void initKeepChannelAbortTest() {

        try {

            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);

            cardAccessManager = new KeepOpenAbortTestManager();
            ((KeepOpenAbortTestManager) cardAccessManager).setPoReader(reader);

            cardAccessManager.getObservable().addObserver(this);
        } catch (IOReaderException e) {
            e.printStackTrace();
        }

    }



    /**
     * Observes Card Access when an event is received
     *
     * @param event event received from Card Access Logic Manager
     */
    public void update(AbstractLogicManager.Event event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mText.append("\n ---- \n");
                mText.append(event.getName());
                // mText.append(Arrays.toString(event.getDetails().entrySet().toArray()));
                mText.append(Arrays.toString(event.getDetails().values().toArray()));
            }
        });
    }

    /**
     * Revocation of the Activity from @{@link org.keyple.plugin.androidnfc.AndroidNfcReader} list
     * of observers
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            Log.d(TAG, "Remove task as an observer for ReaderEvents");
            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);
            ((AbstractObservableReader) reader).removeObserver(this);


            // destroy AndroidNFC fragment
            FragmentManager fm = getFragmentManager();
            Fragment f = fm.findFragmentByTag(TAG_NFC_ANDROID_FRAGMENT);
            if (f != null) {
                fm.beginTransaction().remove(f).commit();
            }

        } catch (IOReaderException e) {
            e.printStackTrace();
        }
    }


    private void clearText() {
        mText.setText("");
    }


    @Override
    public void update(Observable<? extends ReaderEvent> observable, ReaderEvent readerEvent) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "New ReaderEvent received : " + readerEvent.getEventType().toString());

                switch (readerEvent.getEventType()) {
                    case SE_INSERTED:
                        mText.append("\n ---- \n");
                        mText.append("Tag detected");
                        try {

                            cardAccessManager.run();

                        } catch (InconsistentCommandException e) {
                            e.printStackTrace();
                        }
                        break;

                    case SE_REMOVAL:
                        mText.append("\n ---- \n");
                        mText.append("Connection closed to tag");
                        break;

                    case IO_ERROR:
                        mText.append("\n ---- \n");
                        mText.setText("Error reading card");
                        break;

                }
            }
        });
    }
}
