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
 * This exception is raised when the current command is not supported by the current PO (for example
 * Verify PIN with a PO not having this feature).
 */
public class KeypleCalypsoNotSupportedException extends KeypleBaseException {

    public KeypleCalypsoNotSupportedException(String message) {
        super(message);
    }

    public KeypleCalypsoNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
