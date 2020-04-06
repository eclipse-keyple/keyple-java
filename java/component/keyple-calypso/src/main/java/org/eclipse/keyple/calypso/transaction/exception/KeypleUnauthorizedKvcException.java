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
package org.eclipse.keyple.calypso.transaction.exception;

/**
 * The exception <code>KeypleUnauthorizedKvcException</code> indicates that the current PO has an
 * unauthorized KVC.
 */
public class KeypleUnauthorizedKvcException extends KeyplePoTransactionException {

    /**
     * @param message the message to identify the exception context
     */
    public KeypleUnauthorizedKvcException(String message) {
        super(message);
    }
}
