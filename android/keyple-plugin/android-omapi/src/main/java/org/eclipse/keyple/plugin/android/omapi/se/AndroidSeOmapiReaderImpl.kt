package org.eclipse.keyple.plugin.android.omapi.se

import android.se.omapi.Channel
import android.se.omapi.Reader
import android.se.omapi.Session
import android.util.Log
import androidx.annotation.RequiresApi
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.exception.KeypleApplicationSelectionException
import org.eclipse.keyple.core.seproxy.exception.KeypleChannelControlException
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException
import org.eclipse.keyple.core.seproxy.message.ApduResponse
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractLocalReader
import org.eclipse.keyple.core.seproxy.plugin.local.SmartSelectionReader
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiReader
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.HashMap
import java.util.NoSuchElementException
import kotlin.experimental.or

/**
 * Implementation of the {@link AndroidOmapiReader} based on the {@link AbstractLocalReader}
 * with android.se.omapi
 */
@RequiresApi(android.os.Build.VERSION_CODES.P)
class AndroidSeOmapiReaderImpl(pluginName: String, private val omapiReader: Reader, readerName: String)
    : AbstractLocalReader(pluginName, readerName), AndroidOmapiReader, SmartSelectionReader{

    private val logger = LoggerFactory.getLogger(AndroidSeOmapiReaderImpl::class.java)

    private val TAG = AndroidSeOmapiReaderImpl::class.java.simpleName

    private var session: Session? = null
    private var openChannel: Channel? = null
    private val parameters: MutableMap<String, String> = HashMap()

    override fun getParameters(): Map<String, String> {
        Log.w(TAG, "No parameters are supported by AndroidOmapiReaderImpl")
        return parameters
    }

    override fun setParameter(key: String, value: String) {
        Log.w(TAG, "No parameters are supported by AndroidOmapiReaderImpl")
        parameters[key] = value
    }

    /**
     * The transmission mode is always CONTACTS in an OMAPI reader
     *
     * @return the current transmission mode
     */
    override fun getTransmissionMode(): TransmissionMode {
        return TransmissionMode.CONTACTS
    }

    /**
     * Check if a SE is present in this reader. see {@link Reader#isSecureElementPresent()}
     * @return True if the SE is present, false otherwise
     */
    override fun checkSePresence(): Boolean {
        return omapiReader.isSecureElementPresent
    }

    /**
     * Get the SE Answer To Reset
     * @return a byte array containing the ATR or null if no session was available
     */
    override fun getATR(): ByteArray? {
        return session?.let {
            Log.i(TAG, "Retrieving ATR from session...")
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
    @Throws(KeypleIOReaderException::class,KeypleChannelControlException::class,KeypleApplicationSelectionException::class)
    override fun openChannelForAid(aidSelector: SeSelector.AidSelector?): ApduResponse {
        if (aidSelector?.aidToSelect == null) {
            try {
                openChannel = session?.openBasicChannel(null)
            } catch (e: IOException) {
                Log.e(TAG, "IOException", e)
                throw KeypleIOReaderException("IOException while opening basic channel.")
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException", e)
                throw KeypleChannelControlException("Error while opening basic channel, SE_SELECTOR = " + aidSelector.toString(), e.cause)
            }

            if (openChannel == null) {
                throw KeypleIOReaderException("Failed to open a basic channel.")
            }
        } else {
            if (logger.isTraceEnabled) {
                logger.trace("[{}] openLogicalChannel => Select Application with AID = {}",
                        this.name, ByteArrayUtil.toHex(aidSelector.aidToSelect.value))
            }
            try {
                openChannel =
                        session?.openLogicalChannel(aidSelector.aidToSelect.value,
                        aidSelector.fileOccurrence.isoBitMask or aidSelector.fileControlInformation.isoBitMask)
            } catch (e: IOException) {
                Log.e(TAG, "IOException", e)
                throw KeypleIOReaderException("IOException while opening logical channel.")
            } catch (e: NoSuchElementException) {
                Log.e(TAG, "NoSuchElementException", e)
                throw KeypleApplicationSelectionException(
                        "Error while selecting application : " + ByteArrayUtil.toHex(aidSelector.getAidToSelect().value), e)
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException", e)
                throw KeypleChannelControlException("Error while opening logical channel, aid :" + ByteArrayUtil.toHex(aidSelector.getAidToSelect().value), e.cause)
            }

            if (openChannel == null) {
                throw KeypleIOReaderException("Failed to open a logical channel.")
            }
        }
        /* get the FCI and build an ApduResponse */
        return ApduResponse(openChannel?.selectResponse, aidSelector?.successfulSelectionStatusCodes)
    }

    override fun isPhysicalChannelOpen(): Boolean {
        return session?.isClosed == false
    }

    @Throws(KeypleChannelControlException::class)
    override fun openPhysicalChannel() {
        try {
            session = omapiReader.openSession()
        } catch (e: IOException) {
            Log.e(TAG, "IOException", e)
            throw KeypleChannelControlException("IOException while opening physical channel.")
        }
    }

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
    override fun transmitApdu(apduIn: ByteArray): ByteArray {
        // Initialization
        Log.d(TAG, "Data Length to be sent to tag : " + apduIn.size)
        Log.d(TAG, "Data in : " + ByteArrayUtil.toHex(apduIn))
        var dataOut = byteArrayOf(0)
        try {
            openChannel?.let {
                dataOut = it.transmit(apduIn)
            }
        } catch (e: IOException) {
            throw KeypleIOReaderException("Error while transmitting APDU", e)
        }

        Log.d(TAG, "Data out : " + ByteArrayUtil.toHex(dataOut))
        return dataOut
    }

    /**
     * Check that protocolFlag is PROTOCOL_ISO7816_3
     * @param protocolFlag
     * @return true if match PROTOCOL_ISO7816_3
     */
    override fun protocolFlagMatches(protocolFlag: SeProtocol?): Boolean {
        return protocolFlag == SeCommonProtocols.PROTOCOL_ISO7816_3
    }

}