/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands;

import org.keyple.calypso.commands.csm.builder.CsmGetChallengeCmdBuild;
import org.keyple.calypso.commands.csm.builder.DigestAuthenticateCmdBuild;
import org.keyple.calypso.commands.csm.builder.DigestCloseCmdBuild;
import org.keyple.calypso.commands.csm.builder.DigestInitCmdBuild;
import org.keyple.calypso.commands.csm.builder.DigestUpdateCmdBuild;
import org.keyple.calypso.commands.csm.builder.DigestUpdateMultipleCmdBuild;
import org.keyple.calypso.commands.csm.builder.SelectDiversifierCmdBuild;
import org.keyple.calypso.commands.csm.parser.CsmGetChallengeRespPars;
import org.keyple.calypso.commands.csm.parser.DigestAuthenticateRespPars;
import org.keyple.calypso.commands.csm.parser.DigestCloseRespPars;
import org.keyple.calypso.commands.csm.parser.DigestInitRespPars;
import org.keyple.calypso.commands.csm.parser.DigestUpdateMultipleRespPars;
import org.keyple.calypso.commands.csm.parser.DigestUpdateRespPars;
import org.keyple.calypso.commands.csm.parser.SelectDiversifierRespPars;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.*;
import org.keyple.calypso.commands.po.parser.*;
import org.keyple.commands.AbstractApduCommandBuilder;
import org.keyple.commands.AbstractApduResponseParser;
import org.keyple.commands.CommandsTable;

/**
 * PO and CSM commands enumeration. Each enumeration provides the type (PO/CSM), the name, the
 * command builder and the response parser.
 */
public enum CalypsoCommands implements CommandsTable {

    /** The po get data. */
    PO_GET_DATA_FCI(CommandType.PO, "Get Data'FCI'", (byte) 0xCA, GetDataFciCmdBuild.class,
            GetDataFciRespPars.class),

    /** The po open session. */
    PO_OPEN_SESSION_24(CommandType.PO, "Open Secure Session V2.4", (byte) 0x8A,
            OpenSession24CmdBuild.class, OpenSession24RespPars.class),

    /** The po open session. */
    PO_OPEN_SESSION_31(CommandType.PO, "Open Secure Session V3.1", (byte) 0x8A,
            OpenSession31CmdBuild.class, OpenSession31RespPars.class),

    /** The po open session. */
    PO_OPEN_SESSION_32(CommandType.PO, "Open Secure Session V3.2", (byte) 0x8A,
            OpenSession32CmdBuild.class, OpenSession32RespPars.class),

    /** The po close session. */
    PO_CLOSE_SESSION(CommandType.PO, "Close Secure Session", (byte) 0x8E,
            CloseSessionCmdBuild.class, CloseSessionRespPars.class),

    /** The po read records. */
    PO_READ_RECORDS(CommandType.PO, "Read Records", (byte) 0xB2, ReadRecordsCmdBuild.class,
            ReadRecordsRespPars.class),

    /** The po update record. */
    PO_UPDATE_RECORD(CommandType.PO, "Update Record", (byte) 0xDC, UpdateRecordCmdBuild.class,
            UpdateRecordRespPars.class),

    /** The po append record. */
    PO_APPEND_RECORD(CommandType.PO, "Append Record", (byte) 0xE2, AppendRecordCmdBuild.class,
            AppendRecordRespPars.class),

    /** The po get challenge. */
    PO_GET_CHALLENGE(CommandType.PO, "Get Challenge", (byte) 0x84, PoGetChallengeCmdBuild.class,
            PoGetChallengeRespPars.class),

    /** The csm select diversifier. */
    CSM_SELECT_DIVERSIFIER(CommandType.CSM, "Select Diversifier", (byte) 0x14,
            SelectDiversifierCmdBuild.class, SelectDiversifierRespPars.class),

    /** The csm get challenge. */
    CSM_GET_CHALLENGE(CommandType.CSM, "Get Challenge", (byte) 0x84, CsmGetChallengeCmdBuild.class,
            CsmGetChallengeRespPars.class),

    /** The csm digest init. */
    CSM_DIGEST_INIT(CommandType.CSM, "Digest Init", (byte) 0x8A, DigestInitCmdBuild.class,
            DigestInitRespPars.class),

    /** The csm digest update. */
    CSM_DIGEST_UPDATE(CommandType.CSM, "Digest Update", (byte) 0x8C, DigestUpdateCmdBuild.class,
            DigestUpdateRespPars.class),

    /** The csm digest update multiple. */
    CSM_DIGEST_UPDATE_MULTIPLE(CommandType.CSM, "Digest Update Multiple", (byte) 0x8C,
            DigestUpdateMultipleCmdBuild.class, DigestUpdateMultipleRespPars.class),

    /** The csm digest close. */
    CSM_DIGEST_CLOSE(CommandType.CSM, "Digest Close", (byte) 0x8E, DigestCloseCmdBuild.class,
            DigestCloseRespPars.class),

    /** The csm digest authenticate. */
    CSM_DIGEST_AUTHENTICATE(CommandType.CSM, "Digest Authenticate", (byte) 0x82,
            DigestAuthenticateCmdBuild.class, DigestAuthenticateRespPars.class);

    /** The command type. */
    private CommandType commandType;

    /** The name. */
    private String name;

    /** The instruction byte. */
    private byte instructionbyte;

    /** The command builder class. */
    private Class<? extends AbstractApduCommandBuilder> commandBuilderClass;

    /** The response parser class. */
    private Class<? extends AbstractApduResponseParser> responseParserClass;

    /**
     * The generic constructor of CalypsoCommands.
     *
     * @param commandType the command type
     * @param name the name
     * @param instructionByte the instruction byte
     * @param commandBuilderClass the command builder class
     * @param responseParserClass the response parser class
     */
    CalypsoCommands(CommandType commandType, String name, byte instructionByte,
            Class<? extends AbstractApduCommandBuilder> commandBuilderClass,
            Class<? extends AbstractApduResponseParser> responseParserClass) {
        this.commandType = commandType;
        this.name = name;
        this.instructionbyte = instructionByte;
        this.commandBuilderClass = commandBuilderClass;
        this.responseParserClass = responseParserClass;
    }

    /**
     * Gets the type.
     *
     * @return the command type
     */
    public CommandType getType() {
        return commandType;
    }

    /**
     * Gets the name.
     *
     * @return the command name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the instruction byte.
     *
     * @return the value of INS byte
     */
    public byte getInstructionByte() {
        return instructionbyte;
    }

    /**
     * Gets the command builder class.
     *
     * @return the corresponding command builder class
     */
    public Class<? extends AbstractApduCommandBuilder> getCommandBuilderClass() {
        return commandBuilderClass;
    }

    /**
     * Gets the response parser class.
     *
     * @return the corresponding response parser class
     */
    public Class<? extends AbstractApduResponseParser> getResponseParserClass() {
        return responseParserClass;
    }

    /**
     * Get the right open-session command for a given {@link PoRevision}
     * 
     * @param rev Command revision
     * @return Returned command
     */
    public static CalypsoCommands getOpenSessionForRev(PoRevision rev) {
        switch (rev) {
            case REV2_4:
                return CalypsoCommands.PO_OPEN_SESSION_24;
            case REV3_1:
                return CalypsoCommands.PO_OPEN_SESSION_31;
            case REV3_2:
                return CalypsoCommands.PO_OPEN_SESSION_32;
            default:
                throw new IllegalStateException("Any revision should have a matching command");
        }
    }
}
