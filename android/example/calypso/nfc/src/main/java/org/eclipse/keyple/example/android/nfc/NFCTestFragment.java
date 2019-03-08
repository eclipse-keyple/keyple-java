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
package org.eclipse.keyple.example.android.nfc;



import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure;
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars;
import org.eclipse.keyple.calypso.transaction.CalypsoPo;
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest;
import org.eclipse.keyple.calypso.transaction.PoSelector;
import org.eclipse.keyple.calypso.transaction.PoTransaction;
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcFragment;
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPlugin;
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcProtocolSettings;
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcReader;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeProxyService;
import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.event.SelectionResponse;
import org.eclipse.keyple.seproxy.exception.KeypleBaseException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.seproxy.protocol.SeProtocolSetting;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
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

    private SeReader reader;
    private SeSelection seSelection;
    private ReadRecordsRespPars readEnvironmentParser;


    public static NFCTestFragment newInstance() {
        return new NFCTestFragment();
    }

    /**
     * Initialize SEProxy with Keyple Android NFC Plugin Add this view to the list of Observer
     * of @{@link SeReader}
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
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().add(AndroidNfcFragment.newInstance(), TAG_NFC_ANDROID_FRAGMENT)
                .commit();

        try {
            // define task as an observer for ReaderEvents
            LOG.debug("Define this view as an observer for ReaderEvents");
            reader = seProxyService.getPlugins().first().getReaders().first();
            /* remove the observer if it already exist */
            ((ObservableReader) reader).addObserver(this);

            reader.setParameter("FLAG_READER_PRESENCE_CHECK_DELAY", "100");
            reader.setParameter("FLAG_READER_NO_PLATFORM_SOUNDS", "0");
            reader.setParameter("FLAG_READER_SKIP_NDEF_CHECK", "0");


            // with this protocol settings we activate the nfc for ISO1443_4 protocol
            ((ObservableReader) reader).addSeProtocolSetting(
                    new SeProtocolSetting(AndroidNfcProtocolSettings.SETTING_PROTOCOL_ISO14443_4));

            /*
             * Prepare a Calypso PO selection
             */
            seSelection = new SeSelection(reader);

            /*
             * Setting of an AID based selection of a Calypso REV3 PO
             *
             * Select the first application matching the selection AID whatever the SE communication
             * protocol keep the logical channel open after the selection
             */

            /*
             * Calypso selection: configures a PoSelector with all the desired attributes to make
             * the selection and read additional information afterwards
             */
            PoSelectionRequest poSelectionRequest = new PoSelectionRequest(new PoSelector(
                    new PoSelector.PoAidSelector(ByteArrayUtils.fromHex(CalypsoClassicInfo.AID),
                            PoSelector.InvalidatedPo.REJECT),
                    null, "AID: " + CalypsoClassicInfo.AID), ChannelState.KEEP_OPEN,
                    ContactlessProtocols.PROTOCOL_ISO14443_4);

            /*
             * Prepare the reading order and keep the associated parser for later use once the
             * selection has been made.
             */
            readEnvironmentParser = poSelectionRequest.prepareReadRecordsCmd(
                    CalypsoClassicInfo.SFI_EnvironmentAndHolder,
                    ReadDataStructure.SINGLE_RECORD_DATA, CalypsoClassicInfo.RECORD_NUMBER_1,
                    String.format("EnvironmentAndHolder (SFI=%02X))",
                            CalypsoClassicInfo.SFI_EnvironmentAndHolder));

            /*
             * Add the selection case to the current selection (we could have added other cases
             * here)
             */
            seSelection.prepareSelection(poSelectionRequest);

            /*
             * Provide the SeReader with the selection operation to be processed when a PO is
             * inserted.
             */
            ((ObservableReader) reader).setDefaultSelectionRequest(
                    seSelection.getSelectionOperation(),
                    ObservableReader.NotificationMode.MATCHED_ONLY);

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
        mText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                initTextView();
                return true;
            }
        });
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
                    case SE_MATCHED:
                        runCalyspoTransaction(event.getDefaultSelectionResponse());
                        break;

                    case SE_INSERTED:

                        // execute simple tests
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
     * Run Calypso simple read transaction
     * 
     * @param defaultSelectionResponse
     */
    private void runCalyspoTransaction(final SelectionResponse defaultSelectionResponse) {
        LOG.debug("Running Calypso Simple Read transaction");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    initTextView();

                    /*
                     * print tag info in View
                     */
                    mText.append("\n ---- \n");
                    mText.append(((AndroidNfcReader) reader).printTagId());
                    mText.append("\n ---- \n");
                    if (seSelection.processDefaultSelection(defaultSelectionResponse)) {
                        CalypsoPo calypsoPo = (CalypsoPo) seSelection.getSelectedSe();

                        mText.append("\nCalypso PO selection: ");
                        appendColoredText(mText, "SUCCESS\n", Color.GREEN);
                        mText.append("AID: ");
                        appendHexBuffer(mText, ByteArrayUtils.fromHex(CalypsoClassicInfo.AID));

                        /*
                         * Retrieve the data read from the parser updated during the selection
                         * process
                         */
                        byte environmentAndHolder[] = (readEnvironmentParser.getRecords())
                                .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                        mText.append("\n\nEnvironment and Holder file: ");
                        appendHexBuffer(mText, environmentAndHolder);

                        appendColoredText(mText, "\n\n2nd PO exchange:\n", Color.BLACK);
                        mText.append("* read the event log file");
                        PoTransaction poTransaction = new PoTransaction(reader, calypsoPo);

                        /*
                         * Prepare the reading order and keep the associated parser for later use
                         * once the transaction has been processed.
                         */
                        ReadRecordsRespPars readEventLogParser =
                                poTransaction.prepareReadRecordsCmd(CalypsoClassicInfo.SFI_EventLog,
                                        ReadDataStructure.SINGLE_RECORD_DATA,
                                        CalypsoClassicInfo.RECORD_NUMBER_1,
                                        String.format("EventLog (SFI=%02X, recnbr=%d))",
                                                CalypsoClassicInfo.SFI_EventLog,
                                                CalypsoClassicInfo.RECORD_NUMBER_1));

                        /*
                         * Actual PO communication: send the prepared read order, then close the
                         * channel with the PO
                         */
                        if (poTransaction.processPoCommands(ChannelState.CLOSE_AFTER)) {
                            mText.append("\nTransaction: ");
                            appendColoredText(mText, "SUCCESS\n", Color.GREEN);

                            /*
                             * Retrieve the data read from the parser updated during the transaction
                             * process
                             */
                            byte eventLog[] = (readEventLogParser.getRecords())
                                    .get((int) CalypsoClassicInfo.RECORD_NUMBER_1);

                            /* Log the result */
                            mText.append("\nEventLog file:\n");
                            appendHexBuffer(mText, eventLog);
                        }
                        appendColoredText(mText, "\n\nEnd of the Calypso PO processing.",
                                Color.BLACK);
                    } else {
                        appendColoredText(mText,
                                "The selection of the PO has failed. Should not have occurred due to the MATCHED_ONLY selection mode.",
                                Color.RED);
                    }
                } catch (KeypleReaderException e1) {
                    e1.fillInStackTrace();
                } catch (Exception e) {
                    LOG.debug("Exception: " + e.getMessage());
                    appendColoredText(mText, "\nException: " + e.getMessage(), Color.RED);
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

        LOG.debug("Remove task as an observer for ReaderEvents");
        ((ObservableReader) reader).removeObserver(this);

        // destroy AndroidNFC fragment
        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentByTag(TAG_NFC_ANDROID_FRAGMENT);
        if (f != null) {
            fm.beginTransaction().remove(f).commit();
        }
    }


    /**
     * Initialize display
     */
    private void initTextView() {
        mText.setText("");// reset
        appendColoredText(mText, "Waiting for a smartcard...", Color.BLUE);
        mText.append("\n ---- \n");
    }

    /**
     * Append to tv a string containing an hex representation of the byte array provided in
     * argument.
     * <p>
     * The font used is monospaced.
     * 
     * @param tv TextView
     * @param ba byte array
     */
    private static void appendHexBuffer(TextView tv, byte[] ba) {
        int start = tv.getText().length();
        tv.append(ByteArrayUtils.toHex(ba));
        int end = tv.getText().length();

        Spannable spannableText = (Spannable) tv.getText();

        spannableText.setSpan(new TypefaceSpan("monospace"), start, end, 0);
        spannableText.setSpan(new RelativeSizeSpan(0.70f), start, end, 0);
    }

    /**
     * Append to tv a text colored according to the provided argument
     * 
     * @param tv TextView
     * @param text string
     * @param color color value
     */
    private static void appendColoredText(TextView tv, String text, int color) {
        int start = tv.getText().length();
        tv.append(text);
        int end = tv.getText().length();

        Spannable spannableText = (Spannable) tv.getText();
        spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
    }
}
