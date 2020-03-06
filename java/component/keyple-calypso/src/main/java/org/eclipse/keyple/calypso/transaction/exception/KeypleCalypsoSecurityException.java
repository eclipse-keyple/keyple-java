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
 * This exception is raised when a security problem is identified during a secure operation (for
 * example, a SAM error when calculating encrypted data or a signature).
 */
public class KeypleCalypsoSecurityException extends KeypleBaseException {

    public KeypleCalypsoSecurityException(String message) {
        super(message);
    }

    public KeypleCalypsoSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
