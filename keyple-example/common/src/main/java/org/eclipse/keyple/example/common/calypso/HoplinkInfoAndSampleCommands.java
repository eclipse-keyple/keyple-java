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
import java.util.List;
import java.util.Set;
import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.calypso.command.po.builder.ReadRecordsCmdBuild;
import org.eclipse.keyple.calypso.command.po.builder.UpdateRecordCmdBuild;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.util.ByteBufferUtils;

/**
 * Helper class to provide specific information to handle Hoplink cards.
 * <ul>
 * <li>AID application selection</li>
 * <li>CSM_C1_ATR_REGEX regular expresion matching the expected C1 CSM ATR</li>
 * <li>selectApplicationSuccessfulStatusCodes valid status word for selection command in addition to
 * 9000</li>
 * <li>poReadRecordCmd_T2Env T2 environment file read record command</li>
 * <li>poReadRecordCmd_T2Usage T2 usage file read record command</li>
 * <li>poUpdateRecordCmd_T2UsageFill T2 usage file update record command</li>
 * <li>poRatificationCommand ratifcation command (shorten read record)</li>
 * </ul>
 */
public class HoplinkInfoAndSampleCommands {
    /** Hoplink card AID */
    public final static String AID = "A000000291A000000191";
    /** CSM C1 regular expression: platform, version and serial number values are ignored */
    public final static String CSM_C1_ATR_REGEX =
            "3B3F9600805A[0-9a-fA-F]{2}80C1[0-9a-fA-F]{14}829000";
    /** SW=6283 (application invalidated) is considered as successful */
    public final static Set<Short> selectApplicationSuccessfulStatusCodes =
            new HashSet<Short>(Arrays.asList((short) 0x6283));
    /** Sample data for T2 usage update */
    private final static String t2UsageRecord1_dataFill = "0102030405060708090A0B0C0D0E0F10"
            + "1112131415161718191A1B1C1D1E1F20" + "2122232425262728292A2B2C2D2E2F30";
    /** T2 Environment read record */
    public static ReadRecordsCmdBuild poReadRecordCmd_T2Env =
            new ReadRecordsCmdBuild(PoRevision.REV3_1, (byte) 0x14, (byte) 0x01, true, (byte) 0x20);
    /** T2 Usage read record */
    public static ReadRecordsCmdBuild poReadRecordCmd_T2Usage =
            new ReadRecordsCmdBuild(PoRevision.REV3_1, (byte) 0x1A, (byte) 0x01, true, (byte) 0x30);
    /** T2 Usage update record */
    public static UpdateRecordCmdBuild poUpdateRecordCmd_T2UsageFill =
            new UpdateRecordCmdBuild(PoRevision.REV3_1, (byte) 0x1A, (byte) 0x01,
                    ByteBufferUtils.fromHex(t2UsageRecord1_dataFill));

    /**
     * Build an ApduRequest list including:
     * <ul>
     * <li>T2 Environment file read record</li>
     * <li>T2 Usage file read record</li>
     * <li>T2 Usage file update record</li>
     * </ul>
     * 
     * @return SeRequest
     */
    public static List<ApduRequest> getApduList() {
        List<ApduRequest> poApduRequestList;

        poApduRequestList = Arrays.asList(poReadRecordCmd_T2Env.getApduRequest(),
                poReadRecordCmd_T2Usage.getApduRequest(),
                poUpdateRecordCmd_T2UsageFill.getApduRequest());

        return poApduRequestList;
    }
}
