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
 */
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
import org.eclipse.keyple.core.seproxy.SeProxyService
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse
import org.eclipse.keyple.core.seproxy.event.ObservableReader
import org.eclipse.keyple.core.seproxy.event.ReaderEvent
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.example.calypso.android.nfc.R
import org.eclipse.keyple.example.util.CalypsoClassicInfo
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPluginFactory
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcProtocolSettings
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

    private lateinit var reader: AndroidNfcReader
    private lateinit var seSelection: SeSelection
    private var readEnvironmentParserIndex: Int = 0

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

        (reader as ObservableReader).addObserver(this)

        seSelection = SeSelection()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    }

    override fun onResume() {
        super.onResume()
        initTextView()
        try {
            checkNfcAvailability()
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }catch (e: IOException){
            showErrorDialog(e)
        }
    }

    override fun onPause() {
        Timber.i("on Pause Fragment - Stopping Read Write Mode")
        try {
            // Disable Reader Mode for NFC Adapter
            reader.disableNFCReaderMode(this)

            //notify reader that se detection has been switched off
            reader.stopSeDetection()

        } catch (e: KeyplePluginNotFoundException) {
            Timber.e(e,"NFC Plugin not found")

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
        when(item.itemId){
            R.id.start_scan -> startScan()
        }
        return true
    }

    private fun showErrorDialog(t: Throwable){
        Timber.e(t)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.alert_dialog_title)
        builder.setMessage(getString(R.string.alert_dialog_message, t.message))
        val dialog = builder.create()
        dialog.show()
    }

    @Throws(IOException::class)
    private fun checkNfcAvailability(){
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            throw IOException("Your device does not support NFC")
        } else {
            if (!nfcAdapter.isEnabled) {
                throw IOException("Please enable NFC to communicate with NFC Elements\"")
            }
        }
    }

    private fun startScan(){
        // define task as an observer for ReaderEvents
        Timber.d("Define this view as an observer for ReaderEvents")
        val reader = SeProxyService.getInstance().plugins.first().readers.first() as AndroidNfcReader
        reader.setParameter("FLAG_READER_PRESENCE_CHECK_DELAY", "100")
        reader.setParameter("FLAG_READER_NO_PLATFORM_SOUNDS", "0")
        reader.setParameter("FLAG_READER_SKIP_NDEF_CHECK", "0")

        // with this protocol settings we activate the nfc for ISO1443_4 protocol
        reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_ISO14443_4,
                AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_ISO14443_4))

        /*
             * Prepare a Calypso PO selection
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

        //uncomment to active protocol listening for Mifare ultralight
        //reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_MIFARE_UL, AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_UL))

        //uncomment to active protocol listening for Mifare ultralight
        //reader.addSeProtocolSetting(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC, AndroidNfcProtocolSettings.getSetting(SeCommonProtocols.PROTOCOL_MIFARE_CLASSIC))

        //enable detection
        reader.enableNFCReaderMode(this)

        //notify reader that se detection has been launched
        reader.startSeDetection(ObservableReader.PollingMode.SINGLESHOT)
    }

    /**
     * Run Calypso simple read transaction
     *
     * @param defaultSelectionsResponse
     */
    private fun executeCommands(
            defaultSelectionsResponse: AbstractDefaultSelectionsResponse) {

        Timber.d("Running Calypso Simple Read transaction")

        try {
            /*
             * print tag info in View
             */
            text.append("\nTag Id :" + reader.printTagId())
            val selectionsResult = seSelection.processDefaultSelection(defaultSelectionsResponse)
            appendColoredText(text, "\n\n1st PO exchange: aid selection", Color.BLACK)

            if (selectionsResult.hasActiveSelection()) {
                val calypsoPo = selectionsResult.getActiveSelection().getMatchingSe() as CalypsoPo

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
        CoroutineScope(Dispatchers.Main).launch {
            when (event?.eventType) {
                ReaderEvent.EventType.SE_MATCHED -> {
                    text.append("\nTag detected - SE MATCHED")
                    executeCommands(event.defaultSelectionsResponse)
                    (reader  as ObservableReader).notifySeProcessed()
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

    /**
     * Initialize display
     */
    private fun initTextView() {
        text.setText("")// reset
        appendColoredText(text, "Waiting for a smartcard...", Color.BLUE)
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
