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
package org.eclipse.keyple.calypso.command.po.exception;

import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.core.seproxy.exception.KeypleSeCommandException;

/**
 * The exception {@code KeyplePoCommandException} is the parent abstract class of all Keyple PO APDU
 * commands exceptions.
 */
public abstract class KeyplePoCommandException extends KeypleSeCommandException {

    /**
     * @param message the message to identify the exception context
     * @param command the Calypso PO command
     * @param statusCode the status code (optional)
     */
    protected KeyplePoCommandException(String message, CalypsoPoCommand command,
            Integer statusCode) {
        super(message, command, statusCode);
    }

}
