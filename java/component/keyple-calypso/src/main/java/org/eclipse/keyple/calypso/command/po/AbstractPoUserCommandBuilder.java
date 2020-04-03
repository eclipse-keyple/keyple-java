/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
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

import org.eclipse.keyple.core.seproxy.message.ApduRequest;

/**
 * Abstract class for all builders of "user" Calypso commands, i.e. all commands intended to access
 * PO files or data, especially during secure sessions.
 */
public abstract class AbstractPoUserCommandBuilder<AbstractPoResponseParser>
        extends AbstractPoCommandBuilder {
    /**
     * Constructor
     *
     * @param reference a command reference from the Calypso command table
     * @param request the ApduRequest
     */
    public AbstractPoUserCommandBuilder(CalypsoPoCommands reference, ApduRequest request) {
        super(reference, request);
    }

    public abstract int getSessionBufferSizeConsumed();
}
