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
 * Class to build custom (non-referenced) modification PO commands
 */
public class PoCustomModificationCommandBuilder extends AbstractIso7816CommandBuilder
        implements PoModificationCommand {

    protected PoRevision defaultRevision = PoRevision.REV3_1;

    /**
     * Constructor dedicated to the construction of user-defined commands.
     *
     * Caveat:
     * <ul>
     * <li>the caller has to provide all the command data.</li>
     * <li>Using this method bypasses the security and functional verification mechanisms of the
     * PoTransaction API.
     * <p>
     * It is done at the user's risk.</li>
     * </ul>
     *
     * @param name the name of the command (will appear in the ApduRequest log)
     * @param request the ApduRequest (the correct instruction byte must be provided)
     */
    public PoCustomModificationCommandBuilder(String name, ApduRequest request) {
        super("PO Custom Modification Command: " + name, request);
    }
}
