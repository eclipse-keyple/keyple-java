/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.keyple.calypso.command.csm;

import org.eclipse.keyple.command.AbstractIso7816CommandBuilder;
import org.eclipse.keyple.seproxy.ApduRequest;

/**
 * Superclass for all CSM command builders.
 * <p>
 * Used directly, this class can serve as low level command builder.
 */
public class CsmCommandBuilder extends AbstractIso7816CommandBuilder {

    protected org.eclipse.keyple.calypso.command.csm.CsmRevision defaultRevision = CsmRevision.S1D;// 94

    public CsmCommandBuilder(CalypsoSmCommands reference, ApduRequest request) {
        super(reference, request);
    }
}
