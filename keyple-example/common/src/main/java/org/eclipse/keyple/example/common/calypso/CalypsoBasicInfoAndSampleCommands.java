/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.example.common.calypso;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.AppendRecordCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.util.ByteBufferUtils;

/**
 * Helper class to provide specific elements to handle Calypso cards.
 * <ul>
 * <li>AID application selection ("1TIC.ICA")</li>
 * <li>CSM_C1_ATR_REGEX regular expression matching the expected C1 CSM ATR</li>
 * <li>selectApplicationSuccessfulStatusCodes valid status word for selection command in addition to
 * 9000</li>
 * <li>poReadRecordCmd_EventLog Command to read the Event Log file</li>
 * <li>poReadRecordCmd_ContractList Command to read the Contract List file</li>
 * <li>poReadRecordCmd_Contract Command to reader the Contract file</li>
 * <li>eventLog_dataFill Data to write in the Event Log file</li>
 * <li>poAppendRecordCmd_EventLog Command to add a record to the Event Log file</li>
 * <li>poRatificationCommand ratification command (shorten read record)</li>
 * </ul>
 */
public class CalypsoBasicInfoAndSampleCommands {
    /** 1TIC.ICA (Calypso default AID) */
    public final static String AID = "A0000004040125090101"; // "315449432E494341";
    /** CSM C1 regular expression: platform, version and serial number values are ignored */
    public final static String CSM_C1_ATR_REGEX =
            "3B3F9600805A[0-9a-fA-F]{2}80C1[0-9a-fA-F]{14}829000";
    /** SW=6283 (application invalidated) is considered as successful */
    public final static Set<Short> selectApplicationSuccessfulStatusCodes =
            new HashSet<Short>(Arrays.asList((short) 0x6283));

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

    /** Event Log read record */
    public static ReadRecordsCmdBuild poReadRecordCmd_EventLog = new ReadRecordsCmdBuild(
            PoRevision.REV3_1, SFI_EventLog, RECORD_NUMBER_1, true, (byte) 0x00);

    /** Contract List read record */
    public static ReadRecordsCmdBuild poReadRecordCmd_ContractList = new ReadRecordsCmdBuild(
            PoRevision.REV3_1, SFI_ContractList, RECORD_NUMBER_1, true, (byte) 0x00);

    /** Contract #1 read record */
    public static ReadRecordsCmdBuild poReadRecordCmd_Contract = new ReadRecordsCmdBuild(
            PoRevision.REV3_1, SFI_Contracts, RECORD_NUMBER_1, true, (byte) 0x00);

    /** Event Log append record */
    public static AppendRecordCmdBuild poAppendRecordCmd_EventLog = new AppendRecordCmdBuild(
            PoRevision.REV3_1, SFI_EventLog, ByteBufferUtils.fromHex(eventLog_dataFill));

}
