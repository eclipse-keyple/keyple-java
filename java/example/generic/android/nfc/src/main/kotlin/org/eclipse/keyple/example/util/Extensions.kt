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
package org.eclipse.keyple.example.util

import android.content.Context
import android.os.Build
import org.eclipse.keyple.plugin.android.nfc.AndroidNfcReader

/**
 * Extensions to improve readability of example code
 */
fun AndroidNfcReader.configFlags(presenceCheckDelay: Int? = null, noPlateformSound: Int? = null, skipNdefCheck: Int? = null) {
    presenceCheckDelay?.let { this.setParameter("FLAG_READER_PRESENCE_CHECK_DELAY", "$presenceCheckDelay") }
    noPlateformSound?.let { this.setParameter("FLAG_READER_NO_PLATFORM_SOUNDS", "$noPlateformSound") }
    skipNdefCheck?.let { this.setParameter("FLAG_READER_SKIP_NDEF_CHECK", "$skipNdefCheck") }
}

fun Context.getColorResource(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        resources.getColor(id, null)
    } else {
        resources.getColor(id)
    }
}
