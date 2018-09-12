/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po;

import org.eclipse.keyple.calypso.command.AbstractIso7816CommandBuilder;
import org.eclipse.keyple.seproxy.ApduRequest;

/**
 * Superclass for all PO command builders.
 * <p>
 * Used directly, this class can serve as low level command builder.
 */
public class PoCommandBuilder extends AbstractIso7816CommandBuilder {

    protected PoRevision defaultRevision = PoRevision.REV3_1;

    public PoCommandBuilder(CalypsoPoCommands reference, ApduRequest request) {
        super(reference, request);
    }
}
