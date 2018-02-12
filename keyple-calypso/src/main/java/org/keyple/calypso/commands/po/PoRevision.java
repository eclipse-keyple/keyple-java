/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po;

/**
 * This enumeration registers the Calypso revisions of PO.
 *
 * @author Ixxi
 */
public enum PoRevision {

    REV2_4("Calypso Revision 2.4"), // cla 0x94

    REV3_1("Calypso Revision 3.1"), // cla 0x00

    REV3_2("Calypso Revision 3.2"); // cla 0x00

    private String name;

    private PoRevision(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
