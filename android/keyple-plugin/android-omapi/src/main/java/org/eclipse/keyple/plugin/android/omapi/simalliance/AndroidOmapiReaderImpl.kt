/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.keyple.plugin.android.omapi.simalliance

import java.io.IOException
import java.util.NoSuchElementException

import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.exception.KeypleApplicationSelectionException
import org.eclipse.keyple.core.seproxy.exception.KeypleChannelControlException
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException
import org.eclipse.keyple.core.seproxy.message.*
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractLocalReader
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiReader
import org.simalliance.openmobileapi.Channel
import org.simalliance.openmobileapi.Reader
import org.simalliance.openmobileapi.Session
import timber.log.Timber
import kotlin.experimental.or

/**
 * Implementation of the [AndroidOmapiReader] based on the [AbstractLocalReader]
 * with org.simalliance.omapi
 */
internal class AndroidOmapiReaderImpl(private val nativeReader: Reader, pluginName: String,readerName: String)
    : AndroidOmapiReader(pluginName, readerName){

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
     * @throws KeypleIOReaderException
     * @throws KeypleChannelControlException
     * @throws KeypleApplicationSelectionException
     */
    @Throws(KeypleIOReaderException::class, KeypleChannelControlException::class, KeypleApplicationSelectionException::class)
    override fun openChannelForAid(aidSelector: SeSelector.AidSelector): ApduResponse {
        if (aidSelector.aidToSelect == null) {
            try {
                openChannel = session?.openBasicChannel(null)
            } catch (e: IOException) {
                e.printStackTrace()
                throw KeypleIOReaderException("IOException while opening basic channel.")
            } catch (e: SecurityException) {
                throw KeypleChannelControlException("Error while opening basic channel, SE_SELECTOR = $aidSelector", e.cause)
            }

            if (openChannel == null) {
                throw KeypleIOReaderException("Failed to open a basic channel.")
            }
        } else {
            Timber.i("[%s] openLogicalChannel => Select Application with AID = %s",
                    this.name, ByteArrayUtil.toHex(aidSelector.aidToSelect.value))
            try {
                //openLogicalChannel of SimAlliance OMAPI is only available for version 3.0+ of the library.
                //By default the library always pass p2=00h
                //So if a p2 different of 00h is passed, we must check if omapi support it. otherwise we throw an exception
                val p2 = aidSelector.fileOccurrence.isoBitMask or aidSelector.fileControlInformation.isoBitMask or 2.toByte()
                openChannel =
                        if("00".toByte() == p2){
                            session?.openLogicalChannel(aidSelector.aidToSelect.value)
                        }else{
                            if(omapiVersion >= 3){
                                session?.openLogicalChannel(aidSelector.aidToSelect.value, p2)
                            }else{
                                throw IOException(String.format("P2 != 0 while opening logical channel is only supported by OMAPI version >= 3.0. Current is %s", omapiVersion.toString()))
                            }
                        }

            } catch (e: IOException) {
                Timber.e(e, "IOException")
                throw KeypleIOReaderException("IOException while opening logical channel.", e)
            } catch (e: NoSuchElementException) {
                Timber.e(e, "NoSuchElementException")
                throw KeypleApplicationSelectionException(
                        "NoSuchElementException: " + ByteArrayUtil.toHex(aidSelector.aidToSelect.value), e)
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException")
                throw KeypleChannelControlException("SecurityException while opening logical channel, aid :" + ByteArrayUtil.toHex(aidSelector.aidToSelect.value), e.cause)
            }

            Session::class.java.declaredMethods

            if (openChannel == null) {
                throw KeypleIOReaderException("Failed to open a logical channel.")
            }
        }
        /* get the FCI and build an ApduResponse */
        return ApduResponse(openChannel?.selectResponse, aidSelector.successfulSelectionStatusCodes)
    }

    override fun isPhysicalChannelOpen(): Boolean {
        return session?.isClosed == false
    }

    @Throws(KeypleChannelControlException::class)
    public override fun openPhysicalChannel() {
        try {
            session = nativeReader.openSession()
        } catch (e: IOException) {
            Timber.e(e, "IOException")
            throw KeypleChannelControlException("IOException while opening physical channel.", e)
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
     * @throws KeypleIOReaderException if error while sending or receiving bytes
     */
    @Throws(KeypleIOReaderException::class)
    override fun transmitApdu(apduIn: ByteArray): ByteArray {
        // Initialization
        Timber.d("Data Length to be sent to tag : %s", apduIn.size)
        Timber.d("Data in : %s", ByteArrayUtil.toHex(apduIn))
        var dataOut = byteArrayOf(0)
        try {
            openChannel?.let {
                dataOut = it.transmit(apduIn)
            }
        } catch (e: IOException) {
            throw KeypleIOReaderException("Error while transmitting APDU", e)
        }

        Timber.d("Data out : " + ByteArrayUtil.toHex(dataOut))
        return dataOut
    }
}
