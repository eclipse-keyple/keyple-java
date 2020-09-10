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

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.calypsoExampleButton
import kotlinx.android.synthetic.main.activity_main.coreExamplesButton
import kotlinx.android.synthetic.main.activity_main.toolbar
import org.eclipse.keyple.example.calypso.android.nfc.R

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.title = "Android NFC Plugin"
        actionBar?.subtitle = "Example application"

        coreExamplesButton.setOnClickListener(this)
        calypsoExampleButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
                R.id.calypsoExampleButton -> startActivity(Intent(this, CalypsoExamplesActivity::class.java))
                R.id.coreExamplesButton -> startActivity(Intent(this, CoreExamplesActivity::class.java))
                else -> {}
            }
        }
    }
}
