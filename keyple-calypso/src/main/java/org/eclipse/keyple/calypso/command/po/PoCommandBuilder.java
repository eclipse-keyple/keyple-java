/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po;

import org.eclipse.keyple.command.AbstractIso7816CommandBuilder;
import org.eclipse.keyple.seproxy.ApduRequest;

/**
 * Superclass for all PO command builders.
 * <p>
 * Used directly, this class can serve as low level command builder.
 */
public class PoCommandBuilder extends AbstractIso7816CommandBuilder {

    protected PoRevision defaultRevision = PoRevision.REV3_1;

    /**
     * Constructor dedicated for the building of referenced Calypso commands
     * 
     * @param reference a command reference from the Calypso command table
     * @param request the ApduRequest (the instruction byte will be overwritten)
     */
    public PoCommandBuilder(CalypsoPoCommands reference, ApduRequest request) {
        super(reference, request);
    }

    /**
     * Constructor dedicated to user defined commands Caveat: the caller has to provide all the
     * command data.
     * 
     * @param name the name of the command (will appear in the ApduRequest log)
     * @param request the ApduRequest (the correct instruction byte must be provided)
     */
    public PoCommandBuilder(String name, ApduRequest request) {
        super("PO Custom Command: " + name, request);
    }
}
