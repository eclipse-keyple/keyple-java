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
 * Base Exceptions thrown in a {@link org.eclipse.keyple.seproxy.ReaderPlugin} context
 */
public class KeyplePluginException extends KeypleBaseException {

    /**
     * New plugin exception to be thrown
     * 
     * @param message : message to identify the exception and the context
     */
    public KeyplePluginException(String message) {
        super(message);
    }

    /**
     * Encapsulate a lower level plugin exception
     * 
     * @param message : message to add some context to the exception
     * @param cause : lower level exception
     */
    public KeyplePluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
