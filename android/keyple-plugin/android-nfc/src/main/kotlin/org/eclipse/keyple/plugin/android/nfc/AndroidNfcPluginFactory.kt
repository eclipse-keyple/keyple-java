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
import org.eclipse.keyple.core.service.Plugin
import org.eclipse.keyple.core.service.PluginFactory
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler

/**
 * Provides a factory to get the {@link AndroidNfcPlugin}.
 *
 * @property activity Provides the activity context to be used with android.nfc.NfcAdapter. Cannot be null.
 * @property readerObservationExceptionHandler In case of a fatal error during the observation, the handler will receive. Cannot be null.
 * a notification.
 *
 * @since 0.9
 */
class AndroidNfcPluginFactory(private val activity: Activity, private val readerObservationExceptionHandler: ReaderObservationExceptionHandler) : PluginFactory {

    /**
     * {@inheritDoc}
     *
     * @since 0.9
     */
    override fun getPluginName(): String {
        return AndroidNfcPlugin.PLUGIN_NAME
    }

    /**
     * Returns an instance of the {@link AndroidNfcPlugin} if the platform is ready
     *
     * @return A not null {@link AndroidNfcPlugin} instance.
     * @throws KeyplePluginInstantiationException if smartcard.io library is not ready
     * @since 0.9
     */
    override fun getPlugin(): Plugin {
        return AndroidNfcPluginImpl(activity, readerObservationExceptionHandler)
    }
}
