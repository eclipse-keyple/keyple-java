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
package org.eclipse.keyple.plugin.android.omapi.se

import android.se.omapi.Channel
import android.se.omapi.Reader
import android.se.omapi.Session
import androidx.annotation.RequiresApi
import java.io.IOException
import java.util.NoSuchElementException
import kotlin.experimental.or
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException
import org.eclipse.keyple.core.seproxy.message.ApduResponse
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.AbstractAndroidOmapiReader
import timber.log.Timber

/**
 * Implementation of the {@link AndroidOmapiReader} based on the {@link AbstractLocalReader}
 * with android.se.omapi
 */
@RequiresApi(android.os.Build.VERSION_CODES.P)
internal class AndroidOmapiReader(private val nativeReader: Reader, pluginName: String, readerName: String) :
    AbstractAndroidOmapiReader(pluginName, readerName) {

    private var session: Session? = null
    private var openChannel: Channel? = null

    /**
     * Check if a SE is present in this reader. see {@link Reader#isSecureElementPresent()}
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
                Timber.e(e, "IOException")
                throw KeypleReaderIOException("IOException while opening basic channel.")
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException")
                throw KeypleReaderIOException("Error while opening basic channel, SE_SELECTOR = " + aidSelector.toString(), e.cause)
            }

            if (openChannel == null) {
                throw KeypleReaderIOException("Failed to open a basic channel.")
            }
        } else {
            Timber.i("[%s] openLogicalChannel => Select Application with AID = %s",
                    this.name, ByteArrayUtil.toHex(aidSelector.aidToSelect))
            try {
                openChannel =
                        session?.openLogicalChannel(aidSelector.aidToSelect,
                        aidSelector.fileOccurrence.isoBitMask or aidSelector.fileControlInformation.isoBitMask)
            } catch (e: IOException) {
                Timber.e(e, "IOException")
                throw KeypleReaderIOException("IOException while opening logical channel.")
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
    override fun openPhysicalChannel() {
        try {
            session = nativeReader.openSession()
        } catch (e: IOException) {
            Timber.e(e, "IOException")
            throw KeypleReaderIOException("IOException while opening physical channel.", e)
        }
    }

    override fun closePhysicalChannel() {
        openChannel?.let {
            it.session.close()
            openChannel = null
        }
    }

    /**
     * Activates the provided SE protocol.
     *
     *
     *  * Ask the plugin to take this protocol into account if an SE using this protocol is
     * identified during the selection phase.
     *  * Activates the detection of SEs using this protocol (if the plugin allows it).
     *
     *
     * @param seProtocol The protocol to activate (must be not null).
     * @throws KeypleReaderProtocolNotSupportedException if the protocol is not supported.
     */
    override fun activateProtocol(seProtocol: SeProtocol?) {
        TODO("Not yet implemented")
    }

    /**
     * Deactivates the provided SE protocol.
     *
     *
     *  * Ask the plugin to ignore this protocol if an SE using this protocol is identified during
     * the selection phase.
     *  * Inhibits the detection of SEs using this protocol (if the plugin allows it).
     *
     *
     * @param seProtocol The protocol to deactivate (must be not null).
     */
    override fun deactivateProtocol(seProtocol: SeProtocol?) {
        TODO("Not yet implemented")
    }

    /**
     * Gets the communication protocol used by the current SE.
     *
     *
     * This method must be implemented by the plugin which is able to determine the protocol of the
     * SE from the technical data it has available.
     *
     * @return A not null reference to a [SeProtocol].
     * @throws KeypleReaderProtocolNotFoundException if it is not possible to determine the protocol.
     * @since 1.0
     */
    override fun getCurrentProtocol(): SeProtocol {
        return SeCommonProtocols.PROTOCOL_ISO7816_3 // TODO check this
    }

    /** Closes the logical channel explicitly.  */
    override fun closeLogicalChannel() {
        session?.closeChannels()
    }

    /**
     * Transmit an APDU command (as per ISO/IEC 7816) to the SE see org.simalliance.openmobileapi.Channel#transmit(byte[])
     *
     * @param apduIn byte buffer containing the ingoing data
     * @return apduOut response
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    override fun transmitApdu(apduIn: ByteArray): ByteArray {
        // Initialization
        Timber.d("Data Length to be sent to tag : " + apduIn.size)
        Timber.d("Data in : " + ByteArrayUtil.toHex(apduIn))
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
