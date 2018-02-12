/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.csm;

import org.keyple.calypso.commands.CalypsoCommands;
import org.keyple.commands.ApduCommandBuilder;
import org.keyple.seproxy.ApduRequest;

/**
 *
 * This abstract class extends ApduCommandBuilder, it has to be extended by all CSM command builder
 * classes, it manages the current default revision for PO commands
 *
 * @author IXXI
 *
 */
public abstract class CsmCommandBuilder extends ApduCommandBuilder {

    protected CsmRevision defaultRevision = CsmRevision.S1D;// 94

    public CsmCommandBuilder(CalypsoCommands reference, ApduRequest request) {
        super(reference, request);
    }
}
