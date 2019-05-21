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
package org.eclipse.keyple.example.calypso.common.applications;

import static org.eclipse.keyple.calypso.transaction.ApplicationType.GENERAL_USE;
import static org.eclipse.keyple.calypso.transaction.KeyDescriptor.Algorithm.TDES;
import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionAccessLevel.*;
import org.eclipse.keyple.calypso.transaction.AbstractPoApplicationSettings;
import org.eclipse.keyple.calypso.transaction.KeyDescriptor;

public class CalypsoRev31Ticketing extends AbstractPoApplicationSettings {
    public CalypsoRev31Ticketing() {

        setAid("315449432E49434131");

        setPoSerialNumberFilter(null);

        setKeyDescriptor(GENERAL_USE, SESSION_LVL_PERSO,
                new KeyDescriptor("RT1", (byte) 0x4C, (byte) 0x21, (byte) 0x79, TDES));

        setKeyDescriptor(GENERAL_USE, SESSION_LVL_LOAD,
                new KeyDescriptor("RT2", (byte) 0x42, (byte) 0x27, (byte) 0x79, TDES));

        setKeyDescriptor(GENERAL_USE, SESSION_LVL_DEBIT,
                new KeyDescriptor("RT3", (byte) 0x4D, (byte) 0x30, (byte) 0x79, TDES));
    }
}
