/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.keyple.plugin.android.nfc

import android.content.Intent
import android.nfc.NfcAdapter

import org.eclipse.keyple.core.seproxy.SeReader
import org.eclipse.keyple.core.seproxy.event.ObservableReader

/**
 * [SeReader] to communicate with NFC Tag though
 * Android [NfcAdapter]
 *
 * Configure NFCAdapter Protocols with [AndroidNfcReaderImpl.setParameter]
 *
 * Optimized for android 4.4 (API 19) to  6.0 (API 23)
 */
interface AndroidNfcReader : ObservableReader {
    /**
     * Gets a string describing the low level description of the current tag.
     *
     * Used for logging purpose
     * @return string
     */
    fun printTagId(): String

    /**
     * Process data from NFC Intent
     *
     * @param intent : Intent received and filterByProtocol by xml tech_list
     */
    fun processIntent(intent: Intent)

    companion object {

        val READER_NAME = "AndroidNfcReaderImpl"
        val PLUGIN_NAME = AndroidNfcPlugin.PLUGIN_NAME

        val FLAG_READER_SKIP_NDEF_CHECK = "FLAG_READER_SKIP_NDEF_CHECK"
        val FLAG_READER_NO_PLATFORM_SOUNDS = "FLAG_READER_NO_PLATFORM_SOUNDS"
        val FLAG_READER_PRESENCE_CHECK_DELAY = "FLAG_READER_PRESENCE_CHECK_DELAY"
    }
}
