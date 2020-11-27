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
import org.eclipse.keyple.core.plugin.WaitForCardRemovalNonBlocking
import org.eclipse.keyple.core.service.event.ReaderObservationExceptionHandler

/**
 * Singleton used by the plugin to run native NFC reader on Android version < 24 (Android N).
 *
 * It uses a Ping monitoring job to detect card removal
 */
internal class AndroidNfcReaderPreNImpl(activity: Activity, readerObservationExceptionHandler: ReaderObservationExceptionHandler) : AbstractAndroidNfcReader(activity, readerObservationExceptionHandler), WaitForCardRemovalNonBlocking
