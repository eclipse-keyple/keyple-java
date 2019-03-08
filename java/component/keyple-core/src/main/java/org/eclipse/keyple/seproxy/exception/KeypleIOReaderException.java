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
package org.eclipse.keyple.seproxy.exception;

import org.eclipse.keyple.seproxy.message.ProxyReader;

/**
 * Exception thrown when IO operations failed in a {@link ProxyReader}
 */
public class KeypleIOReaderException extends KeypleReaderException {

    /**
     * New exception to be thrown
     *
     * @param message : message to identify the exception and the context
     */
    public KeypleIOReaderException(String message) {
        super(message);
    }

    /**
     * Encapsulate a lower level reader exception
     *
     * @param message : message to add some context to the exception
     * @param cause : lower level exception
     */
    public KeypleIOReaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
