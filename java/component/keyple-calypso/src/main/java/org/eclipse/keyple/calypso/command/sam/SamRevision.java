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

/**
 * This enumeration registers all supported revisions of SAM.
 *
 */
public enum SamRevision {

    /** The revision of C1 and S1E SAM. CLA 0x00 or 0x80 */
    C1("C1", "C1", (byte) 0x80),

    /** The revision of S1E SAM. CLA 0x00 or 0x80 */
    S1E("S1E", "E1", (byte) 0x80),

    /** The revision of S1D SAM SAM. CLA 0x94 */
    S1D("S1D", "D?", (byte) 0x94),

    /**
     * Joker value matching any application type.
     * <p>
     * Used as an argument in SamSelector.
     * <p>
     * The actual revision will be retrieved from the ATR historical bytes.
     */
    AUTO("AUTO", "??", (byte) 0x00);

    private final String name;
    private final String applicationTypeMask;
    private final byte classByte;

    SamRevision(String name, String applicationTypeMask, byte classByte) {
        this.name = name;
        this.applicationTypeMask = applicationTypeMask;
        this.classByte = classByte;
    }

    public String getName() {
        return name;
    }

    public String getApplicationTypeMask() {
        return applicationTypeMask;
    }

    public byte getClassByte() {
        return classByte;
    }
}
