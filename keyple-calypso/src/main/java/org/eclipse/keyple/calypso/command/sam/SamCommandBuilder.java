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
 * Superclass for all SAM command builders.
 * <p>
 * Used directly, this class can serve as low level command builder.
 */
public class SamCommandBuilder extends AbstractIso7816CommandBuilder {

    protected org.eclipse.keyple.calypso.command.sam.SamRevision defaultRevision = SamRevision.S1D;// 94

    public SamCommandBuilder(CalypsoSamCommands reference, ApduRequest request) {
        super(reference, request);
    }
}
