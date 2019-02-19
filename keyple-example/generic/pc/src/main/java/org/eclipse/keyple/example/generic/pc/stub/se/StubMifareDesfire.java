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
package org.eclipse.keyple.example.generic.pc.stub.se;


import org.eclipse.keyple.plugin.stub.StubSecureElement;
import org.eclipse.keyple.util.ByteArrayUtils;

/**
 * Simple contact stub SE (no command)
 */
public class StubMifareDesfire extends StubSecureElement {

    final static String seProtocol = "PROTOCOL_MIFARE_DESFIRE";
    final String ATR_HEX = "3B8180018080";

    public StubMifareDesfire() {
        /* Get data */
        addHexCommand("FFCA 000000", "223344556677889000");
    }

    @Override
    public byte[] getATR() {
        return ByteArrayUtils.fromHex(ATR_HEX);
    }

    @Override
    public String getSeProcotol() {
        return seProtocol;
    }


}
