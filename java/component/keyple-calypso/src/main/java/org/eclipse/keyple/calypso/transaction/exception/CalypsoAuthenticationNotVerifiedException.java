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

/**
 * The exception {@code CalypsoAuthenticationNotVerifiedException} indicates that the PO has
 * correctly closed the secure session, but that it is impossible to check the authenticity of the
 * support because the SAM is no more available (timeout, network problem,...).
 */
public class CalypsoAuthenticationNotVerifiedException extends CalypsoPoTransactionException {

    /**
     * @param message the message to identify the exception context
     */
    public CalypsoAuthenticationNotVerifiedException(String message) {
        super(message);
    }

    /**
     * Encapsulates lower level exception.
     *
     * @param message message to identify the exception context
     * @param cause the cause
     */
    public CalypsoAuthenticationNotVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
