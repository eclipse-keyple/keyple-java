/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.sam;

import org.eclipse.keyple.command.AbstractIso7816CommandBuilder;
import org.eclipse.keyple.seproxy.ApduRequest;

/**
 * Class to build custom (non-referenced) SAM commands
 */
public class SamCustomCommandBuilder extends AbstractIso7816CommandBuilder {

    protected SamRevision defaultRevision = SamRevision.S1D;// 94

    /**
     * Constructor dedicated to the construction of user-defined commands.
     *
     * Caveat: the caller has to provide all the command data.
     *
     * @param name the name of the command (will appear in the ApduRequest log)
     * @param request the ApduRequest (the correct instruction byte must be provided)
     */
    public SamCustomCommandBuilder(String name, ApduRequest request) {
        super("SAM Custom Command: " + name, request);
    }
}
