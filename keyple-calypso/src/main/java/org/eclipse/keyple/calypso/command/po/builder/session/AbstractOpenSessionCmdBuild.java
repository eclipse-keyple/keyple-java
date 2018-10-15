/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.calypso.command.po.builder.session;



import org.eclipse.keyple.calypso.command.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.command.po.PoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.PoRevision;

/**
 * The Class AbstractOpenSessionCmdBuild. This class provides the dedicated constructor to build the
 * Open Secure Session APDU command.
 *
 */
public abstract class AbstractOpenSessionCmdBuild extends PoCommandBuilder {

    /**
     * Instantiates a new AbstractOpenSessionCmdBuild.
     *
     * @param revision the revision of the PO
     * @throws java.lang.IllegalArgumentException - if the key index is 0 and rev is 2.4
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public AbstractOpenSessionCmdBuild(PoRevision revision) {
        super(CalypsoPoCommands.getOpenSessionForRev(revision), null);
        defaultRevision = revision;
    }

    public static AbstractOpenSessionCmdBuild create(PoRevision revision, byte debitKeyIndex,
            byte[] sessionTerminalChallenge, byte sfi, byte recordNb, String extraInfo) {
        switch (revision) {
            case REV1_0:
                return new OpenSession10CmdBuild(debitKeyIndex, sessionTerminalChallenge, sfi,
                        recordNb, extraInfo);
            case REV2_4:
                return new OpenSession24CmdBuild(debitKeyIndex, sessionTerminalChallenge, sfi,
                        recordNb, extraInfo);
            case REV3_1:
                return new OpenSession31CmdBuild(debitKeyIndex, sessionTerminalChallenge, sfi,
                        recordNb, extraInfo);
            case REV3_2:
                return new OpenSession32CmdBuild(debitKeyIndex, sessionTerminalChallenge, sfi,
                        recordNb, extraInfo);
            default:
                throw new IllegalArgumentException("Revision " + revision + " isn't supported");
        }
    }
}
