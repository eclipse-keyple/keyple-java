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
import kotlin.experimental.or
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException
import org.eclipse.keyple.core.seproxy.message.ApduResponse
import org.eclipse.keyple.core.seproxy.plugin.reader.AbstractLocalReader
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiReader
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
     * Check if a SE is present in this reader. see [Reader.isSecureElementPresent]
     * @return True if the SE is present, false otherwise
     */
    override fun checkSePresence(): Boolean {
        return nativeReader.isSecureElementPresent
    }

    /**
     * Get the SE Answer To Reset
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
     * @param aidSelector the selection parameters
     * @return a ApduResponse built from the FCI data resulting from the application selection
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    @Throws(KeypleReaderIOException::class)
    override fun openChannelForAid(aidSelector: SeSelector.AidSelector): ApduResponse {
        if (aidSelector.aidToSelect == null) {
            try {
                openChannel = session?.openBasicChannel(null)
            } catch (e: IOException) {
                Timber.e(e)
                throw KeypleReaderIOException("IOException while opening basic channel.")
            } catch (e: SecurityException) {
                Timber.e(e)
                throw KeypleReaderIOException("Error while opening basic channel, SE_SELECTOR = $aidSelector", e.cause)
            }

            if (openChannel == null) {
                throw KeypleReaderIOException("Failed to open a basic channel.")
            }
        } else {
            Timber.i("[%s] openLogicalChannel => Select Application with AID = %s",
                    this.name, ByteArrayUtil.toHex(aidSelector.aidToSelect))
            try {
                // openLogicalChannel of SimAlliance OMAPI is only available for version 3.0+ of the library.
                // By default the library always passes p2=00h
                // So if a p2 different of 00h is requested, we must check if omapi support it. Otherwise we throw an exception.
                val p2 = aidSelector.fileOccurrence.isoBitMask or aidSelector.fileControlInformation.isoBitMask
                openChannel =
                        if (0 == p2.toInt()) {
                            session?.openLogicalChannel(aidSelector.aidToSelect)
                        } else {
                            if (omapiVersion >= P2_SUPPORTED_MIN_VERSION) {
                                session?.openLogicalChannel(aidSelector.aidToSelect, p2)
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
                        "NoSuchElementException: " + ByteArrayUtil.toHex(aidSelector.aidToSelect), e)
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException")
                throw KeypleReaderIOException("SecurityException while opening logical channel, aid :" + ByteArrayUtil.toHex(aidSelector.aidToSelect), e.cause)
            }

            if (openChannel == null) {
                throw KeypleReaderIOException("Failed to open a logical channel.")
            }
        }
        /* get the FCI and build an ApduResponse */
        return ApduResponse(openChannel?.selectResponse, aidSelector.successfulSelectionStatusCodes)
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
     * Transmit an APDU command (as per ISO/IEC 7816) to the SE see org.simalliance.openmobileapi.Channel#transmit(byte[])
     *
     * @param apduIn byte buffer containing the ingoing data
     * @return apduOut response
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
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
