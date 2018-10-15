/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.seproxy.exception;



/**
 * Base Exception for all Keyple Checked Exception
 */
public class KeypleBaseException extends Exception {
    private static final long serialVersionUID = -500856379312027085L;

    /**
     * New exception to be thrown
     * 
     * @param message : message to identify the exception and the context
     */
    public KeypleBaseException(String message) {
        super(message);
    }

    /**
     * Encapsulate a lower level exception (ie CardException, IOException, HostNotFoundException..)
     * 
     * @param message : message to identify the exception and the context
     * @param cause : lower level exception
     */
    public KeypleBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
