/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.examples.android.omapi;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.example.common.deprecated.AbstractLogicManager;
import org.eclipse.keyple.example.common.deprecated.BasicCardAccessManager;
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiFragment;
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReadersPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
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

/**
 * View for OMAPI Tests
 */
public class OMAPITestFragment extends Fragment
        implements Observable.Observer<AbstractLogicManager.Event> {


    private static final String TAG = OMAPITestFragment.class.getSimpleName();
    private static final String TAG_OMAPI_ANDROID_FRAGMENT =
            "org.eclipse.keyple.plugin.android.omapi.AndroidOmapiFragment";

    private TextView mText;

    public static OMAPITestFragment newInstance() {
        return new OMAPITestFragment();
    }

    /**
     * Initialize SEProxy with Keyple Android OMAPI Plugin
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

        // add OMAPI Fragment to activity in order to communicate with Android Plugin
        Log.d(TAG, "Add Keyple OMAPI Fragment to activity in order "
                + "to communicate with Android OMAPI Plugin");
        AndroidOmapiFragment omapi = AndroidOmapiFragment.newInstance();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragtrans = fm.beginTransaction();
        fragtrans.add(omapi, TAG_OMAPI_ANDROID_FRAGMENT);
        fragtrans.commit();

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
        return view;
    }


    /**
     * Run a basic set of commands to connected Keyple readers
     */
    @Override
    public void onResume() {
        super.onResume();

        BasicCardAccessManager cardAccessManager;

        try {
            List<? extends ProxyReader> readers =
                    SeProxyService.getInstance().getPlugins().get(0).getReaders();

            if (readers.size() < 1) {
                mText.append("\nNo readers setup in Keyple Plugin");
            } else {
                for (ProxyReader reader : readers) {
                    mText.append("\nConnected to reader : " + reader.getName());
                    cardAccessManager = new BasicCardAccessManager();
                    cardAccessManager.setPoReader(reader);
                    cardAccessManager.getTopic().addObserver(this);
                    cardAccessManager.run();
                }
            }

        } catch (IOReaderException e) {
            e.printStackTrace();
        }

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
     * Destroy Keyple OMAPI Fragment
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        // Destroy Keyple OMAPI Fragment
        Log.d(TAG, "Destroy Keyple OMAPI Fragment");
        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentByTag(TAG_OMAPI_ANDROID_FRAGMENT);
        if (f != null) {
            fm.beginTransaction().remove(f).commit();
        }


    }


}
