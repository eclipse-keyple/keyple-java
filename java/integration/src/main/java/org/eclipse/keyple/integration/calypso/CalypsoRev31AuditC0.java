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
package org.eclipse.keyple.integration.calypso;

import static org.eclipse.keyple.calypso.transaction.ApplicationType.*;
import static org.eclipse.keyple.calypso.transaction.KeyDescriptor.Algorithm.TDES;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionAccessLevel.*;
import org.eclipse.keyple.calypso.transaction.AbstractPoApplicationSettings;
import org.eclipse.keyple.calypso.transaction.KeyDescriptor;

public class CalypsoRev31AuditC0 extends AbstractPoApplicationSettings {
    public CalypsoRev31AuditC0() {

        setAid("315449432E49434131");

        setPoSerialNumberFilter(null);

        // MF
        setKeyDescriptor(MASTER_FILE, SESSION_LVL_PERSO,
                new KeyDescriptor("MF1", (byte) 0x46, (byte) 0x61, (byte) 0x79, TDES));

        setKeyDescriptor(MASTER_FILE, SESSION_LVL_LOAD,
                new KeyDescriptor("MF2", (byte) 0x47, (byte) 0x67, (byte) 0x79, TDES));

        setKeyDescriptor(MASTER_FILE, SESSION_LVL_DEBIT,
                new KeyDescriptor("MF3", (byte) 0x48, (byte) 0x70, (byte) 0x79, TDES));

        // RT
        setKeyDescriptor(GENERAL_USE, SESSION_LVL_PERSO,
                new KeyDescriptor("RT1", (byte) 0x4C, (byte) 0x21, (byte) 0x78, TDES));

        setKeyDescriptor(GENERAL_USE, SESSION_LVL_LOAD,
                new KeyDescriptor("RT2", (byte) 0x42, (byte) 0x27, (byte) 0x78, TDES));

        setKeyDescriptor(GENERAL_USE, SESSION_LVL_DEBIT,
                new KeyDescriptor("RT3", (byte) 0x4D, (byte) 0x30, (byte) 0x78, TDES));

        // SV
        setKeyDescriptor(STORES_VALUE, SESSION_LVL_PERSO,
                new KeyDescriptor("SV1", (byte) 0x49, (byte) 0x01, (byte) 0x79, TDES));

        setKeyDescriptor(STORES_VALUE, SESSION_LVL_LOAD,
                new KeyDescriptor("SV2", (byte) 0x4A, (byte) 0x07, (byte) 0x79, TDES));

        setKeyDescriptor(STORES_VALUE, SESSION_LVL_DEBIT,
                new KeyDescriptor("SV3", (byte) 0x4B, (byte) 0x10, (byte) 0x79, TDES));
    }
}
