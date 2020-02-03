package org.eclipse.keyple.plugin.android.omapi.se

import android.se.omapi.Channel
import android.se.omapi.Reader
import android.se.omapi.Session
import androidx.annotation.RequiresApi
import java.io.IOException
import java.util.NoSuchElementException
import kotlin.experimental.or
import org.eclipse.keyple.core.seproxy.SeSelector
import org.eclipse.keyple.core.seproxy.exception.KeypleApplicationSelectionException
import org.eclipse.keyple.core.seproxy.exception.KeypleChannelControlException
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException
import org.eclipse.keyple.core.seproxy.message.ApduResponse
import org.eclipse.keyple.core.util.ByteArrayUtil
import org.eclipse.keyple.plugin.android.omapi.AndroidOmapiReader
import timber.log.Timber

/**
 * Implementation of the {@link AndroidOmapiReader} based on the {@link AbstractLocalReader}
 * with android.se.omapi
 */
@RequiresApi(android.os.Build.VERSION_CODES.P)
internal class AndroidOmapiReaderImpl(private val nativeReader: Reader, pluginName: String, readerName: String) :
    AndroidOmapiReader(pluginName, readerName) {

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
                Timber.e(e, "IOException")
                throw KeypleIOReaderException("IOException while opening basic channel.")
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException")
                throw KeypleChannelControlException("Error while opening basic channel, SE_SELECTOR = " + aidSelector.toString(), e.cause)
            }

            if (openChannel == null) {
                throw KeypleIOReaderException("Failed to open a basic channel.")
            }
        } else {
            Timber.i("[%s] openLogicalChannel => Select Application with AID = %s",
                    this.name, ByteArrayUtil.toHex(aidSelector.aidToSelect.value))
            try {
                openChannel =
                        session?.openLogicalChannel(aidSelector.aidToSelect.value,
                        aidSelector.fileOccurrence.isoBitMask or aidSelector.fileControlInformation.isoBitMask)
            } catch (e: IOException) {
                Timber.e(e, "IOException")
                throw KeypleIOReaderException("IOException while opening logical channel.")
            } catch (e: NoSuchElementException) {
                Timber.e(e, "NoSuchElementException")
                throw KeypleApplicationSelectionException(
                        "NoSuchElementException: " + ByteArrayUtil.toHex(aidSelector.getAidToSelect().value), e)
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException")
                throw KeypleChannelControlException("SecurityException while opening logical channel, aid :" + ByteArrayUtil.toHex(aidSelector.getAidToSelect().value), e.cause)
            }

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
    override fun openPhysicalChannel() {
        try {
            session = nativeReader.openSession()
        } catch (e: IOException) {
            Timber.e(e, "IOException")
            throw KeypleChannelControlException("IOException while opening physical channel.", e)
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
        Timber.d("Data Length to be sent to tag : " + apduIn.size)
        Timber.d("Data in : " + ByteArrayUtil.toHex(apduIn))
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
