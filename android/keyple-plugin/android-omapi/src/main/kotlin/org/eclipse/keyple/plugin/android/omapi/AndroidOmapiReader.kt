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
package org.eclipse.keyple.plugin.android.omapi

import java.util.HashMap
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractLocalReader
import org.eclipse.keyple.core.seproxy.plugin.local.SmartSelectionReader
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode
import timber.log.Timber

/**
 * Communicates with Android readers throught the Open Mobile API see org.simalliance.openmobileapi.Reader
 *
 * Instances of this class represent SE readers supported by this device. These readers can be physical devices
 * or virtual devices. They can be removable or not. They can contain one SE that can or cannot be
 * removed.
 */
internal abstract class AndroidOmapiReader(pluginName: String, readerName: String) : AbstractLocalReader(pluginName, readerName), SmartSelectionReader {

    private val parameters: MutableMap<String, String> = HashMap()

    override fun getParameters(): Map<String, String> {
        Timber.w("No parameters are supported by AndroidOmapiReaderImpl")
        return parameters
    }

    override fun setParameter(key: String, value: String) {
        Timber.w("No parameters are supported by AndroidOmapiReaderImpl")
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
     * Check that protocolFlag is PROTOCOL_ISO7816_3
     * @param protocolFlag
     * @return true if match PROTOCOL_ISO7816_3
     */
    override fun protocolFlagMatches(protocolFlag: SeProtocol?): Boolean {
        return protocolFlag == SeCommonProtocols.PROTOCOL_ISO7816_3
    }
}
