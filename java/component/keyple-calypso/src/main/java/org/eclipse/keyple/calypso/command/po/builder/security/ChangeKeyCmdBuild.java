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
package org.eclipse.keyple.calypso.command.po.builder.security;

import org.eclipse.keyple.calypso.command.PoClass;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommands;
import org.eclipse.keyple.calypso.command.po.parser.security.ChangeKeyRespPars;
import org.eclipse.keyple.seproxy.message.ApduResponse;

public class ChangeKeyCmdBuild extends AbstractPoCommandBuilder<ChangeKeyRespPars> {
    private static final CalypsoPoCommands command = CalypsoPoCommands.CHANGE_KEY;

    /**
     * Change Key Calypso command
     * 
     * @param poClass indicates which CLA byte should be used for the Apdu
     * @param keyIndex index of the key of the current DF to change
     * @param cryptogram key encrypted with Issuer key (key #1)
     */
    public ChangeKeyCmdBuild(PoClass poClass, byte keyIndex, byte[] cryptogram) {
        super(command, null);

        if (cryptogram == null || (cryptogram.length != 0x18 && cryptogram.length != 0x20)) {
            throw new IllegalArgumentException("Bad cryptogram value.");
        }

        byte cla = poClass.getValue();
        byte p1 = (byte) 0x00;
        byte p2 = keyIndex;

        this.request = setApduRequest(cla, command, p1, p2, cryptogram, null);
        this.addSubName("Change Key");
    }

    @Override
    public ChangeKeyRespPars createResponseParser(ApduResponse apduResponse) {
        return new ChangeKeyRespPars(apduResponse);
    }
}
