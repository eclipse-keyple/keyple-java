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

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import org.eclipse.keyple.core.plugin.reader.WaitForCardRemovalBlocking
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler
import timber.log.Timber

/**
 * Singleton used by the plugin to run native NFC reader on Android version >= 24 (Android N)
 *
 * It will used native features of Android NFC API to detect card removal.
 */
internal class AndroidNfcReaderPostNImpl(activity: Activity, readerObservationExceptionHandler: ReaderObservationExceptionHandler) : AbstractAndroidNfcReader(activity, readerObservationExceptionHandler), WaitForCardRemovalBlocking {

    private var isWatingForRemoval = false
    private val syncWaitRemoval = Object()

    override fun stopWaitForCardRemoval() {
        Timber.d("stopWaitForCardRemoval")
        isWatingForRemoval = false
        synchronized(syncWaitRemoval) {
            syncWaitRemoval.notify()
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun waitForCardAbsentNative(): Boolean {
        Timber.d("waitForCardAbsentNative")
        var isRemoved = false
        if (!isWatingForRemoval) {
            isWatingForRemoval = true
            nfcAdapter?.ignore(getTagProxyTag(), 1000, {
                isRemoved = true
                synchronized(syncWaitRemoval) {
                    syncWaitRemoval.notify()
                }
            }, null)

            synchronized(syncWaitRemoval) {
                syncWaitRemoval.wait(10000)
            }
        }
        return isRemoved
    }
}
