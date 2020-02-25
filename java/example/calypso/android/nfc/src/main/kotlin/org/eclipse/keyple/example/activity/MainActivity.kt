/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.activity

import android.graphics.Color
import android.nfc.NfcAdapter
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.TypefaceSpan
import android.view.MenuItem
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.navigationView
import kotlinx.android.synthetic.main.activity_main.text
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.keyple.calypso.command.po.parser.ReadDataStructure
import org.eclipse.keyple.calypso.command.po.parser.ReadRecordsRespPars
import org.eclipse.keyple.calypso.transaction.CalypsoPo
import org.eclipse.keyple.calypso.transaction.PoResource
import org.eclipse.keyple.calypso.transaction.PoSelectionRequest
import org.eclipse.keyple.calypso.transaction.PoSelector
import org.eclipse.keyple.calypso.transaction.PoTransaction
import org.eclipse.keyple.core.selection.SeSelection
import org.eclipse.keyple.core.seproxy.ChannelControl
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing
import org.eclipse.keyple.core.seproxy.SeProxyService
import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse
import org.eclipse.keyple.core.seproxy.event.ObservableReader
import org.eclipse.keyple.core.seproxy.event.ReaderEvent
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.example.calypso.android.nfc.R
import org.eclipse.keyple.example.util.CalypsoClassicInfo
import org.eclipse.keyple.example.util.configFlags
import org.eclipse.keyple.example.util.configProtocol
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPluginFactory
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcReader
import timber.log.Timber
import java.io.IOException

/**
 * Example of @[SeProxyService] implementation based on the @[AndroidNfcPlugin]
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
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ObservableReader.ReaderObserver {

    /**
     * Use to modify event update behaviour reguarding current use case execution
     */
    interface UseCase {
        fun onEventUpdate(event: ReaderEvent?)
    }

    private lateinit var seSelection: SeSelection
    private lateinit var reader: AndroidNfcReader
    private var readEnvironmentParserIndex: Int = 0
    private var useCase: UseCase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * Init Action Bar
         */
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = "NFC Plugins"
        actionBar?.subtitle = "Examples application"

        /**
         * Init menu
         */
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_navigation_drawer, R.string.close_navigation_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        /**
         * Register AndroidNfc plugin
         */
        SeProxyService.getInstance().registerPlugin(AndroidNfcPluginFactory())

        /**
         *  remove the observer if it already exist
         */
        reader = SeProxyService.getInstance().plugins.first().readers.first() as AndroidNfcReader
        reader.configFlags(presenceCheckDelay = 100, noPlateformSound = 0, skipNdefCheck = 0)

        (reader as ObservableReader).addObserver(this)

        // with this protocol settings we activate the nfc for ISO1443_4 protocol
        reader.configProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onResume() {
        super.onResume()
        try {
            checkNfcAvailability()
            if (intent.action != null && intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) run {
                configureUseCase0()

                Timber.d("Handle ACTION TECH intent")
                // notify reader that se detection has been launched
                reader.startSeDetection(ObservableReader.PollingMode.SINGLESHOT)
                initFromBackgroundTextView()
                reader.processIntent(intent)
            } else {
                initWaitingTextView()
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
                // enable detection
                reader.enableNFCReaderMode(this)
            }
        } catch (e: IOException) {
            showErrorDialog(e)
        }
    }

    override fun onPause() {
        Timber.i("on Pause Fragment - Stopping Read Write Mode")
        try {

            // notify reader that se detection has been switched off
            reader.stopSeDetection()

            // Disable Reader Mode for NFC Adapter
            reader.disableNFCReaderMode(this)
        } catch (e: KeyplePluginNotFoundException) {
            Timber.e(e, "NFC Plugin not found")
        }

        super.onPause()
    }

    override fun onDestroy() {
        (reader as ObservableReader).removeObserver(this)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        when (item.itemId) {
            R.id.usecase1 -> {
                configureUseCase1ExplicitSelectionAid()
            }
            R.id.usecase2 -> {
                configureUseCase2DefaultSelectionNotification()
            }
            R.id.usecase3 -> {
                configureUseCase3GroupedMultiSelection()
            }
            R.id.usecase4 -> {
                configureUseCase4SequentialMultiSelection()
            }
            R.id.start_scan -> {
                configureUseCase0()
                // notify reader that se detection has been launched
                reader.startSeDetection(ObservableReader.PollingMode.REPEATING)
            }
        }
        return true
    }

    private fun showErrorDialog(t: Throwable) {
        Timber.e(t)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.alert_dialog_title)
        builder.setMessage(getString(R.string.alert_dialog_message, t.message))
        val dialog = builder.create()
        dialog.show()
    }

    @Throws(IOException::class)
    private fun checkNfcAvailability() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            throw IOException("Your device does not support NFC")
        } else {
            if (!nfcAdapter.isEnabled) {
                throw IOException("Please enable NFC to communicate with NFC Elements\"")
            }
        }
    }

    private fun configureUseCase4SequentialMultiSelection() {
        appendColoredText(text, "\nUseCase Generic #4: AID based sequential explicit multiple selection ", Color.BLACK)
        appendColoredText(text, "\nSE Reader  NAME = ${reader.name}", Color.BLACK)

        /*Check if a SE is present in the reader */
        // if (reader.isSePresent) {
        if (true) {
            /*
              * operate SE AID selection (change the AID prefix here to adapt it to the SE used for
              * the test [the SE should have at least two applications matching the AID prefix])
              */
            val seAidPrefix = CalypsoClassicInfo.AID_PREFIX

            /* First selection case */
            seSelection = SeSelection(MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.KEEP_OPEN)

            /* AID based selection (1st selection, later indexed 0) */
            val selectionRequest1st = PoSelectionRequest(PoSelector(
                    SeCommonProtocols.PROTOCOL_ISO14443_4, null, PoSelector.PoAidSelector(
                    SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                    SeSelector.AidSelector.FileOccurrence.FIRST,
                    SeSelector.AidSelector.FileControlInformation.FCI), "Initial selection #1"))

            seSelection.prepareSelection(selectionRequest1st)

            /* Do the selection and display the result */
            doAndAnalyseSelection(reader, seSelection, 1)

            /*
              * New selection: get the next application occurrence matching the same AID, close the
              * physical channel after
              */
            seSelection = SeSelection(MultiSeRequestProcessing.FIRST_MATCH, ChannelControl.CLOSE_AFTER)

            val selectionRequest2nd = PoSelectionRequest(PoSelector(
                    SeCommonProtocols.PROTOCOL_ISO14443_4, null, PoSelector.PoAidSelector(
                    SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                    SeSelector.AidSelector.FileOccurrence.NEXT,
                    SeSelector.AidSelector.FileControlInformation.FCI), "Initial selection #2"))

            seSelection.prepareSelection(selectionRequest2nd)

            /* Do the selection and display the result */
            doAndAnalyseSelection(reader, seSelection, 2)
        } else {
            appendColoredText(text, "\nNo SE were detected.", Color.RED)
        }
    }

    private fun doAndAnalyseSelection(reader: SeReader, seSelection: SeSelection, index: Int) {
        val selectionsResult = seSelection.processExplicitSelection(reader)
        if (selectionsResult.hasActiveSelection()) {
            with(selectionsResult.getMatchingSelection(0)) {
                val matchingSe = this.matchingSe
                appendColoredText(text, "\n\nSelection status for selection ${this.extraInfo} " +
                        "(indexed ${this.selectionIndex}): \n\t\t" +
                        "ATR: ${ByteArrayUtil.toHex(matchingSe.selectionStatus.atr.bytes)}\n\t\t" +
                        "FCI: ${ByteArrayUtil.toHex(matchingSe.selectionStatus.fci.bytes)}",
                        Color.BLUE)
            }
        } else {
            appendColoredText(text, "\nThe selection did not match for case $index.", Color.RED)
        }
    }

    private fun configureUseCase3GroupedMultiSelection() {

        appendColoredText(text, "\nUseCase Generic #3: AID based grouped explicit multiple selection", Color.BLACK)
        appendColoredText(text, "\nSE Reader  NAME = ${reader.name}", Color.BLACK)

        /* CLOSE_AFTER to force selection of all applications*/
        seSelection = SeSelection(MultiSeRequestProcessing.PROCESS_ALL, ChannelControl.CLOSE_AFTER)

        /* operate SE selection (change the AID here to adapt it to the SE used for the test) */
        val seAidPrefix = "A000000404012509"

        // if (reader.isSePresent) {
        if (true) {
            /* AID based selection (1st selection, later indexed 0) */
            val selectionRequest1st = PoSelectionRequest(PoSelector(
                    SeCommonProtocols.PROTOCOL_ISO14443_4, null, PoSelector.PoAidSelector(
                    SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                    SeSelector.AidSelector.FileOccurrence.FIRST,
                    SeSelector.AidSelector.FileControlInformation.FCI), "Initial selection #1"))

            seSelection.prepareSelection(selectionRequest1st)

            /* next selection (2nd selection, later indexed 1) */
            val selectionRequest2nd = PoSelectionRequest(PoSelector(
                    SeCommonProtocols.PROTOCOL_ISO14443_4, null, PoSelector.PoAidSelector(
                    SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                    SeSelector.AidSelector.FileOccurrence.NEXT,
                    SeSelector.AidSelector.FileControlInformation.FCI), "Next selection #2"))

            seSelection.prepareSelection(selectionRequest2nd)

            /* next selection (3rd selection, later indexed 2) */
            val selectionRequest3rd = PoSelectionRequest(PoSelector(
                    SeCommonProtocols.PROTOCOL_ISO14443_4, null, PoSelector.PoAidSelector(
                    SeSelector.AidSelector.IsoAid(seAidPrefix), null,
                    SeSelector.AidSelector.FileOccurrence.NEXT,
                    SeSelector.AidSelector.FileControlInformation.FCI), "Next selection #3"))

            seSelection.prepareSelection(selectionRequest3rd)

            /*
            * Actual SE communication: operate through a single request the SE selection
            */
            val selectionResult = seSelection.processExplicitSelection(reader)

            if (selectionResult.matchingSelections.size > 0) {
                selectionResult.matchingSelections.forEach {
                    val matchingSe = it.matchingSe
                    appendColoredText(text, "\nSelection status for selection ${it.extraInfo} " +
                            "(indexed ${it.selectionIndex}): \n\t\t" +
                            "ATR: ${ByteArrayUtil.toHex(matchingSe.selectionStatus.atr.bytes)}\n\t\t" +
                            "FCI: ${ByteArrayUtil.toHex(matchingSe.selectionStatus.fci.bytes)}",
                            Color.BLACK)
                }
            } else {
                appendColoredText(text, "\nNo SE matched the selection.", Color.RED)
            }
        } else {
            appendColoredText(text, "\nNo SE were detected.", Color.RED)
        }

        useCase = null
    }

    private fun configureUseCase2DefaultSelectionNotification() {
        /*
        * Prepare a a new Calypso PO selection
        */
        seSelection = SeSelection()

        val aid = CalypsoClassicInfo.AID

        appendColoredText(text, "\n== UseCase Generic #2: AID based default selection ==", Color.BLACK)
        appendColoredText(text, "\n= SE Reader  NAME = ${reader.name}", Color.BLACK)

        /*
        * Setting of an AID based selection
        *
        * Select the first application matching the selection AID whatever the SE communication
        * protocol keep the logical channel open after the selection
        */

        /*
         * Generic selection: configures a SeSelector with all the desired attributes to make the
         * selection
         */
        val selectionRequest = PoSelectionRequest(PoSelector(
                SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                PoSelector.PoAidSelector(
                        SeSelector.AidSelector.IsoAid(aid), null),
                "AID: $aid"))

        /*
        * Add the selection case to the current selection (we could have added other cases here)
        */
        seSelection.prepareSelection(selectionRequest)

        /*
         * Provide the SeReader with the selection operation to be processed when a SE is inserted.
         */
        (reader as ObservableReader).setDefaultSelectionRequest(seSelection.selectionOperation,
                ObservableReader.NotificationMode.MATCHED_ONLY, ObservableReader.PollingMode.REPEATING)

        // (reader as ObservableReader).addObserver(this) //ALready done in onCreate

        appendColoredText(text, "\n==============", Color.BLACK)
        appendColoredText(text, "\n= Wait for a SE. The default AID based selection to be processed as soon as the SE is detected.", Color.BLACK)
        appendColoredText(text, "\n==============", Color.BLACK)

        useCase = object : UseCase {
            override fun onEventUpdate(event: ReaderEvent?) {
                CoroutineScope(Dispatchers.Main).launch {
                    when (event?.eventType) {
                        ReaderEvent.EventType.SE_MATCHED -> {
                            Timber.d("Tag detected - SE MATCHED")
                            val selectedSe = seSelection.processDefaultSelection(event.defaultSelectionsResponse).activeSelection.matchingSe
                            if (selectedSe != null) {
                                appendColoredText(text, "\nObserver notification: the selection of the SE has succeeded.", Color.BLUE)
                                appendColoredText(text, "\n==============", Color.BLUE)
                                appendColoredText(text, "\n= End of the SE processing.", Color.BLUE)
                                appendColoredText(text, "\n==============", Color.BLUE)
                            } else {
                                appendColoredText(text, "\nThe selection of the SE has failed. Should not have occurred due to the MATCHED_ONLY selection mode.", Color.RED)
                            }
                        }

                        ReaderEvent.EventType.SE_INSERTED -> {
                            Timber.d("SE Inserted")
                            appendColoredText(text, "\nSE_INSERTED event: should not have occurred due to the MATCHED_ONLY selection mode.", Color.GREEN)
                        }

                        ReaderEvent.EventType.SE_REMOVED -> {
                            Timber.d("SE removed")
                            appendColoredText(text, "\nThere is no PO inserted anymore. Return to the waiting state...", Color.GREEN)
                        }

                        else -> {
                        }
                    }
                }
                if (event?.eventType == ReaderEvent.EventType.SE_INSERTED || event?.eventType == ReaderEvent.EventType.SE_MATCHED) {
                    /*
                     * Informs the underlying layer of the end of the SE processing, in order to manage the
                     * removal sequence. <p>If closing has already been requested, this method will do
                     * nothing.
                     */
                    try {
                        (SeProxyService.getInstance().getPlugin(event.pluginName).getReader(event.readerName) as ObservableReader).notifySeProcessed()
                    } catch (e: KeypleReaderNotFoundException) {
                        Timber.e(e)
                    } catch (e: KeyplePluginNotFoundException) {
                        Timber.e(e)
                    }
                }
            }
        }
    }

    private fun configureUseCase1ExplicitSelectionAid() {
        /*
        * Prepare a a new Calypso PO selection
        */
        seSelection = SeSelection()

        val aid = CalypsoClassicInfo.AID

        // if (reader.isSePresent) {
        if (true) {
            /**
             * configure Protocol
             */
            val selectionRequest = PoSelectionRequest(PoSelector(
                    SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                    PoSelector.PoAidSelector(
                            SeSelector.AidSelector.IsoAid(aid), null),
                    "AID: $aid"))

            /**
             * Prepare Selection
             */
            seSelection.prepareSelection(selectionRequest)

            /**
             * We won't be listening for event update within this use case
             */
            useCase = null

            val selectionsResult = seSelection.processExplicitSelection(reader)

            if (selectionsResult.hasActiveSelection()) {
                val matchedSe = selectionsResult.activeSelection.matchingSe
                text.append("\n-- Calypso PO selection: ")
                appendColoredText(text, "The selection of the SE has succeeded.", Color.BLUE)
                text.append("\n-- Application FCI = ${matchedSe.selectionStatus.fci}")
                appendColoredText(text, "End of the generic SE processing.", Color.BLACK)
            } else {
                appendColoredText(text, "The selection of the SE has failed.", Color.RED)
            }
        } else {
            appendColoredText(text, "\nNo SE were detected.", Color.RED)
        }
    }

    private fun configureUseCase0() {
        // define task as an observer for ReaderEvents
        /*
         * Prepare a a new Calypso PO selection
         */
        seSelection = SeSelection()

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
        val poSelectionRequest = PoSelectionRequest(PoSelector(
                SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                PoSelector.PoAidSelector(
                        SeSelector.AidSelector.IsoAid(CalypsoClassicInfo.AID),
                        PoSelector.InvalidatedPo.REJECT),
                "AID: " + CalypsoClassicInfo.AID))

        /*
             * Prepare the reading order and keep the associated parser for later use once the
             * selection has been made.
             */
        readEnvironmentParserIndex = poSelectionRequest.prepareReadRecordsCmd(
                CalypsoClassicInfo.SFI_EnvironmentAndHolder,
                ReadDataStructure.SINGLE_RECORD_DATA, CalypsoClassicInfo.RECORD_NUMBER_1,
                String.format("EnvironmentAndHolder (SFI=%02X))",
                        CalypsoClassicInfo.SFI_EnvironmentAndHolder))

        /*
         * Add the selection case to the current selection (we could have added other cases
         * here)
         */
        seSelection.prepareSelection(poSelectionRequest)

        /*
             * Provide the SeReader with the selection operation to be processed when a PO is
             * inserted.
             */
        (reader as ObservableReader).setDefaultSelectionRequest(
                seSelection.selectionOperation, ObservableReader.NotificationMode.ALWAYS)

        // uncomment to active protocol listening for Mifare ultralight
        // reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_MIFARE_UL, AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_UL))
        reader.configProtocol(SeCommonProtocols.PROTOCOL_MIFARE_UL)

        // uncomment to active protocol listening for Mifare ultralight
        // reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC, AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC))
        reader.configProtocol(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC)

        useCase = object : UseCase {
            override fun onEventUpdate(event: ReaderEvent?) {
                CoroutineScope(Dispatchers.Main).launch {
                    when (event?.eventType) {
                        ReaderEvent.EventType.SE_MATCHED -> {
                            text.append("\nTag detected - SE MATCHED")
                            executeCommands(event.defaultSelectionsResponse)
                            (reader as ObservableReader).notifySeProcessed()
                            Timber.d("Tag detected - SE MATCHED")
                        }

                        ReaderEvent.EventType.SE_INSERTED -> {
                            text.append("\nPO detected but AID didn't match with " + CalypsoClassicInfo.AID)
                            (reader as ObservableReader).notifySeProcessed()
                            Timber.d("PO detected but AID didn't match with ${CalypsoClassicInfo.AID}")
                        }

                        ReaderEvent.EventType.SE_REMOVED -> {
                            text.append("\nTag removed")
                            Timber.d("Tag removed")
                        }

                        ReaderEvent.EventType.TIMEOUT_ERROR -> {
                            text.append("\nError reading card")
                            Timber.d("Error reading card")
                        }
                    }
                }
            }
        }
    }

    /**
     * Run Calypso simple read transaction
     *
     * @param defaultSelectionsResponse
     */
    private fun executeCommands(
        defaultSelectionsResponse: AbstractDefaultSelectionsResponse
    ) {

        Timber.d("Running Calypso Simple Read transaction")

        try {
            /*
             * print tag info in View
             */
            text.append("\nTag Id :" + reader.printTagId())
            val selectionsResult = seSelection.processDefaultSelection(defaultSelectionsResponse)
            appendColoredText(text, "\n\n1st PO exchange: aid selection", Color.BLACK)

            if (selectionsResult.hasActiveSelection()) {
                val calypsoPo = selectionsResult.activeSelection.matchingSe as CalypsoPo

                text.append("\n-- Calypso PO selection: ")
                appendColoredText(text, "SUCCESS", Color.BLUE)
                text.append("\n-- AID: ")
                appendHexBuffer(text, ByteArrayUtil.fromHex(CalypsoClassicInfo.AID))

                /*
                 * Retrieve the data read from the parser updated during the selection process
                 */
                val readEnvironmentParser = selectionsResult
                        .getActiveSelection().getResponseParser(readEnvironmentParserIndex) as ReadRecordsRespPars

                val environmentAndHolder = readEnvironmentParser.records[CalypsoClassicInfo.RECORD_NUMBER_1.toInt()]

                text.append("\n-- Environment and Holder file: ")
                appendHexBuffer(text, environmentAndHolder!!)

                appendColoredText(text, "\n\n2nd PO exchange: read the event log file",
                        Color.BLACK)
                val poTransaction = PoTransaction(PoResource(reader, calypsoPo))

                /*
                 * Prepare the reading order and keep the associated parser for later use once the
                 * transaction has been processed.
                 */
                val readEventLogParserIndex = poTransaction.prepareReadRecordsCmd(
                        CalypsoClassicInfo.SFI_EventLog, ReadDataStructure.SINGLE_RECORD_DATA,
                        CalypsoClassicInfo.RECORD_NUMBER_1,
                        String.format("EventLog (SFI=%02X, recnbr=%d))",
                                CalypsoClassicInfo.SFI_EventLog,
                                CalypsoClassicInfo.RECORD_NUMBER_1))

                /*
                 * Actual PO communication: send the prepared read order, then close the channel
                 * with the PO
                 */
                if (poTransaction.processPoCommands(ChannelControl.CLOSE_AFTER)) {
                    text.append("\n-- Transaction: ")
                    appendColoredText(text, "SUCCESS", Color.BLUE)

                    /*
                     * Retrieve the data read from the parser updated during the transaction process
                     */

                    val readEventLogParser = poTransaction
                            .getResponseParser(readEventLogParserIndex) as ReadRecordsRespPars
                    val eventLog = readEventLogParser.records[CalypsoClassicInfo.RECORD_NUMBER_1.toInt()]

                    /* Log the result */
                    text.append("\n-- EventLog file:")
                    appendHexBuffer(text, eventLog!!)
                }
                appendColoredText(text, "\n\nEnd of the Calypso PO processing.", Color.BLACK)
                text.append("\n ----")
                appendColoredText(text, "\nYou can remove the card now", Color.BLUE)
                text.append("\n ----")
            } else {
                appendColoredText(text,
                        "The selection of the PO has failed. Should not have occurred due to the MATCHED_ONLY selection mode.",
                        Color.RED)
            }
        } catch (e1: KeypleReaderException) {
            e1.fillInStackTrace()
        } catch (e: Exception) {
            Timber.d("Exception: " + e.message)
            appendColoredText(text, "\nException: " + e.message, Color.RED)
            e.fillInStackTrace()
        }
    }

    override fun update(event: ReaderEvent?) {
        Timber.i("New ReaderEvent received : $event")
        useCase?.onEventUpdate(event)
    }

    /**
     * Initialize display
     */
    private fun initWaitingTextView() {
        text.setText("") // reset
        appendColoredText(text, "Waiting for a smartcard...", Color.BLUE)
        text.append("\n ---- ")
    }

    private fun initFromBackgroundTextView() {
        text.setText("") // reset
        appendColoredText(text, "Smartcard detected while in background...", Color.BLUE)
        text.append("\n ---- ")
    }

    /**
     * Append to tv a string containing an hex representation of the byte array provided in
     * argument.
     *
     *
     * The font used is monospaced.
     *
     * @param tv TextView
     * @param ba byte array
     */
    private fun appendHexBuffer(tv: TextView, ba: ByteArray) {
        val start = tv.text.length
        tv.append(ByteArrayUtil.toHex(ba))
        val end = tv.text.length

        val spannableText = tv.text as Spannable

        spannableText.setSpan(TypefaceSpan("monospace"), start, end, 0)
        spannableText.setSpan(RelativeSizeSpan(0.70f), start, end, 0)
    }

    /**
     * Append to tv a text colored according to the provided argument
     *
     * @param tv TextView
     * @param text string
     * @param color color value
     */
    private fun appendColoredText(tv: TextView, text: String, color: Int) {
        val start = tv.text.length
        tv.append(text)
        val end = tv.text.length

        val spannableText = tv.text as Spannable
        spannableText.setSpan(ForegroundColorSpan(color), start, end, 0)
    }
}
