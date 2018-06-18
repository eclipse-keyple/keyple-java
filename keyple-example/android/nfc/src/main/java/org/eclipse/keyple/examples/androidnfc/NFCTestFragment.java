/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.examples.androidnfc;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.calypso.commands.po.PoRevision;
import org.eclipse.keyple.calypso.commands.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.commands.po.builder.UpdateRecordCmdBuild;
import org.eclipse.keyple.commands.InconsistentCommandException;
import org.eclipse.keyple.plugin.androidnfc.AndroidNfcFragment;
import org.eclipse.keyple.plugin.androidnfc.AndroidNfcPlugin;
import org.eclipse.keyple.plugin.androidnfc.AndroidNfcProtocolSettings;
import org.eclipse.keyple.plugin.androidnfc.AndroidNfcReader;
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
import android.widget.RadioGroup;
import android.widget.TextView;


/**
 * Test the Keyple NFC Plugin Configure the NFC reader Configure the Observability Run test commands
 * when appropriate tag is detected.
 */
public class NFCTestFragment extends Fragment
        implements AbstractObservableReader.Observer<ReaderEvent> {


    private static final String TAG = NFCTestFragment.class.getSimpleName();
    private static final String TAG_NFC_ANDROID_FRAGMENT =
            "org.eclipse.keyple.plugin.androidnfc.AndroidNfcFragment";

    // UI
    private TextView mText;
    private RadioGroup radioGroup;


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

        // 1 - First initialize SEProxy with Android Plugin
        Log.d(TAG, "Initialize SEProxy with Android Plugin");
        SeProxyService seProxyService = SeProxyService.getInstance();
        SortedSet<ReadersPlugin> plugins = new ConcurrentSkipListSet<ReadersPlugin>();
        plugins.add(AndroidNfcPlugin.getInstance());
        seProxyService.setPlugins(plugins);

        // 2 - add NFC Fragment to activity in order to communicate with Android Plugin
        Log.d(TAG, "Add Keyple NFC Fragment to activity in order to "
                + "communicate with Android Plugin");
        getFragmentManager().beginTransaction()
                .add(AndroidNfcFragment.newInstance(), TAG_NFC_ANDROID_FRAGMENT).commit();


        try {
            // define task as an observer for ReaderEvents
            Log.d(TAG, "Define this view as an observer for ReaderEvents");
            ProxyReader reader = seProxyService.getPlugins().first().getReaders().first();
            ((AndroidNfcReader) reader).addObserver(this);

            reader.setParameter("READER_PRESENCE_CHECK_DELAY", "5000");
            // reader.setParameter("FLAG_READER", "NO_PLATFORM_SOUNDS");
            // reader.setParameter("FLAG_READER", "SKIP_NDEF_CHECK");


            // with this protocol settings we activate the nfc for ISO1443_4 protocol
            ((AndroidNfcReader) reader)
                    .addSeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4);


            /*
             * uncomment to active protocol listening for Mifare ultralight ((AndroidNfcReader)
             * reader).addSeProtocolSetting( AndroidNfcProtocolSettings.SETTING_PROTOCOL_MIFARE_UL);
             * 
             * uncomment to active protocol listening for Mifare Classic ((AndroidNfcReader)
             * reader).addSeProtocolSetting(
             * AndroidNfcProtocolSettings.SETTING_PROTOCOL_MIFARE_CLASSIC);
             */

        } catch (IOReaderException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

        // Define UI components
        View view = inflater.inflate(R.layout.fragment_nfc_test, container, false);
        mText = view.findViewById(R.id.text);
        radioGroup = view.findViewById(R.id.radioGroup);
        return view;
    }

    /**
     * Catch @{@link AndroidNfcReader} events When a SE is inserted, launch test commands
     *
     * @param observable
     * @param event
     */
    @Override
    public void update(Observable<ReaderEvent> observable, ReaderEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "New ReaderEvent received : " + event.toString());

                switch (event) {
                    case SE_INSERTED:
                        mText.append("\n ---- \n");
                        mText.append("Tag opened to tag");
                        try {

                            runTest();

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
     * Runs the selected commands test
     */
    private void runTest() {
        if (radioGroup.getCheckedRadioButtonId() == R.id.hoplinkSimpleRead) {
            runHoplinkSimpleRead();
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
     * Revocation of the Activity from @{@link AndroidNfcReader} list of observers
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
            Fragment f = fm.findFragmentByTag(TAG_NFC_ANDROID_FRAGMENT);
            if (f != null) {
                fm.beginTransaction().remove(f).commit();
            }

        } catch (IOReaderException e) {
            e.printStackTrace();
        }
    }



}
