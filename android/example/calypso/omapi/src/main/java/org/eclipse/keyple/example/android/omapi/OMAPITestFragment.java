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
package org.eclipse.keyple.example.android.omapi;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.UpdateRecordCmdBuild;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPlugin;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.exception.KeyplePluginNotFoundException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.message.ApduRequest;
import org.eclipse.keyple.seproxy.message.ApduResponse;
import org.eclipse.keyple.seproxy.message.ProxyReader;
import org.eclipse.keyple.seproxy.message.SeRequest;
import org.eclipse.keyple.seproxy.message.SeRequestSet;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.message.SeResponseSet;
import org.eclipse.keyple.seproxy.protocol.ContactsProtocols;
import org.eclipse.keyple.util.ByteArrayUtils;
import android.app.Fragment;
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
public class OMAPITestFragment extends Fragment {


    private static final String TAG = OMAPITestFragment.class.getSimpleName();

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
        SortedSet<ReaderPlugin> plugins = new TreeSet<ReaderPlugin>();
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
            SortedSet<? extends SeReader> readers =
                    SeProxyService.getInstance().getPlugin(AndroidOmapiPlugin.PLUGIN_NAME).getReaders();

            if (readers == null || readers.size() < 1) {
                mText.append("\nNo readers found in OMAPI Keyple Plugin");
                mText.append("\nTry to reload..");
            } else {
                for (SeReader aReader : readers) {
                    Log.d(TAG, "Launching tests for reader : " + aReader.getName());
                    runHoplinkSimpleRead(aReader);
                }

            }

        } catch (KeypleReaderException | KeyplePluginNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Run Hoplink Simple read command
     */
    private void runHoplinkSimpleRead(SeReader reader) {
        Log.d(TAG, "Running HopLink Simple Read Tests");

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mText.append("\nLaunching tests for reader : " + reader.getName());

                try {



                    String poAid = "A000000291A000000191";
                    String t2UsageRecord1_dataFill =
                            "0102030405060708090A0B0C0D0E0F10" + "1112131415161718191A1B1C1D1E1F20"
                                    + "2122232425262728292A2B2C2D2E2F30";

                    mText.append("\n");
                    mText.append("Selecting application : " + poAid);
                    mText.append("\n");


                    ReadRecordsCmdBuild poReadRecordCmd_T2Env =
                            new ReadRecordsCmdBuild(PoClass.ISO,  (byte) 0x14, ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, true,
                                    (byte) 0x20, "Hoplink EF T2Environment");

                    ReadRecordsCmdBuild poReadRecordCmd_T2Usage =
                            new ReadRecordsCmdBuild(PoClass.ISO, (byte) 0x1A, ReadDataStructure.SINGLE_RECORD_DATA, (byte) 0x01, true,
                                    (byte) 0x30, "Hoplink EF T2Usage");

                    UpdateRecordCmdBuild poUpdateRecordCmd_T2UsageFill = new UpdateRecordCmdBuild(
                            PoClass.ISO, (byte) 0x1A, (byte) 0x01,
                            ByteArrayUtils.fromHex(t2UsageRecord1_dataFill), "Hoplink EF T2Usage");


                    Boolean keepChannelOpen = false;

                    mText.append("\n");
                    mText.append("Executing command Calypso : " + poReadRecordCmd_T2Env.getName());
                    mText.append("\n");
                    mText.append(
                            "Executing command Calypso : " + poReadRecordCmd_T2Usage.getName());
                    mText.append("\n");
                    mText.append("Executing command Calypso : "
                            + poUpdateRecordCmd_T2UsageFill.getName());
                    mText.append("\n");
                    mText.append("Keep Channel Open : " + keepChannelOpen);
                    mText.append("\n");
                    mText.append(
                            "Using protocol : " + ContactsProtocols.PROTOCOL_ISO7816_3.getName());
                    mText.append("\n ----\n ");

                    List<ApduRequest> poApduRequestList;

                    poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest(),
                            poReadRecordCmd_T2Usage.getApduRequest(),
                            poUpdateRecordCmd_T2UsageFill.getApduRequest());


                    SeRequest seRequest =
                            new SeRequest(new SeSelector(new SeSelector.AidSelector(ByteArrayUtils.fromHex(poAid), null), null, "AID = " + poAid),
                                    poApduRequestList, ChannelState.CLOSE_AFTER,
                                    ContactsProtocols.PROTOCOL_ISO7816_3);

//                    SeRequest seRequest =
//                            new SeRequest(new SeSelector(null, new SeSelector.AtrFilter(".*"), "Select any ATR"),
//                                    poApduRequestList, ChannelState.CLOSE_AFTER,
//                                    ContactsProtocols.PROTOCOL_ISO7816_3);


                    SeResponseSet seResponseSet =
                            ((ProxyReader) reader).transmitSet(new SeRequestSet(seRequest));


                    mText.append("\n ---- \n");
                    for (SeResponse response : seResponseSet.getResponses()) {
                        if (response != null) {
                            for (ApduResponse apdu : response.getApduResponses()) {
                                mText.append("Response : " + apdu.getStatusCode() + " - "
                                        + ByteArrayUtils.toHex(apdu.getBytes()));
                                mText.append("\n");
                            }
                            mText.append("\n\n\n\n\n");

                        } else {
                            mText.append("Response : null");
                            mText.append("\n\n\n\n\n");
                        }
                    }
                } catch (final KeypleReaderException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            e.printStackTrace();
                            mText.append("\n ---- \n");
                            mText.append("IOReader Exception : " + e.getMessage());

                        }
                    });

                }
            }
        });
    }



    /**
     * Revocation of the Activity
     * from @{@link org.eclipse.keyple.plugin.android.omapi.AndroidOmapiReader} list of observers
     */
    @Override
    public void onDestroy() {
        super.onDestroy();


        /*
         * // destroy AndroidNFC fragment if needed FragmentManager fm = getFragmentManager();
         * Fragment f = fm.findFragmentByTag(TAG_OMAPI_ANDROID_FRAGMENT); if (f != null) {
         * fm.beginTransaction().remove(f).commit(); }
         */

    }


}
