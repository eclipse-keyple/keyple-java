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
package org.eclipse.keyple.calypso.command.po.builder.session;


import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.command.po.PoRevision;

public final class OpenSession31CmdBuild extends AbstractOpenSessionCmdBuild {
    /**
     * Instantiates a new AbstractOpenSessionCmdBuild.
     *
     * @param keyIndex the key index
     * @param samChallenge the sam challenge returned by the SAM Get Challenge APDU command
     * @param sfiToSelect the sfi to select
     * @param recordNumberToRead the record number to read
     * @param extraInfo extra information included in the logs (can be null or empty)
     * @throws java.lang.IllegalArgumentException - if the request is inconsistent
     */
    public OpenSession31CmdBuild(byte keyIndex, byte[] samChallenge, byte sfiToSelect,
            byte recordNumberToRead, String extraInfo) throws IllegalArgumentException {
        super(PoRevision.REV3_1);

        byte p1 = (byte) ((recordNumberToRead * 8) + keyIndex);
        byte p2 = (byte) ((sfiToSelect * 8) + 1);
        /*
         * case 4: this command contains incoming and outgoing data. We define le = 0, the actual
         * length will be processed by the lower layers.
         */
        byte le = 0;

        this.request = setApduRequest(PoClass.ISO.getValue(),
                CalypsoPoCommands.getOpenSessionForRev(PoRevision.REV3_1), p1, p2, samChallenge,
                le);
        if (extraInfo != null) {
            this.addSubName(extraInfo);
        }
    }
}
