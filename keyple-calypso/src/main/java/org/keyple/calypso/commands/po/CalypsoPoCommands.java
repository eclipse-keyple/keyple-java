package org.keyple.calypso.commands.po;

import org.keyple.calypso.commands.CommandType;
import org.keyple.calypso.commands.po.builder.*;
import org.keyple.calypso.commands.po.parser.*;
import org.keyple.commands.AbstractApduCommandBuilder;
import org.keyple.commands.AbstractApduResponseParser;
import org.keyple.commands.CommandsTable;

public enum CalypsoPoCommands implements CommandsTable {

        /** The po get data. */
        GET_DATA_FCI("Get Data'FCI'", (byte) 0xCA, GetDataFciCmdBuild.class,
                GetDataFciRespPars.class),

        /** The po open session. */
        OPEN_SESSION_24("Open Secure Session V2.4", (byte) 0x8A,
                OpenSession24CmdBuild.class, OpenSession24RespPars.class),

        /** The po open session. */
        OPEN_SESSION_31("Open Secure Session V3.1", (byte) 0x8A,
                OpenSession31CmdBuild.class, OpenSession31RespPars.class),

        /** The po open session. */
        OPEN_SESSION_32("Open Secure Session V3.2", (byte) 0x8A,
                OpenSession32CmdBuild.class, OpenSession32RespPars.class),

        /** The po close session. */
        CLOSE_SESSION("Close Secure Session", (byte) 0x8E,
                CloseSessionCmdBuild.class, CloseSessionRespPars.class),

        /** The po read records. */
        READ_RECORDS("Read Records", (byte) 0xB2, ReadRecordsCmdBuild.class,
                ReadRecordsRespPars.class),

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

}
