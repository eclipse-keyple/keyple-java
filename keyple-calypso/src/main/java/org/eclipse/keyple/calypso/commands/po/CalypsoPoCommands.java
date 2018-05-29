/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.commands.po;

import org.eclipse.keyple.calypso.commands.po.builder.*;
import org.eclipse.keyple.calypso.commands.po.parser.*;
import org.eclipse.keyple.commands.AbstractApduCommandBuilder;
import org.eclipse.keyple.commands.AbstractApduResponseParser;
import org.eclipse.keyple.commands.CommandsTable;

public enum CalypsoPoCommands implements CommandsTable {

    /** The po get data. */
    GET_DATA_FCI("Get Data'FCI'", (byte) 0xCA, GetDataFciCmdBuild.class, GetDataFciRespPars.class),

    /** The po open session. */
    OPEN_SESSION_24("Open Secure Session V2.4", (byte) 0x8A, OpenSession24CmdBuild.class,
            OpenSession24RespPars.class),

    /** The po open session. */
    OPEN_SESSION_31("Open Secure Session V3.1", (byte) 0x8A, OpenSession31CmdBuild.class,
            OpenSession31RespPars.class),

    /** The po open session. */
    OPEN_SESSION_32("Open Secure Session V3.2", (byte) 0x8A, OpenSession32CmdBuild.class,
            OpenSession32RespPars.class),

    /** The po close session. */
    CLOSE_SESSION("Close Secure Session", (byte) 0x8E, CloseSessionCmdBuild.class,
            CloseSessionRespPars.class),

    /** The po read records. */
    READ_RECORDS("Read Records", (byte) 0xB2, ReadRecordsCmdBuild.class, ReadRecordsRespPars.class),

    /** The po update record. */
    UPDATE_RECORD("Update Record", (byte) 0xDC, UpdateRecordCmdBuild.class,
            UpdateRecordRespPars.class),

    /** The po append record. */
    APPEND_RECORD("Append Record", (byte) 0xE2, AppendRecordCmdBuild.class,
            AppendRecordRespPars.class),

    /** The po get challenge. */
    GET_CHALLENGE("Get Challenge", (byte) 0x84, PoGetChallengeCmdBuild.class,
            PoGetChallengeRespPars.class);


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
     * @param name the name
     * @param instructionByte the instruction byte
     * @param commandBuilderClass the command builder class
     * @param responseParserClass the response parser class
     */
    CalypsoPoCommands(String name, byte instructionByte,
            Class<? extends AbstractApduCommandBuilder> commandBuilderClass,
            Class<? extends AbstractApduResponseParser> responseParserClass) {
        this.name = name;
        this.instructionbyte = instructionByte;
        this.commandBuilderClass = commandBuilderClass;
        this.responseParserClass = responseParserClass;
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
    public static CalypsoPoCommands getOpenSessionForRev(PoRevision rev) {
        switch (rev) {
            case REV2_4:
                return OPEN_SESSION_24;
            case REV3_1:
                return OPEN_SESSION_31;
            case REV3_2:
                return OPEN_SESSION_32;
            default:
                throw new IllegalStateException("Any revision should have a matching command");
        }
    }
}
