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
package org.eclipse.keyple.calypso.command;

/* Class for the Calypso command: LEGACY for REV1 / BPRIME type PO, ISO for REV2/3 / B type */
public enum PoClass {

    LEGACY((byte) 0x94), ISO((byte) 0x00);

    private final byte cla;


    public byte getValue() {
        return cla;
    }

    PoClass(byte cla) {
        this.cla = cla;
    }
}
