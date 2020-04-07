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

import org.eclipse.keyple.calypso.transaction.PoTransaction;

/**
 * The exception {@code KeyplePoTransactionIllegalStateException} indicates an improper use of the
 * {@link PoTransaction} API.
 */
public class KeyplePoTransactionIllegalStateException extends KeyplePoTransactionException {

    /**
     * @param message the message to identify the exception context
     */
    protected KeyplePoTransactionIllegalStateException(String message) {
        super(message);
    }
}
