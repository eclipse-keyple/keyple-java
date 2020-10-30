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
package org.eclipse.keyple.plugin.android.nfc

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import org.eclipse.keyple.core.plugin.reader.SmartRemovalReader
import org.eclipse.keyple.core.service.Reader

/**
 * [Reader] to communicate with NFC Tag though
 * Android [NfcAdapter]
 *
 * Configure NFCAdapter Protocols with [AndroidNfcReaderImpl.setParameter]
 *
 * Optimized for android 4.4 (API 19) to  6.0 (API 23)
 */
interface AndroidNfcReader : SmartRemovalReader {
    /**
     * Gets a string describing the low level description of the current tag.
     *
     * Used for logging purpose
     * @return string
     */
    fun printTagId(): String

    /**
     * Process data from NFC Intent. Can be use to handle NFC Tag received when app is
     * triggered by nfc detection
     *
     * @param intent : Intent received and filterByProtocol by xml tech_list
     */
    fun processIntent(intent: Intent)

    /**
     * Declare app to handle NFC Tags while in the foreground
     */
    fun enableNFCReaderMode(activity: Activity)

    /**
     * Stop app handling NFC Tags while in the foreground
     */
    fun disableNFCReaderMode(activity: Activity)

    /**
     * Configure NFC Reader
     */
    fun setParameter(key: String, value: String)

    /**
     * Get Reader parameters
     *
     * @return parameters
     */
    fun getParameters(): Map<String, String?>

    companion object {

        val READER_NAME = "AndroidNfcReaderImpl"
        val PLUGIN_NAME = AndroidNfcPlugin.PLUGIN_NAME

        // FLAG_READER_SKIP_NDEF_CHECK Prevent the platform from performing any NDEF checks in reader mode. Must be 0 or 1.
        val FLAG_READER_SKIP_NDEF_CHECK = "FLAG_READER_SKIP_NDEF_CHECK"

        // Allows the caller to prevent the platform from playing sounds when it discovers a tag. Must be 0 or 1.
        val FLAG_READER_NO_PLATFORM_SOUNDS = "FLAG_READER_NO_PLATFORM_SOUNDS"

        // Allows the calling application to specify the delay that the platform will use for performing presence checks on any discovered tag.
        val FLAG_READER_PRESENCE_CHECK_DELAY = "FLAG_READER_PRESENCE_CHECK_DELAY"
    }
}
