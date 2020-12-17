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

import org.eclipse.keyple.core.service.Plugin

/**
 * Provides the public elements used to manage the Android NFC plugin.<br>
 * It defines in particular the type of object produced by the {@link AndroidNfcPluginFactory} and allow
 * to set parameters to retrieve the communication mode from the name of a reader.
 */
interface AndroidNfcPlugin : Plugin {
    companion object {
        const val PLUGIN_NAME = "AndroidNfcPlugin"
    }
}
