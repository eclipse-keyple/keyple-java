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
import android.nfc.Tag
import android.os.Bundle
import org.eclipse.keyple.core.plugin.AbstractObservableLocalAutonomousReader
import org.eclipse.keyple.core.plugin.AbstractObservableLocalReader
import org.eclipse.keyple.core.plugin.DontWaitForCardRemovalDuringProcessing
import org.eclipse.keyple.core.plugin.WaitForCardInsertionAutonomous
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler
import org.eclipse.keyple.core.service.exception.KeypleReaderException
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException
import org.eclipse.keyple.core.util.ByteArrayUtil
import timber.log.Timber
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.HashMap

/**
 *
 * Abstract implementation of [AndroidNfcReader] based on keyple core abstract classes [AbstractObservableLocalReader]
 * and
 */
internal abstract class AbstractAndroidNfcReader(activity: Activity, readerObservationExceptionHandler: ReaderObservationExceptionHandler) :
    AbstractObservableLocalAutonomousReader(
        AndroidNfcReader.PLUGIN_NAME,
        AndroidNfcReader.READER_NAME
    ),
    AndroidNfcReader, NfcAdapter.ReaderCallback, WaitForCardInsertionAutonomous,
    DontWaitForCardRemovalDuringProcessing {

    companion object {
        private const val NO_TAG = "no-tag"
    }

    private val readerObservationExceptionHandler: ReaderObservationExceptionHandler
    private var contextWeakRef: WeakReference<Activity?>

    init {
        this.contextWeakRef = WeakReference(activity)
        this.readerObservationExceptionHandler = readerObservationExceptionHandler
    }

    // keep state between session if required
    private var tagProxy: TagProxy? = null

    private val protocolsMap = HashMap<String, String?>()

    // Android NFC Adapter
    protected var nfcAdapter: NfcAdapter? = null

    private var mPresenceCheckDelay: Int? = null
    private var mNoPlateformSound: Boolean? = null
    private var mSkipNdefCheck: Boolean? = null

    override var presenceCheckDelay: Int?
        get() = mPresenceCheckDelay
        set(value) {
            mPresenceCheckDelay = value
        }

    override var noPlateformSound: Boolean?
        get() = mNoPlateformSound
        set(value) {
            mNoPlateformSound = value
        }

    override var skipNdefCheck: Boolean?
        get() = mSkipNdefCheck
        set(value) {
            mSkipNdefCheck = value
        }

    /**
     * Build Reader Mode flags Integer from parameters
     *
     * @return flags Integer
     */
    // Build flags list for reader mode
    val flags: Int
        get() {
            var flags = 0

            if (skipNdefCheck != null && skipNdefCheck == true) {
                flags = flags or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
            }

            if (noPlateformSound != null && noPlateformSound == true) {
                flags = flags or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS
            }
            for (cardProtocol in this.protocolsMap.keys) {
                if (AndroidNfcSupportedProtocols.ISO_14443_4.name == cardProtocol) {
                    flags = flags or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_NFC_A
                } else if (AndroidNfcSupportedProtocols.MIFARE_ULTRA_LIGHT.name == cardProtocol || AndroidNfcSupportedProtocols.MIFARE_CLASSIC.name == cardProtocol) {
                    flags = flags or NfcAdapter.FLAG_READER_NFC_A
                }
            }

            return flags
        }

    /**
     * Build Reader Mode options Bundle from parameters
     *
     * @return options
     */
    val options: Bundle
        get() {
            val options = Bundle(1)
            presenceCheckDelay?.let { delay ->
                options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, delay)
            }
            return options
        }

    override fun clearContext() {
        contextWeakRef.clear()
        contextWeakRef = WeakReference(null)

        nfcAdapter = null
    }

    /**
     * The transmission mode is always CONTACTLESS in a NFC reader
     *
     * @return Always true.
     */
    override fun isContactless(): Boolean {
        return true
    }

    /**
     * Callback function invoked by @[NfcAdapter] when a @[Tag] is discovered A
     * TagTransceiver is created based on the Tag technology see [TagProxy.getTagProxy]
     * Do not call this function directly.
     *
     * @param tag : detected tag
     */
    override fun onTagDiscovered(tag: Tag?) {
        Timber.i("Received Tag Discovered event $tag")
        tag?.let {
            try {
                Timber.i("Getting tag proxy")
                tagProxy = TagProxy.getTagProxy(tag)
                onCardInserted()
            } catch (e: KeypleReaderException) {
                Timber.e(e)
            }
        }
    }

    /**
     *
     * @return true if a card is present
     */
    public override fun checkCardPresence(): Boolean {
        return tagProxy != null
    }

    public override fun getATR(): ByteArray? {
        val atr = tagProxy?.atr
        Timber.d("ATR : ${ByteArrayUtil.toHex(atr)}")
        return if (atr?.isNotEmpty() == true) atr else null
    }

    public override fun isPhysicalChannelOpen(): Boolean {
        return tagProxy?.isConnected == true
    }

    @Throws(KeypleReaderIOException::class)
    public override fun openPhysicalChannel() {
        if (tagProxy?.isConnected != true) {
            try {
                Timber.d("Connect to tag..")
                tagProxy?.connect()
                Timber.i("Tag connected successfully : ${printTagId()}")
            } catch (e: IOException) {
                Timber.e(e, "Error while connecting to Tag ")
                throw KeypleReaderIOException("Error while opening physical channel", e)
            }
        } else {
            Timber.i("Tag is already connected to : ${printTagId()}")
        }
    }

    @Throws(KeypleReaderIOException::class)
    public override fun closePhysicalChannel() {
        try {
            tagProxy?.close()
            Timber.i("Disconnected tag : ${printTagId()}")
        } catch (e: IOException) {
            Timber.e(e, "Disconnecting error")
            throw KeypleReaderIOException("Error while closing physical channel", e)
        } finally {
            tagProxy = null
        }
    }

    @Throws(KeypleReaderIOException::class)
    public override fun transmitApdu(apduIn: ByteArray): ByteArray {
        Timber.d("Send data to card : ${apduIn.size} bytes")
        return with(tagProxy) {
            if (this == null) {
                throw KeypleReaderIOException(
                        "Error while transmitting APDU, invalid out data buffer"
                )
            } else {
                try {
                    val bytes = transceive(apduIn)
                    if (bytes.size < 2) {
                        throw KeypleReaderIOException(
                                "Error while transmitting APDU, invalid out data buffer"
                        )
                    } else {
                        Timber.d("Receive data from card : ${ByteArrayUtil.toHex(bytes)}")
                        bytes
                    }
                } catch (e: IOException) {
                    throw KeypleReaderIOException(
                            "Error while transmitting APDU, invalid out data buffer", e
                    )
                } catch (e: NoSuchElementException) {
                    throw KeypleReaderIOException(
                            "Error while transmitting APDU, no such Element",
                            e
                    )
                }
            }
        }
    }

    /**
     * Activates the provided card protocol.
     *
     *
     *  * Ask the plugin to take this protocol into account if a card using this protocol is
     * identified during the selection phase.
     *  * Activates the detection of SEs using this protocol (if the plugin allows it).
     *
     *
     * @param readerProtocolName The protocol to activate (must be not null).
     * @throws KeypleReaderProtocolNotSupportedException if the protocol is not supported.
     */
    override fun activateReaderProtocol(readerProtocolName: String?) {
        if (!protocolsMap.containsKey(readerProtocolName)) {
            protocolsMap[readerProtocolName!!] =
                    AndroidNfcProtocolSettings.getSetting(readerProtocolName)
        }
        Timber.d("$name: Activate protocol $readerProtocolName with rule \"${protocolsMap[readerProtocolName]}\".")
    }

    /**
     * Deactivates the provided card protocol.
     *
     *
     *  * Ask the plugin to ignore this protocol if a card using this protocol is identified during
     * the selection phase.
     *  * Inhibits the detection of SEs using this protocol (if the plugin allows it).
     *
     *
     * @param readerProtocolName The protocol to deactivate (must be not null).
     */
    override fun deactivateReaderProtocol(readerProtocolName: String?) {
        if (protocolsMap.containsKey(readerProtocolName)) {
            protocolsMap.remove(readerProtocolName)
        }
        Timber.d("$name: Deactivate protocol $readerProtocolName.")
    }

    /**
     * Tells if the provided protocol matches the current protocol.
     *
     * @param readerProtocolName A not empty String.
     * @return True or false
     * @throws KeypleReaderProtocolNotFoundException if it is not possible to determine the protocol.
     * @since 1.0
     */
    override fun isCurrentProtocol(readerProtocolName: String?): Boolean {
        return readerProtocolName == null || protocolsMap.containsKey(readerProtocolName) && protocolsMap[readerProtocolName] == tagProxy?.tech
    }

    override fun onStartDetection() {
        Timber.d("onStartDetection")
        if (contextWeakRef.get() == null) {
            throw IllegalStateException("onStartDetection() failed : no context available")
        }

        if (nfcAdapter == null) {
            nfcAdapter = NfcAdapter.getDefaultAdapter(contextWeakRef.get()!!)
        }

        val flags = flags

        val options = options

        Timber.i("Enabling Read Write Mode with flags : $flags and options : $options")

        // Reader mode for NFC reader allows to listen to NFC events without the Intent mechanism.
        // It is active only when the activity thus the fragment is active.
        nfcAdapter?.enableReaderMode(contextWeakRef.get(), this, flags, options)
    }

    override fun onStopDetection() {
        Timber.d("onStopDetection")
        nfcAdapter?.let {
            if (contextWeakRef.get() != null) {
                it.disableReaderMode(contextWeakRef.get())
            } else {
                throw IllegalStateException("onStopDetection failed : no context available")
            }
        }
    }

    override fun unregister() {
        super.unregister()
        clearContext()
    }

    /**
     * Process data from NFC Intent
     *
     * @param intent : Intent received and filterByProtocol by xml tech_list
     */
    override fun processIntent(intent: Intent) {
        // Extract Tag from Intent
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        this.onTagDiscovered(tag)
    }

    override fun printTagId(): String {
        return with(tagProxy) {
            if (this == null) {
                NO_TAG
            } else {
                // build a user friendly TechList
                val techList = tag.techList.joinToString(separator = ", ") {
                    it.replace(
                            "android.nfc.tech.",
                            ""
                    )
                }
                // build a hexa TechId
                val tagId = tag.id.joinToString(separator = " ") { String.format("%02X", it) }
                "$tagId - $techList"
            }
        }
    }

    protected fun getTagProxyTag(): Tag? {
        return tagProxy?.tag
    }

    override fun getObservationExceptionHandler(): ReaderObservationExceptionHandler {
        return readerObservationExceptionHandler
    }
}
