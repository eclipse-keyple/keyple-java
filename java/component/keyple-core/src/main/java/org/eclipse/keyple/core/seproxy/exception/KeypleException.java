/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.seproxy.exception;

/**
 * The exception <code>KeypleException</code> is the parent class of all Keyple checked exceptions.
 */
public class KeypleException extends Exception {

    /**
     * @param message the message to identify the exception context
     */
    public KeypleException(String message) {
        super(message);
    }

    /**
     * Encapsulate a lower level exception (ie CardException, IOException, HostNotFoundException..)
     *
     * @param message message to identify the exception context
     * @param cause the cause
     */
    public KeypleException(String message, Throwable cause) {
        super(message, cause);
    }
}
