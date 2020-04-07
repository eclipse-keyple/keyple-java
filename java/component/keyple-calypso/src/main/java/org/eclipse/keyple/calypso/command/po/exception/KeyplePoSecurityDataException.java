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

/**
 * The exception <code>KeyplePoSecurityDataException</code> indicates that the security input data
 * provided is not valid.<br>
 * This can occur, for example, during the closing of a secure session if the SAM's signature is
 * incorrect.
 */
public class KeyplePoSecurityDataException extends KeyplePoCommandException {

    /**
     * @param message the message to identify the exception context
     * @param command the Calypso PO command
     * @param statusCode the status code
     */
    public KeyplePoSecurityDataException(String message, CalypsoPoCommand command,
            Integer statusCode) {
        super(message, command, statusCode);
    }
}
