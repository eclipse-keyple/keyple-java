/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;


public enum ContactProtocols implements SeProtocol {

    // Contact standard
    PROTOCOL_ISO7816_3("ISO 7816-3");

    /** The protocol name. */
    private String name;

    ContactProtocols(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
