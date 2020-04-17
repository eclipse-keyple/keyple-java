/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.transaction;

/**
 * The PO Transaction Access Level: personalization, loading or debiting.
 */
public enum SessionAccessLevel {

    /** Session Access Level used for personalization purposes. */
    SESSION_LVL_PERSO("perso", (byte) 0x01),
    /** Session Access Level used for reloading purposes. */
    SESSION_LVL_LOAD("load", (byte) 0x02),
    /** Session Access Level used for validating and debiting purposes. */
    SESSION_LVL_DEBIT("debit", (byte) 0x03);

    private final String name;
    private final byte sessionKey;

    SessionAccessLevel(String name, byte sessionKey) {
        this.name = name;
        this.sessionKey = sessionKey;
    }

    public String getName() {
        return name;
    }

    public byte getSessionKey() {
        return sessionKey;
    }
}
