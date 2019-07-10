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
import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommands;
import org.eclipse.keyple.calypso.command.sam.SamRevision;

/**
 * Builder for the SAM Unlock APDU command.
 */
public class UnlockCmdBuild extends AbstractSamCommandBuilder {
    /** The command reference. */

    private static final CalypsoSamCommands command = CalypsoSamCommands.UNLOCK;

    /**
     *
     * @param revision
     * @param unlockData
     */
    public UnlockCmdBuild(SamRevision revision, byte[] unlockData) {
        super(command, null);
        if (revision != null) {
            this.defaultRevision = revision;
        }
        byte cla = this.defaultRevision.getClassByte();
        byte p1 = (byte) 0x00;
        byte p2 = (byte) 0x00;

        if (unlockData == null) {
            throw new IllegalArgumentException("Unlock data null!");
        }

        if (unlockData.length != 8 && unlockData.length != 16) {
            throw new IllegalArgumentException("Unlock data should be 8 ou 16 bytes long!");
        }

        request = setApduRequest(cla, command, p1, p2, unlockData, null);
    }
}
