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
