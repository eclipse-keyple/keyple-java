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
import org.keyple.plugin.android.omapi.AndroidOmapiFragment;
import org.keyple.plugin.android.omapi.AndroidOmapiPlugin;
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
import android.widget.TextView;


public class OMAPITestFragment extends Fragment
        implements ReaderObserver, Topic.Subscriber<AbstractLogicManager.Event> {


    private static final String TAG = OMAPITestFragment.class.getSimpleName();
    private static final String TAG_OMAPI_ANDROID_FRAGMENT =
            "org.keyple.plugin.android.omapi.AndroidOmapiFragment";

    private TextView mText;
    private AbstractLogicManager cardAccessManager;

    public static OMAPITestFragment newInstance() {
        return new OMAPITestFragment();
    }

    /**
     * Initialize SEProxy with Keyple Android OMAPI Plugin Add this view to the list of Observer
     * of @{@link ProxyReader}
     * 
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize SEProxy with Android Plugin
        Log.d(TAG, "Initialize SEProxy with Android OMAPI Plugin ");
        SeProxyService seProxyService = SeProxyService.getInstance();
        List<ReadersPlugin> plugins = new ArrayList<ReadersPlugin>();
        plugins.add(AndroidOmapiPlugin.getInstance());
        seProxyService.setPlugins(plugins);

        // add OMPAI Fragment to activity in order to communicate with Android Plugin
        Log.d(TAG, "Add Keyple OMAPI Fragment to activity in order "
                + "to communicate with Android OMAPI Plugin");
        AndroidOmapiFragment omapi = AndroidOmapiFragment.newInstance();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragtrans = fm.beginTransaction();
        fragtrans.add(omapi, TAG_OMAPI_ANDROID_FRAGMENT);
        fragtrans.commit();


        try {
            // define this view as an observer for ReaderEvents
            Log.d(TAG, "Define this view as observer for all ReaderEvents");
            for (ProxyReader reader : seProxyService.getPlugins().get(0).getReaders()) {
                ((ObservableReader) reader).addObserver(this);
            }

        } catch (IOReaderException e) {
            e.printStackTrace();
        }


    }

    /**
     * Initialize UI for this view
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


        View view = inflater.inflate(R.layout.fragment_omapi_test, container, false);

        mText = (TextView) view.findViewById(R.id.text);
        mText.setText("Setting up Open Mobile API plugin for Keyple.");

        try {
            for (ProxyReader reader : SeProxyService.getInstance().getPlugins().get(0)
                    .getReaders()) {
                mText.append("\nConnected to reader : " + reader.getName());
            }
        } catch (IOReaderException e) {
            e.printStackTrace();
        }

        return view;


    }


    /**
     * Observes Card Access for an event to be received
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
                        mText.append("SE Inserted");
                        try {
                            cardAccessManager = new BasicCardAccessManager();
                            ((BasicCardAccessManager) cardAccessManager)
                                    .setPoReader(readerEvent.getReader());
                            cardAccessManager.run();

                        } catch (InconsistentCommandException e) {
                            e.printStackTrace();
                        }
                        break;

                    case SE_REMOVAL:
                        mText.append("\n ---- \n");
                        mText.append("SE Removed");
                        break;

                    case IO_ERROR:
                        mText.append("\n ---- \n");
                        mText.setText("Error reading SE");
                        break;

                }
            }
        });
    }



    /**
     * Revocation of this view from @{@link org.keyple.plugin.androidnfc.AndroidNfcReader} list of
     * observers
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            Log.d(TAG, "Remove task as an observer for ReaderEvents");
            SeProxyService seProxyService = SeProxyService.getInstance();
            for (ProxyReader reader : seProxyService.getPlugins().get(0).getReaders()) {
                ((ObservableReader) reader).deleteObserver(this);
            }

            // Destroy Keyple OMAPI Fragment
            Log.d(TAG, "Destroy Keyple OMAPI Fragment");
            FragmentManager fm = getFragmentManager();
            Fragment f = fm.findFragmentByTag(TAG_OMAPI_ANDROID_FRAGMENT);
            if (f != null) {
                fm.beginTransaction().remove(f).commit();
            }

        } catch (IOReaderException e) {
            e.printStackTrace();
        }
    }


}
