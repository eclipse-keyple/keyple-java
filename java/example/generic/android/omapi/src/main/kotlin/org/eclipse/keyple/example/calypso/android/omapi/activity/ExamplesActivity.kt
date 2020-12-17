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
package org.eclipse.keyple.example.calypso.android.omapi.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_core_examples.drawerLayout
import kotlinx.android.synthetic.main.activity_core_examples.eventRecyclerView
import kotlinx.android.synthetic.main.activity_core_examples.navigationView
import kotlinx.android.synthetic.main.activity_core_examples.toolbar
import org.eclipse.keyple.core.service.Reader
import org.eclipse.keyple.core.service.SmartCardService
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.example.calypso.android.omapi.adapter.EventAdapter
import org.eclipse.keyple.example.calypso.android.omapi.model.ChoiceEventModel
import org.eclipse.keyple.example.calypso.android.omapi.model.EventModel
import org.eclipse.keyple.plugin.android.omapi.PLUGIN_NAME
import timber.log.Timber

abstract class ExamplesActivity : BasicActivity(), NavigationView.OnNavigationItemSelectedListener {

    /**
     * Variables for event window
     */
    protected lateinit var readers: Map<String, Reader>
    private lateinit var adapter: RecyclerView.Adapter<*>
    private lateinit var layoutManager: RecyclerView.LayoutManager
    protected val events = arrayListOf<EventModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = EventAdapter(events)
        layoutManager = LinearLayoutManager(this)
        initContentView()

        /**
         * Init menu
         */
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_navigation_drawer, R.string.close_navigation_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        /**
         * Get OMAPI Readers
         */
        readers = SmartCardService.getInstance().getPlugin(PLUGIN_NAME).readers

        eventRecyclerView.layoutManager = layoutManager
        eventRecyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        clearEvents()
        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    abstract fun initContentView()

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

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearEvents()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        return true
    }

    override fun onDestroy() {
        SmartCardService.getInstance().plugins.forEach {
            SmartCardService.getInstance().unregisterPlugin(it.key)
        }
        super.onDestroy()
    }
}
