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
 * The exception {@code KeyplePoBadExpectedLengthException} indicates that the expected output data
 * length is not correct.<br>
 * This exception is specially associated with the code 6CXXh.<br>
 * It is therefore possible to retrieve the value XX using the method {@code getExpectedLength()} if
 * {@code statusCode} is set.
 */
public class KeyplePoBadExpectedLengthException extends KeyplePoCommandException {

    /**
     * @param message the message to identify the exception context
     * @param command the Calypso PO command
     * @param statusCode the status code
     */
    public KeyplePoBadExpectedLengthException(String message, CalypsoPoCommand command,
            Integer statusCode) {
        super(message, command, statusCode);
    }

    /**
     * @return the expected length if {@code statusCode} is set, or null
     */
    public int getExpectedLength() {
        return getStatusCode() != null ? getStatusCode() & 0xFF : null;
    }
}
