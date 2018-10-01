/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.android.nfc;


import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.UpdateRecordCmdBuild;
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcFragment;
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPlugin;
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcProtocolSettings;
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcReader;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.seproxy.SeRequestSet;
import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Test the Keyple NFC Plugin Configure the NFC reader Configure the Observability Run test commands
 * when appropriate tag is detected.
 */
public class NFCTestFragment extends Fragment implements ObservableReader.ReaderObserver {

    private static final Logger LOG = LoggerFactory.getLogger(NFCTestFragment.class);

    private static final String TAG = NFCTestFragment.class.getSimpleName();
    private static final String TAG_NFC_ANDROID_FRAGMENT =
            "org.eclipse.keyple.plugin.android.nfc.AndroidNfcFragment";

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

        // 1 - First initialize SEProxy with Android Plugin
        LOG.debug("Initialize SEProxy with Android Plugin");
        SeProxyService seProxyService = SeProxyService.getInstance();
        SortedSet<ReaderPlugin> plugins = new ConcurrentSkipListSet<ReaderPlugin>();
        plugins.add(AndroidNfcPlugin.getInstance());
        seProxyService.setPlugins(plugins);

        // 2 - add NFC Fragment to activity in order to communicate with Android Plugin
        LOG.debug("Add Keyple NFC Fragment to activity in order to "
                + "communicate with Android Plugin");
        getFragmentManager().beginTransaction()
                .add(AndroidNfcFragment.newInstance(), TAG_NFC_ANDROID_FRAGMENT).commit();


        try {
            // define task as an observer for ReaderEvents
            LOG.debug("Define this view as an observer for ReaderEvents");
            ProxyReader reader = seProxyService.getPlugins().first().getReaders().first();
            ((AndroidNfcReader) reader).addObserver(this);

            reader.setParameter("FLAG_READER_PRESENCE_CHECK_DELAY", "5000");
            reader.setParameter("FLAG_READER_NO_PLATFORM_SOUNDS", "0");
            reader.setParameter("FLAG_READER_SKIP_NDEF_CHECK", "0");


            // with this protocol settings we activate the nfc for ISO1443_4 protocol
            ((AndroidNfcReader) reader).addSeProtocolSetting(
                    new SeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4));


            /*
             * uncomment to active protocol listening for Mifare ultralight ((AndroidNfcReader)
             *
             * reader).addSeProtocolSetting( AndroidNfcProtocolSettings.SETTING_PROTOCOL_MIFARE_UL);
             * 
             * uncomment to active protocol listening for Mifare Classic ((AndroidNfcReader)
             * reader).addSeProtocolSetting(
             * AndroidNfcProtocolSettings.SETTING_PROTOCOL_MIFARE_CLASSIC);
             */

        } catch (KeypleBaseException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
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
        View view =
                inflater.inflate(org.eclipse.keyple.example.android.nfc.R.layout.fragment_nfc_test,
                        container, false);
        mText = view.findViewById(org.eclipse.keyple.example.android.nfc.R.id.text);
        initTextView();
        return view;
    }

    /**
     * Catch @{@link AndroidNfcReader} events When a SE is inserted, launch test commands
     ** 
     * @param event
     */
    @Override
    public void update(final ReaderEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                LOG.info("New ReaderEvent received : " + event.toString());

                switch (event.getEventType()) {
                    case SE_INSERTED:

                        // execute simple tests
                        runHoplinkSimpleRead();
                        break;

                    case SE_REMOVAL:
                        // mText.append("\n ---- \n");
                        // mText.append("Connection closed to tag");
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
     * Run Hoplink Simple read command
     */
    private void runHoplinkSimpleRead() {
        LOG.debug("Running HopLink Simple Read Tests");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {



                    initTextView();

                    ProxyReader reader = null;
                    reader = SeProxyService.getInstance().getPlugins().first().getReaders().first();

                    /*
                     * print tag info in View
                     */
                    mText.append("\n ---- \n");
                    mText.append(((AndroidNfcReader) reader).printTagId());
                    mText.append("\n ---- \n");

                    /*
                     * Build and execute Calypso commands
                     */

                    String poAid = "A000000291A000000191"; // HOPLINK APPLICATION
                    String t2UsageRecord1_dataFill =
                            "0102030405060708090A0B0C0D0E0F10" + "1112131415161718191A1B1C1D1E1F20"
                                    + "2122232425262728292A2B2C2D2E2F30";


                    ReadRecordsCmdBuild poReadRecordCmd_T2Env = new ReadRecordsCmdBuild(
                            PoRevision.REV3_1, (byte) 0x14, (byte) 0x01, true, (byte) 0x20);

                    ReadRecordsCmdBuild poReadRecordCmd_T2Usage = new ReadRecordsCmdBuild(
                            PoRevision.REV3_1, (byte) 0x1A, (byte) 0x01, true, (byte) 0x30);

                    UpdateRecordCmdBuild poUpdateRecordCmd_T2UsageFill =
                            new UpdateRecordCmdBuild(PoRevision.REV3_1, (byte) 0x1A, (byte) 0x01,
                                    ByteBufferUtils.fromHex(t2UsageRecord1_dataFill));

                    List<ApduRequest> poApduRequestList;

                    poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest(),
                            poReadRecordCmd_T2Usage.getApduRequest(),
                            poUpdateRecordCmd_T2UsageFill.getApduRequest());

                    Boolean keepChannelOpen = false;

                    SeRequest seRequest =
                            new SeRequest(new SeRequest.AidSelector(ByteBufferUtils.fromHex(poAid)),
                                    poApduRequestList, keepChannelOpen,
                                    ContactlessProtocols.PROTOCOL_ISO14443_4);

                    // transmit seRequestSet to Reader
                    final SeResponseSet seResponseSet =
                            reader.transmit(new SeRequestSet(seRequest));

                    /*
                     * print responses in View
                     */

                    for (SeResponse response : seResponseSet.getResponses()) {
                        if (response != null) {

                            // mText.append("AID selected : " + poAid);

                            // print AID selection results
                            mText.append("AID " + poAid + " : ");
                            if (response.getFci().isSuccessful()) {
                                appendColoredText(mText, "SUCCESS", Color.GREEN);
                            } else {
                                appendColoredText(mText, "FAILED", Color.RED);
                            }
                            mText.append("\n ---- \n");

                            // print Response status
                            for (int i = 0; i < response.getApduResponses().size(); i++) {
                                // print command name
                                mText.append(
                                        poApduRequestList.get(i).getName() + " REV3_1" + " : ");
                                // print response status
                                if (response.getApduResponses().get(i).isSuccessful()) {
                                    appendColoredText(mText, "SUCCESS", Color.GREEN);
                                } else {
                                    appendColoredText(mText, "FAILED", Color.RED);
                                }
                                mText.append("\n ---- \n");
                            }
                            mText.append("\n\n\n\n\n");
                        }
                    }
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }

        });

    }



    /**
     * Revocation of the Activity from @{@link AndroidNfcReader} list of observers
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            LOG.debug("Remove task as an observer for ReaderEvents");
            SeProxyService seProxyService = SeProxyService.getInstance();
            ProxyReader reader = seProxyService.getPlugins().first().getReaders().first();
            ((ObservableReader) reader).removeObserver(this);

            // destroy AndroidNFC fragment
            FragmentManager fm = getFragmentManager();
            Fragment f = fm.findFragmentByTag(TAG_NFC_ANDROID_FRAGMENT);
            if (f != null) {
                fm.beginTransaction().remove(f).commit();
            }

        } catch (KeypleReaderException e) {
            e.printStackTrace();
        }
    }


    private void initTextView() {
        mText.setText("");// reset
        appendColoredText(mText, "Waiting for a smartcard...", Color.BLUE);
        mText.append("\n ---- \n");

    }

    private static void appendColoredText(TextView tv, String text, int color) {
        int start = tv.getText().length();
        tv.append(text);
        int end = tv.getText().length();

        Spannable spannableText = (Spannable) tv.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }



}
