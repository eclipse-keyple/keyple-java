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

import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import java.io.IOException
import kotlinx.android.synthetic.main.activity_calypso_examples.drawerLayout
import kotlinx.android.synthetic.main.activity_calypso_examples.eventRecyclerView
import kotlinx.android.synthetic.main.activity_calypso_examples.navigationView
import kotlinx.android.synthetic.main.activity_calypso_examples.toolbar
import org.eclipse.keyple.core.card.selection.AbstractSmartCard
import org.eclipse.keyple.core.card.selection.CardSelection
import org.eclipse.keyple.core.service.SmartCardService
import org.eclipse.keyple.core.service.event.ObservableReader
import org.eclipse.keyple.core.service.event.ReaderEvent
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler
import org.eclipse.keyple.core.service.util.ContactlessCardCommonProtocols
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.example.adapter.EventAdapter
import org.eclipse.keyple.example.calypso.android.nfc.R
import org.eclipse.keyple.example.model.ChoiceEventModel
import org.eclipse.keyple.example.model.EventModel
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcPluginFactory
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcProtocolSettings
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcReader
import timber.log.Timber

abstract class AbstractExampleActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ObservableReader.ReaderObserver {

    /**
     * Use to modify event update behaviour regarding current use case execution
     */
    interface UseCase {
        fun onEventUpdate(event: ReaderEvent?)
    }

    /**
     * Variables for event window
     */
    private lateinit var adapter: RecyclerView.Adapter<*>
    private lateinit var layoutManager: RecyclerView.LayoutManager
    protected val events = arrayListOf<EventModel>()

    protected lateinit var reader: AndroidNfcReader

    protected var useCase: UseCase? = null
    protected lateinit var cardSelection: CardSelection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initContentView()

        /**
         * Init recycler view
         */
        adapter = EventAdapter(events)
        layoutManager = LinearLayoutManager(this)
        eventRecyclerView.layoutManager = layoutManager
        eventRecyclerView.adapter = adapter

        /**
         * Init menu
         */
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_navigation_drawer, R.string.close_navigation_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val readerObservationExceptionHandler = ReaderObservationExceptionHandler { pluginName, readerName, e -> }

        /**
         * Register AndroidNfc plugin Factory
         */
        val plugin = SmartCardService.getInstance().registerPlugin(AndroidNfcPluginFactory(this, readerObservationExceptionHandler))

        /**
         *  remove the observer if it already exist
         */
        reader = plugin.readers.values.first() as AndroidNfcReader
        reader.presenceCheckDelay = 100
        reader.noPlateformSound = false
        reader.skipNdefCheck = false

        (reader as ObservableReader).addObserver(this)

        // with this protocol settings we activate the nfc for ISO1443_4 protocol
        reader.activateProtocol(ContactlessCardCommonProtocols.ISO_14443_4.name,
                AndroidNfcProtocolSettings.getSetting(ContactlessCardCommonProtocols.ISO_14443_4.name))

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    protected fun initActionBar(toolbar: Toolbar, title: String, subtitle: String) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = title
        actionBar?.subtitle = subtitle
    }

    protected fun showAlertDialog(t: Throwable) {
        val builder = AlertDialog.Builder(this@AbstractExampleActivity)
        builder.setTitle(R.string.alert_dialog_title)
        builder.setMessage(getString(R.string.alert_dialog_message, t.message))
        val dialog = builder.create()
        dialog.show()
    }

    protected fun initFromBackgroundTextView() {
        addResultEvent("Smartcard detected while in background...")
    }

    protected fun clearEvents() {
        events.clear()
        adapter.notifyDataSetChanged()
    }

    protected fun addHeaderEvent(message: String) {
        events.add(EventModel(EventModel.TYPE_HEADER, message))
        adapter.notifyItemInserted(events.lastIndex)
        Timber.d("Header: %s", message)
    }

    protected fun addActionEvent(message: String) {
        events.add(EventModel(EventModel.TYPE_ACTION, message))
        adapter.notifyItemInserted(events.lastIndex)
        Timber.d("Action: %s", message)
    }

    protected fun addResultEvent(message: String) {
        events.add(EventModel(EventModel.TYPE_RESULT, message))
        adapter.notifyItemInserted(events.lastIndex)
        Timber.d("Result: %s", message)
    }

    protected fun addChoiceEvent(title: String, choices: List<String>, callback: (choice: String) -> Unit) {
        events.add(ChoiceEventModel(title, choices, callback))
        adapter.notifyItemInserted(events.lastIndex)
        Timber.d("Choice: %s: %s", title, choices.toString())
    }

    @Throws(IOException::class)
    protected fun checkNfcAvailability() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            throw IOException("Your device does not support NFC")
        } else {
            if (!nfcAdapter.isEnabled) {
                throw IOException("Please enable NFC to communicate with NFC Elements\"")
            }
        }
    }

    abstract fun initContentView()

    override fun onDestroy() {
        SmartCardService.getInstance().plugins.forEach {
            SmartCardService.getInstance().unregisterPlugin(it.key)
        }
        super.onDestroy()
    }

    protected fun getSmardCardInfos(smartCard: AbstractSmartCard, index: Int): String {
        val atr = try {
            ByteArrayUtil.toHex(smartCard.atrBytes)
        } catch (e: IllegalStateException) {
            Timber.w(e)
            e.message
        }
        val fci = try {
            ByteArrayUtil.toHex(smartCard.fciBytes)
        } catch (e: IllegalStateException) {
            Timber.w(e)
            e.message
        }

        return "Selection status for selection " +
                "(indexed $index): \n\t\t" +
                "ATR: ${atr}\n\t\t" +
                "FCI: $fci"
    }
}
