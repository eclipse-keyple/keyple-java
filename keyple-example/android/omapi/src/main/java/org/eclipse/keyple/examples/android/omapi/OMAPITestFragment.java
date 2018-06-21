/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.examples.android.omapi;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.calypso.commands.po.PoRevision;
import org.eclipse.keyple.calypso.commands.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.commands.po.builder.UpdateRecordCmdBuild;
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReadersPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.event.AbstractObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.eclipse.keyple.util.Observable;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * View for OMAPI Tests
 */
public class OMAPITestFragment extends Fragment
        implements AbstractObservableReader.Observer<ReaderEvent> {


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
        SortedSet<ReadersPlugin> plugins = new TreeSet<ReadersPlugin>();
        plugins.add(AndroidOmapiPlugin.getInstance());
        seProxyService.setPlugins(plugins);

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

        try {
            SortedSet<? extends ProxyReader> readers =
                    SeProxyService.getInstance().getPlugins().first().getReaders();

            if (readers.size() < 1) {
                mText.append("\nNo readers setup in Keyple Plugin");
            } else {
                for (ProxyReader reader : readers) {
                    if (reader.getName().contains("SIM")) {
                        Log.d(TAG, "Launching tests for reader : " + reader.getName());
                        mText.append("\nLaunching tests for reader : " + reader.getName());
                        runHoplinkSimpleRead();
                    }

                    /*
                     * Exception if(reader.getName().contains("eSE")){ Log.d(TAG,
                     * "Launching tests for reader : " + reader.getName());
                     * mText.append("\nLaunching tests for reader : " + reader.getName());
                     * runHoplinkSimpleRead(); }
                     */
                }
            }

        } catch (IOReaderException e) {
            e.printStackTrace();
        }

    }

    /**
     * Run Hoplink Simple read command
     */
    private void runHoplinkSimpleRead() {
        Log.d(TAG, "Running HopLink Simple Read Tests");
        ProxyReader reader = null;
        try {
            reader = SeProxyService.getInstance().getPlugins().first().getReaders().first();

            String poAid = "A000000291A000000191";
            String t2UsageRecord1_dataFill = "0102030405060708090A0B0C0D0E0F10"
                    + "1112131415161718191A1B1C1D1E1F20" + "2122232425262728292A2B2C2D2E2F30";

            ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                    (byte) 0x01, true, (byte) 0x14, (byte) 0x20);

            ReadRecordsCmdBuild poReadRecordCmd_T2Usage = new ReadRecordsCmdBuild(PoRevision.REV3_1,
                    (byte) 0x01, true, (byte) 0x1A, (byte) 0x30);

            UpdateRecordCmdBuild poUpdateRecordCmd_T2UsageFill =
                    new UpdateRecordCmdBuild(PoRevision.REV3_1, (byte) 0x01, (byte) 0x1A,
                            ByteBufferUtils.fromHex(t2UsageRecord1_dataFill));

            List<ApduRequest> poApduRequestList;

            poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest(),
                    poReadRecordCmd_T2Usage.getApduRequest(),
                    poUpdateRecordCmd_T2UsageFill.getApduRequest());


            SeRequest seRequest = new SeRequest(ByteBufferUtils.fromHex(poAid), poApduRequestList,
                    false, ContactlessProtocols.PROTOCOL_ISO14443_4);


            SeResponseSet seResponseSet = reader.transmit(new SeRequestSet(seRequest));

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mText.append("\n ---- \n");
                    for (SeResponse response : seResponseSet.getResponses()) {
                        if (response != null) {
                            for (ApduResponse apdu : response.getApduResponses()) {
                                mText.append("Response : " + apdu.getStatusCode() + " - "
                                        + ByteBufferUtils.toHex(apdu.getDataOut()));
                                mText.append("\n");
                            }
                        } else {
                            mText.append("Response : null");
                            mText.append("\n");
                        }
                    }
                }
            });

        } catch (IOReaderException e) {
            e.printStackTrace();
        }
    }



    /**
     * Revocation of the Activity
     * from @{@link org.eclipse.keyple.plugin.android.omapi.AndroidOmapiReader} list of observers
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            Log.d(TAG, "Remove task as an observer for ReaderEvents");
            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().first().getReaders().first();
            ((AbstractObservableReader) reader).removeObserver(this);

            // destroy AndroidNFC fragment
            FragmentManager fm = getFragmentManager();
            Fragment f = fm.findFragmentByTag(TAG_OMAPI_ANDROID_FRAGMENT);
            if (f != null) {
                fm.beginTransaction().remove(f).commit();
            }

        } catch (IOReaderException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void update(Observable<ReaderEvent> observable, ReaderEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "New ReaderEvent received : " + event.toString());

                switch (event) {
                    case SE_INSERTED:
                        mText.append("\n ---- \n");
                        mText.append("Connected to SE");
                        break;

                    case SE_REMOVAL:
                        mText.append("\n ---- \n");
                        mText.append("Disconnected from SE");
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
