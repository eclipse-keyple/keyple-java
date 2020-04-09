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
package org.eclipse.keyple.calypso.command.sam.exception;

import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommand;
import org.eclipse.keyple.core.seproxy.exception.KeypleSeCommandException;

/**
 * The exception {@code CalypsoSamCommandException} is the parent abstract class of all Keyple SAM
 * APDU commands exceptions.
 */
public abstract class CalypsoSamCommandException extends KeypleSeCommandException {

    /**
     * @param message the message to identify the exception context
     * @param command the Calypso SAM command
     * @param statusCode the status code (optional)
     */
    protected CalypsoSamCommandException(String message, CalypsoSamCommand command,
            Integer statusCode) {
        super(message, command, statusCode);
    }

}
