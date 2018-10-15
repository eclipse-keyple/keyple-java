/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.example.common.generic;

import org.eclipse.keyple.seproxy.SeProtocol;

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
