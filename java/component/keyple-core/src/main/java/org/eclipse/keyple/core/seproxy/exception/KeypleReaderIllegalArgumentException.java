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
 * The exception <code>KeypleReaderIllegalArgumentException</code> indicates that a reader's method
 * is called by some illegal arguments.
 */
public class KeypleReaderIllegalArgumentException extends KeypleReaderException {

    /**
     * @param message the message to identify the exception context
     */
    public KeypleReaderIllegalArgumentException(String message) {
        super(message);
    }
}
