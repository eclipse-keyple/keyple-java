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

import android.content.Intent
import android.nfc.NfcAdapter
import org.eclipse.keyple.core.service.Reader
import org.eclipse.keyple.core.service.event.ObservableReader

/**
 * [Reader] to communicate with NFC Tag though
 * Android [NfcAdapter]
 *
 * Configure NFCAdapter Protocols with [AbstractAndroidNfcReader.setParameter]
 *
 * @since 0.9
 */
interface AndroidNfcReader : ObservableReader {
    /**
     * Gets a string describing the low level description of the current tag.
     *
     * Used for logging purpose
     * @return string
     *
     * @since 0.9
     */
    fun printTagId(): String

    /**
     * Process data from NFC Intent. Can be use to handle NFC Tag received when app is started
     * by nfc detection
     *
     * @param intent : Intent received and filterByProtocol by xml tech_list
     *
     * @since 0.9
     */
    fun processIntent(intent: Intent)

    /**
     * Allows the calling application to specify the delay that the platform will use for performing presence checks on any discovered tag.
     * see @NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY
     *
     * @since 0.9
     */
    var presenceCheckDelay: Int?

    /**
     * Allows the invoker to prevent the platform from playing sounds when it discovers a tag.
     *
     * @since 0.9
     */
    var noPlateformSound: Boolean?

    /**
     * Prevent the platform from performing any NDEF checks in reader mode.
     *
     * @since 0.9
     */
    var skipNdefCheck: Boolean?

    companion object {
        val READER_NAME = "AndroidNfcReader"
        val PLUGIN_NAME = AndroidNfcPlugin.PLUGIN_NAME
    }
}
