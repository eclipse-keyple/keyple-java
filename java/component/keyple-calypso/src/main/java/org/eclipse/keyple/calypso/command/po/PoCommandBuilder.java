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
package org.eclipse.keyple.calypso.command.po;

import org.eclipse.keyple.command.AbstractIso7816CommandBuilder;
import org.eclipse.keyple.seproxy.message.ApduRequest;

/**
 * Superclass for all PO command builders.
 * <p>
 * Used directly, this class can serve as low level command builder.
 */
public class PoCommandBuilder extends AbstractIso7816CommandBuilder {

    /**
     * Constructor dedicated for the building of referenced Calypso commands
     * 
     * @param reference a command reference from the Calypso command table
     * @param request the ApduRequest (the instruction byte will be overwritten)
     */
    public PoCommandBuilder(CalypsoPoCommands reference, ApduRequest request) {
        super(reference, request);
    }
}
