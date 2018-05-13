/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.csm;

import org.keyple.calypso.commands.csm.builder.*;
import org.keyple.calypso.commands.csm.parser.*;
import org.keyple.commands.AbstractApduCommandBuilder;
import org.keyple.commands.AbstractApduResponseParser;
import org.keyple.commands.CommandsTable;

public enum CalypsoSmCommands implements CommandsTable {

    /** The csm select diversifier. */
    SELECT_DIVERSIFIER("Select Diversifier", (byte) 0x14, SelectDiversifierCmdBuild.class,
            SelectDiversifierRespPars.class),

    /** The csm get challenge. */
    GET_CHALLENGE("Get Challenge", (byte) 0x84, CsmGetChallengeCmdBuild.class,
            CsmGetChallengeRespPars.class),

    /** The csm digest init. */
    DIGEST_INIT("Digest Init", (byte) 0x8A, DigestInitCmdBuild.class, DigestInitRespPars.class),

    /** The csm digest update. */
    DIGEST_UPDATE("Digest Update", (byte) 0x8C, DigestUpdateCmdBuild.class,
            DigestUpdateRespPars.class),

    /** The csm digest update multiple. */
    DIGEST_UPDATE_MULTIPLE("Digest Update Multiple", (byte) 0x8C,
            DigestUpdateMultipleCmdBuild.class, DigestUpdateMultipleRespPars.class),

    /** The csm digest close. */
    DIGEST_CLOSE("Digest Close", (byte) 0x8E, DigestCloseCmdBuild.class, DigestCloseRespPars.class),

    /** The csm digest authenticate. */
    DIGEST_AUTHENTICATE("Digest Authenticate", (byte) 0x82, DigestAuthenticateCmdBuild.class,
            DigestAuthenticateRespPars.class);

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
    CalypsoSmCommands(String name, byte instructionByte,
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
