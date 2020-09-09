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

object CalypsoClassicInfo {
    /** AID: Keyple test kit profile 1, Application 2  */
    val AID = "315449432E49434131"
    val AID_PREFIX = "315449432E494341"
    // / ** 1TIC.ICA AID */
    // public final static String AID = "315449432E494341";
    /** SAM C1 regular expression: platform, version and serial number values are ignored  */
    val SAM_C1_ATR_REGEX = "3B3F9600805A[0-9a-fA-F]{2}80C1[0-9a-fA-F]{14}829000"

    val ATR_REV1_REGEX = "3B8F8001805A0A0103200311........829000.."

    val RECORD_NUMBER_1: Byte = 1
    val RECORD_NUMBER_2: Byte = 2
    val RECORD_NUMBER_3: Byte = 3
    val RECORD_NUMBER_4: Byte = 4

    val SFI_EnvironmentAndHolder = 0x07.toByte()
    val SFI_EventLog = 0x08.toByte()
    val SFI_ContractList = 0x1E.toByte()
    val SFI_Contracts = 0x09.toByte()

    val eventLog_dataFill = "00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC"
}
