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

import org.eclipse.keyple.calypso.command.sam.builder.security.*;
import org.eclipse.keyple.calypso.command.sam.parser.security.*;
import org.eclipse.keyple.core.command.SeCommand;

public enum CalypsoSamCommand implements SeCommand {

    /** The sam select diversifier. */
    SELECT_DIVERSIFIER("Select Diversifier", (byte) 0x14),

    /** The sam get challenge. */
    GET_CHALLENGE("Get Challenge", (byte) 0x84),

    /** The sam digest init. */
    DIGEST_INIT("Digest Init", (byte) 0x8A),

    /** The sam digest update. */
    DIGEST_UPDATE("Digest Update", (byte) 0x8C),

    /** The sam digest update multiple. */
    DIGEST_UPDATE_MULTIPLE("Digest Update Multiple", (byte) 0x8C),

    /** The sam digest close. */
    DIGEST_CLOSE("Digest Close", (byte) 0x8E),

    /** The sam digest authenticate. */
    DIGEST_AUTHENTICATE("Digest Authenticate", (byte) 0x82),

    /** The sam digest authenticate. */
    GIVE_RANDOM("Give Random", (byte) 0x86),

    /** The sam digest authenticate. */
    CARD_GENERATE_KEY("Card Generate Key", (byte) 0x12),

    /** The sam card cipher PIN. */
    CARD_CIPHER_PIN("Card Cipher PIN", (byte) 0x12),

    /** The sam unlock. */
    UNLOCK("Unlock", (byte) 0x20),

    /** The sam write key command. */
    WRITE_KEY("Write Key", (byte) 0x1A),

    READ_KEY_PARAMETERS("Read Key Parameters", (byte) 0xBC),

    READ_EVENT_COUNTER("Read Event Counter", (byte) 0xBE),

    READ_CEILINGS("Read Ceilings", (byte) 0xBE);

    /** The name. */
    private final String name;

    /** The instruction byte. */
    private final byte instructionByte;

    /**
     * The generic constructor of CalypsoCommands.
     *
     * @param name the name
     * @param instructionByte the instruction byte
     */
    CalypsoSamCommand(String name, byte instructionByte) {
        this.name = name;
        this.instructionByte = instructionByte;
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
        return instructionByte;
    }
}
