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
package org.eclipse.keyple.calypso.command.sam;

import org.eclipse.keyple.calypso.command.sam.builder.session.*;
import org.eclipse.keyple.calypso.command.sam.builder.session.SelectDiversifierCmdBuild;
import org.eclipse.keyple.calypso.command.sam.parser.session.*;
import org.eclipse.keyple.calypso.command.sam.parser.session.SelectDiversifierRespPars;
import org.eclipse.keyple.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.command.CommandsTable;

public enum CalypsoSamCommands implements CommandsTable {

    /** The sam select diversifier. */
    SELECT_DIVERSIFIER("Select Diversifier", (byte) 0x14, SelectDiversifierCmdBuild.class,
            SelectDiversifierRespPars.class),

    /** The sam get challenge. */
    GET_CHALLENGE("Get Challenge", (byte) 0x84, SamGetChallengeCmdBuild.class,
            SamGetChallengeRespPars.class),

    /** The sam digest init. */
    DIGEST_INIT("Digest Init", (byte) 0x8A, DigestInitCmdBuild.class, DigestInitRespPars.class),

    /** The sam digest update. */
    DIGEST_UPDATE("Digest Update", (byte) 0x8C, DigestUpdateCmdBuild.class,
            DigestUpdateRespPars.class),

    /** The sam digest update multiple. */
    DIGEST_UPDATE_MULTIPLE("Digest Update Multiple", (byte) 0x8C,
            DigestUpdateMultipleCmdBuild.class, DigestUpdateMultipleRespPars.class),

    /** The sam digest close. */
    DIGEST_CLOSE("Digest Close", (byte) 0x8E, DigestCloseCmdBuild.class, DigestCloseRespPars.class),

    /** The sam digest authenticate. */
    DIGEST_AUTHENTICATE("Digest Authenticate", (byte) 0x82, DigestAuthenticateCmdBuild.class,
            DigestAuthenticateRespPars.class);

    /** The name. */
    private final String name;

    /** The instruction byte. */
    private final byte instructionbyte;

    /** The command builder class. */
    private final Class<? extends AbstractApduCommandBuilder> commandBuilderClass;

    /** The response parser class. */
    private final Class<? extends AbstractApduResponseParser> responseParserClass;

    /**
     * The generic constructor of CalypsoCommands.
     *
     * @param name the name
     * @param instructionByte the instruction byte
     * @param commandBuilderClass the command builder class
     * @param responseParserClass the response parser class
     */
    CalypsoSamCommands(String name, byte instructionByte,
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
