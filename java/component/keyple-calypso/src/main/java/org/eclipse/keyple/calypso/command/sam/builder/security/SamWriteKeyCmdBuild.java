/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.command.sam.builder.security;

import org.eclipse.keyple.calypso.command.sam.AbstractSamCommandBuilder;
import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommand;
import org.eclipse.keyple.calypso.command.sam.SamRevision;

/**
 * Builder for the SAM Write Key APDU command.
 */
public class SamWriteKeyCmdBuild extends AbstractSamCommandBuilder {
    /** The command reference. */

    private static final CalypsoSamCommand command = CalypsoSamCommand.WRITE_KEY;

    /**
     * Builder constructor
     * 
     * @param revision the SAM revision
     * @param writingMode the writing mode (P1)
     * @param keyReference the key reference (P2)
     * @param keyData the key data
     */
    public SamWriteKeyCmdBuild(SamRevision revision, byte writingMode, byte keyReference,
            byte[] keyData) {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        byte cla = this.defaultRevision.getClassByte();

        if (keyData == null) {
            throw new IllegalArgumentException("Key data null!");
        }

        if (keyData.length < 48 && keyData.length > 80) {
            throw new IllegalArgumentException("Key data should be between 40 and 80 bytes long!");
        }

        request = setApduRequest(cla, command, writingMode, keyReference, keyData, null);
    }
}
