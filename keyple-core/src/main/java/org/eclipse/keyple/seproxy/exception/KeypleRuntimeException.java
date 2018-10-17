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
package org.eclipse.keyple.seproxy.exception;

public class KeypleRuntimeException extends RuntimeException {

    public KeypleRuntimeException(String message) {
        super(message);
    }

    public KeypleRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
