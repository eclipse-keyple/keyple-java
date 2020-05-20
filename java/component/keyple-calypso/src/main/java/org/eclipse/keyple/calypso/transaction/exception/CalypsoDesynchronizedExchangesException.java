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
 * The exception {@code CalypsoDesynchronizedExchangesException} indicates a desynchronization of
 * the APDU exchanges.<br>
 * This means that the number of APDU requests is different from the number of APDU responses.
 */
public class CalypsoDesynchronizedExchangesException extends CalypsoPoTransactionException {

    /**
     * @param message the message to identify the exception context
     */
    public CalypsoDesynchronizedExchangesException(String message) {
        super(message);
    }
}
