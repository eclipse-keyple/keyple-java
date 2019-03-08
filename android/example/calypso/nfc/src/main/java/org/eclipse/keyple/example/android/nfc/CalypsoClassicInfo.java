/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.example.android.nfc;


/**
 * Helper class to provide specific elements to handle Calypso cards.
 * <ul>
 * <li>AID application selection (default Calypso AID)</li>
 * <li>SAM_C1_ATR_REGEX regular expression matching the expected C1 SAM ATR</li>
 * <li>Files infos (SFI, rec number, etc) for
 * <ul>
 * <li>Environment and Holder</li>
 * <li>Event Log</li>
 * <li>Contract List</li>
 * <li>Contracts</li>
 * </ul>
 * </li>
 * </ul>
 */
public class CalypsoClassicInfo {
    /** Calypso default AID */
    public final static String AID = "A0000004040125090101";
    /// ** 1TIC.ICA AID */
    // public final static String AID = "315449432E494341";
    /** SAM C1 regular expression: platform, version and serial number values are ignored */
    public final static String SAM_C1_ATR_REGEX =
            "3B3F9600805A[0-9a-fA-F]{2}80C1[0-9a-fA-F]{14}829000";

    public final static String ATR_REV1_REGEX = "3B8F8001805A0A0103200311........829000..";

    public final static byte RECORD_NUMBER_1 = 1;
    public final static byte RECORD_NUMBER_2 = 2;
    public final static byte RECORD_NUMBER_3 = 3;
    public final static byte RECORD_NUMBER_4 = 4;

    public final static byte SFI_EnvironmentAndHolder = (byte) 0x07;
    public final static byte SFI_EventLog = (byte) 0x08;
    public final static byte SFI_ContractList = (byte) 0x1E;
    public final static byte SFI_Contracts = (byte) 0x09;

    public final static String eventLog_dataFill =
            "00112233445566778899AABBCCDDEEFF00112233445566778899AABBCC";

    public CalypsoClassicInfo() {}
}
