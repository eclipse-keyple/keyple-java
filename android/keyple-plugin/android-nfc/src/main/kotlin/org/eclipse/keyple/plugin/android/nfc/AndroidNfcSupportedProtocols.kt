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

/**
 * List of protocols supported by this plugin
 *
 *  @since 0.9
 */
enum class AndroidNfcSupportedProtocols {
    ISO_14443_4,
//    NFC_A_ISO_14443_3A,
//    NFC_B_ISO_14443_3B,
//    NFC_F_JIS_6319_4,
//    NFC_V_ISO_15693,
//    NFC_NDEF_TAG,
//    NFC_NDEF_FORMATABLE,
//    NFC_BARCODE,
    MIFARE_ULTRA_LIGHT,
    MIFARE_CLASSIC,
}
