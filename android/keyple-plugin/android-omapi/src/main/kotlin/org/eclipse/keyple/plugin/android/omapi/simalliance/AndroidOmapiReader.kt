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
package org.eclipse.keyple.plugin.android.omapi.simalliance

import java.io.IOException
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiReader
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiSupportedProtocols
import org.simalliance.openmobileapi.Channel
import org.simalliance.openmobileapi.Reader
import org.simalliance.openmobileapi.Session
import timber.log.Timber

/**
 * Implementation of the [AbstractAndroidOmapiReader] based on the [AbstractLocalReader]
 * with org.simalliance.omapi
 */
internal class AndroidOmapiReader(private val nativeReader: Reader, pluginName: String, readerName: String) :
    AbstractAndroidOmapiReader(pluginName, readerName) {

    companion object {
        private const val P2_SUPPORTED_MIN_VERSION = 3
    }

    private var session: Session? = null
    private var openChannel: Channel? = null
    private val omapiVersion = nativeReader.seService.version.toFloat()

    /**
     * Check if a card is present in this reader. see [Reader.isSecureElementPresent]
     * @return True if the card is present, false otherwise
     */
    override fun checkCardPresence(): Boolean {
        return nativeReader.isSecureElementPresent
    }

    /**
     * Get the card Answer To Reset
     * @return a byte array containing the ATR or null if no session was available
     */
    override fun getATR(): ByteArray? {
        return session?.let {
            Timber.i("Retrieving ATR from session...")
            it.atr
        }
    }

    /**
     * Open a logical channel by selecting the application
     * @param dfName A byte array containing the DF name or null if a basic opening is wanted.
     * @param isoControlMask The selection bits defined by the ISO selection command and expected by the OMAPI as P2 parameter.
     * @return A byte array containing the response to the OMAPI openLogicalChannel process or null if the Secure Element is unable to
     *         provide a new logical channel
     * @throws KeypleReaderIOException if the communication with the reader or the card has failed
     */
    @Throws(KeypleReaderIOException::class)
    override fun openChannelForAid(dfName: ByteArray?, isoControlMask: Byte): ByteArray? {
        if (dfName == null) {
            try {
                openChannel = session?.openBasicChannel(null)
            } catch (e: IOException) {
                Timber.e(e)
                throw KeypleReaderIOException("IOException while opening basic channel.")
            } catch (e: SecurityException) {
                Timber.e(e)
                throw KeypleReaderIOException("Error while opening basic channel, DFNAME = " + ByteArrayUtil.toHex(dfName), e.cause)
            }

            if (openChannel == null) {
                throw KeypleReaderIOException("Failed to open a basic channel.")
            }
        } else {
            Timber.i("[%s] openLogicalChannel => Select Application with AID = %s",
                    this.name, ByteArrayUtil.toHex(dfName))
            try {
                // openLogicalChannel of SimAlliance OMAPI is only available for version 3.0+ of the library.
                // By default the library always passes p2=00h
                // So if a p2 different of 00h is requested, we must check if omapi support it. Otherwise we throw an exception.
                val p2 = isoControlMask
                openChannel =
                        if (0 == p2.toInt()) {
                            session?.openLogicalChannel(dfName)
                        } else {
                            if (omapiVersion >= P2_SUPPORTED_MIN_VERSION) {
                                session?.openLogicalChannel(dfName, p2)
                            } else {
                                throw KeypleReaderIOException("P2 != 00h while opening logical channel is only supported by OMAPI version >= 3.0. Current is $omapiVersion")
                            }
                        }
            } catch (e: IOException) {
                Timber.e(e, "IOException")
                throw KeypleReaderIOException("IOException while opening logical channel.", e)
            } catch (e: NoSuchElementException) {
                Timber.e(e, "NoSuchElementException")
                throw java.lang.IllegalArgumentException(
                        "NoSuchElementException: " + ByteArrayUtil.toHex(dfName), e)
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException")
                throw KeypleReaderIOException("SecurityException while opening logical channel, aid :" + ByteArrayUtil.toHex(dfName), e.cause)
            }

            if (openChannel == null) {
                throw KeypleReaderIOException("Failed to open a logical channel.")
            }
        }
        /* get the FCI and build an ApduResponse */
        return openChannel!!.selectResponse
    }

    override fun isPhysicalChannelOpen(): Boolean {
        return session?.isClosed == false
    }

    @Throws(KeypleReaderIOException::class)
    public override fun openPhysicalChannel() {
        try {
            session = nativeReader.openSession()
        } catch (e: IOException) {
            Timber.e(e, "IOException")
            throw KeypleReaderIOException("IOException while opening physical channel.", e)
        }
    }

    /**
     * Close session see org.simalliance.openmobileapi.Session#close()
     */
    override fun closePhysicalChannel() {
        openChannel?.let {
            it.session.close()
            openChannel = null
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
        // Do nothing
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
        // Do nothing
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
        return AndroidOmapiSupportedProtocols.ISO_7816_3.name == readerProtocolName
    }

    /** Closes the logical channel explicitly.  */
    override fun closeLogicalChannel() {
        session?.closeChannels()
    }

    /**
     * Transmit an APDU command (as per ISO/IEC 7816) to the card see org.simalliance.openmobileapi.Channel#transmit(byte[])
     *
     * @param apduIn byte buffer containing the ingoing data
     * @return apduOut response
     * @throws KeypleReaderIOException if the communication with the reader or the card has failed
     */
    @Throws(KeypleReaderIOException::class)
    override fun transmitApdu(apduIn: ByteArray): ByteArray {
        // Initialization
        Timber.d("Data Length to be sent to tag : %s", apduIn.size)
        Timber.d("Data in : %s", ByteArrayUtil.toHex(apduIn))
        var dataOut = byteArrayOf(0)
        try {
            openChannel.let {
                dataOut = it?.transmit(apduIn) ?: throw IOException("Channel is not open")
            }
        } catch (e: IOException) {
            throw KeypleReaderIOException("Error while transmitting APDU", e)
        }

        Timber.d("Data out : " + ByteArrayUtil.toHex(dataOut))
        return dataOut
    }
}
