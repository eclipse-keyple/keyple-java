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
package org.eclipse.keyple.calypso.transaction.exception;

import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;

/**
 * This exception is raised when information is requested but the operation that would have provided
 * the information has not been carried out. (for example when the status of the PIN is requested
 * but no verification request has been made)
 */
public class KeypleCalypsoUnkownStatusException extends KeypleBaseException {

    public KeypleCalypsoUnkownStatusException(String message) {
        super(message);
    }

    public KeypleCalypsoUnkownStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
