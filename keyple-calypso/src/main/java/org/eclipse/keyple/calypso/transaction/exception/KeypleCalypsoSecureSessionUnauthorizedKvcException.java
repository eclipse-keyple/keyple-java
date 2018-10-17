/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.keyple.calypso.transaction.exception;

import org.eclipse.keyple.seproxy.exception.KeypleReaderException;

/**
 * Thrown when the current PO has an unauthorized KVC
 */
public class KeypleCalypsoSecureSessionUnauthorizedKvcException extends KeypleReaderException {
    public KeypleCalypsoSecureSessionUnauthorizedKvcException(String message) {
        super(message);
    }
}
