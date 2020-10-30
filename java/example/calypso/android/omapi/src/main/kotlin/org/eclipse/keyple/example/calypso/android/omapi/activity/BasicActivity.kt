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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.eclipse.keyple.core.service.SmartCardService
import org.eclipse.keyple.core.service.exception.KeyplePluginInstantiationException
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiPluginFactory

abstract class BasicActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* register Omapi Plugin to the SmartCardService */
        try {
            SmartCardService.getInstance().registerPlugin(AndroidOmapiPluginFactory(this))
        } catch (e: KeyplePluginInstantiationException) {
            e.printStackTrace()
        }
    }
    fun initActionBar(toolbar: Toolbar, title: String, subtitle: String) {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = title
        actionBar?.subtitle = subtitle
    }

    fun showAlertDialog(t: Throwable) {
        val builder = AlertDialog.Builder(this@BasicActivity)
        builder.setTitle(R.string.alert_dialog_title)
        builder.setMessage(getString(R.string.alert_dialog_message, t.message))
        val dialog = builder.create()
        dialog.show()
    }
}
