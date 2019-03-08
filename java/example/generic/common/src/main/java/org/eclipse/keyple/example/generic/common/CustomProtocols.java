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
package org.eclipse.keyple.example.generic.common;

import org.eclipse.keyple.seproxy.protocol.SeProtocol;

/**
 * Custom protocol definitions to illustrate the extension of the Keyple SDK definitions
 */
public enum CustomProtocols implements SeProtocol {
    CUSTOM_PROTOCOL_B_PRIME("Custom Old Calypso B prime"),

    CUSTOM_PROTOCOL_MIFARE_DESFIRE("Custom Mifare DESFire");

    /** The protocol name. */
    private String name;

    CustomProtocols(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
