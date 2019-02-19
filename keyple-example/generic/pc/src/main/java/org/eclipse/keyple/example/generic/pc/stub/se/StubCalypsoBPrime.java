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
public class StubCalypsoBPrime extends StubSecureElement {

    final static String seProtocol = "PROTOCOL_B_PRIME";
    final String ATR_HEX = "3B8F8001805A0A01032003111122334482900082";

    public StubCalypsoBPrime() {
        /* Get data */
        addHexCommand("FFCA 000000", "CA7195009000");
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
