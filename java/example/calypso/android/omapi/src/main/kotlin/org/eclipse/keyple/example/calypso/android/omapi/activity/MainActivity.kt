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

import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.calypsoExampleButton
import kotlinx.android.synthetic.main.activity_main.connectOmapiTV
import kotlinx.android.synthetic.main.activity_main.coreExamplesButton
import kotlinx.android.synthetic.main.activity_main.menuLayout
import kotlinx.android.synthetic.main.activity_main.progressBar
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.keyple.core.seproxy.Reader
import org.eclipse.keyple.core.seproxy.SmartCardService
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginInstantiationException
import org.eclipse.keyple.core.seproxy.exception.KeyplePluginNotFoundException
import org.eclipse.keyple.example.calypso.android.omapi.R
import org.eclipse.keyple.plugin.android.omapi.PLUGIN_NAME
import timber.log.Timber

class MainActivity : BasicActivity(), View.OnClickListener {

    companion object {
        private const val MAX_TRIES = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initActionBar(toolbar, "OMAPI Plugin", "Examples application")

        coreExamplesButton.setOnClickListener(this)
        calypsoExampleButton.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        // Retrieve OMAPI Readers, can't observed but may take time.
        // So we retry every second 10 times
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                connectOmapi()
                // readers are initialized, we can show the menu
                connectOmapiTV.visibility = View.GONE
                menuLayout.visibility = View.VISIBLE
            } catch (e: KeyplePluginNotFoundException) {
                Timber.e(e)
                showAlertDialog(e)
            } catch (e: KeyplePluginInstantiationException) {
                Timber.e(e)
                showAlertDialog(e)
            } finally {
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    @Throws(KeyplePluginNotFoundException::class, KeyplePluginInstantiationException::class)
    private suspend fun connectOmapi(): Map<String, Reader> = withContext(Dispatchers.IO) {
        var readers: Map<String, Reader> ? = null
        for (x in 1..MAX_TRIES) {
            readers = SmartCardService.getInstance().getPlugin(PLUGIN_NAME).readers
            if (readers == null || readers.size < 1) {
                Timber.d("No readers found in OMAPI Keyple Plugin")
                Timber.d("Retrying in 1 second")
                delay(1000)
            } else {
                Timber.d("Readers Found")
                break
            }
        }

        readers ?: throw KeyplePluginInstantiationException(getString(R.string.error_no_reader_found, MAX_TRIES))
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
