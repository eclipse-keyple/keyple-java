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
 * Parent Exception for all Keyple Checked Exception
 */
public class KeypleException extends Exception {

    /**
     * New exception to be thrown
     * 
     * @param message message to identify the exception and the context
     */
    public KeypleException(String message) {
        super(message);
    }

    /**
     * Encapsulate a lower level exception (ie CardException, IOException, HostNotFoundException..)
     * 
     * @param message message to identify the exception and the context
     * @param cause lower level exception
     */
    public KeypleException(String message, Throwable cause) {
        super(message, cause);
    }
}
