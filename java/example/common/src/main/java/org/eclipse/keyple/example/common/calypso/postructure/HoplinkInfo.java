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
package org.eclipse.keyple.example.calypso.common.postructure;



/**
 * Helper class to provide specific information to handle Hoplink cards.
 * <ul>
 * <li>AID application selection</li>
 * <li>SAM_C1_ATR_REGEX regular expresion matching the expected C1 SAM ATR</li>
 * <li>Files infos (SFI, rec number, etc) for T2 Environment and T2 Usage</li>
 * </ul>
 */
public class HoplinkInfo {
    /** Hoplink card AID */
    public final static String AID = "A000000291A000000191";
    /** SAM C1 regular expression: platform, version and serial number values are ignored */
    public final static String SAM_C1_ATR_REGEX =
            "3B3F9600805A[0-9a-fA-F]{2}80C1[0-9a-fA-F]{14}829000";
    /** Sample data for T2 usage update */
    public final static String t2UsageRecord1_dataFill = "0102030405060708090A0B0C0D0E0F10"
            + "1112131415161718191A1B1C1D1E1F20" + "2122232425262728292A2B2C2D2E2F30";

    public final static byte RECORD_NUMBER_1 = 1;
    public final static byte RECORD_NUMBER_2 = 2;
    public final static byte RECORD_NUMBER_3 = 3;
    public final static byte RECORD_NUMBER_4 = 4;

    public final static byte SFI_T2Usage = (byte) 0x1A;
    public final static byte SFI_T2Environment = (byte) 0x14;

    public final static String EXTRAINFO_ReadRecord_T2UsageRec1 =
            String.format("T2 Usage (SFI=%02X, recnbr=%d))", SFI_T2Usage, RECORD_NUMBER_1);
    public final static String EXTRAINFO_ReadRecord_T2EnvironmentRec1 = String
            .format("T2 Environment (SFI=%02X, recnbr=%d))", SFI_T2Environment, RECORD_NUMBER_1);
}
