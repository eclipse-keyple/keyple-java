/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.examples.androidnfc;

import java.util.ArrayList;
import java.util.List;
import org.keyple.commands.InconsistentCommandException;
import org.keyple.example.common.AbstractLogicManager;
import org.keyple.example.common.BasicCardAccessManager;
import org.keyple.example.common.KeepOpenCardAccessManager;
import org.keyple.plugin.androidnfc.AndroidNfcFragment;
import org.keyple.plugin.androidnfc.AndroidNfcPlugin;
import org.keyple.seproxy.ObservableReader;
import org.keyple.seproxy.ProxyReader;
import org.keyple.seproxy.ReaderEvent;
import org.keyple.seproxy.ReaderObserver;
import org.keyple.seproxy.ReadersPlugin;
import org.keyple.seproxy.SeProxyService;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.util.event.Topic;
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
        implements ReaderObserver, Topic.Subscriber<AbstractLogicManager.Event> {


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
     * Initialize SEProxy with Keyple Android NFC Plugin
     * Add this view to the list of Observer
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
            ((ObservableReader) reader).addObserver(this);

            initBasicCardAccessTest();

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

        mText = (TextView) view.findViewById(R.id.text);

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected

                switch (checkedId) {
                    case R.id.simpleTestButton:
                        Log.i(TAG, "switched to Basic Card Access Test");
                        clearText();
                        mText.setText(
                                "When a smartcard is detected, a set of 3 basic commands will be sent");
                        initBasicCardAccessTest();
                        break;
                    case R.id.keepChannelButton:
                        Log.i(TAG, "switched to Keep Channel Card Access Test");
                        clearText();
                        mText.setText(
                                "When a smartcard is detected,  a set of 3 basic commands will be sent, then 3 seconds later, commands will be sent again");
                        initKeepChannelAccessTest();
                        break;
                }
            }
        });

        return view;


    }

    /**
     * Management of SE insertion event to operate a ticketing processing
     *
     * @param readerEvent : event received from SEProxyService
     */
    @Override
    public void notify(final ReaderEvent readerEvent) {

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

    /**
     * Init basic test suite
     */
    private void initBasicCardAccessTest() {

        try {

            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);

            cardAccessManager = new BasicCardAccessManager();
            ((BasicCardAccessManager) cardAccessManager).setPoReader(reader);

            cardAccessManager.getTopic().addSubscriber(this);
        } catch (IOReaderException e) {
            e.printStackTrace();
        }

    }

    /**
     * Init advanced test suite (keep channel open between two sets of commands)
     */
    private void initKeepChannelAccessTest() {

        try {

            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().get(0).getReaders().get(0);

            cardAccessManager = new KeepOpenCardAccessManager();
            ((KeepOpenCardAccessManager) cardAccessManager).setPoReader(reader);

            cardAccessManager.getTopic().addSubscriber(this);
        } catch (IOReaderException e) {
            e.printStackTrace();
        }

    }


    /**
     * Observes Card Access when an event is received
     *
     * @param event
     */
    public void update(AbstractLogicManager.Event event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mText.append("\n ---- \n");
                mText.append(event.toString());
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
            ((ObservableReader) reader).deleteObserver(this);


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
}
