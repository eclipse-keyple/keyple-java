/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.exception;

import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.SeResponseSet;

/**
 * Base Exceptions thrown in a {@link org.eclipse.keyple.seproxy.ProxyReader} context
 */
public class KeypleReaderException extends KeypleBaseException {
    private final SeResponseSet seResponseSet;
    private final SeResponse seResponse;

    /**
     * New exception to be thrown
     * 
     * @param message : message to identify the exception and the context
     */
    public KeypleReaderException(String message) {
        super(message);
        this.seResponseSet = null;
        this.seResponse = null;
    }

    /**
     * Encapsulate a lower level reader exception
     * 
     * @param message: message to add some context to the exception
     * @param cause: lower level exception
     */
    public KeypleReaderException(String message, Throwable cause) {
        super(message, cause);
        this.seResponseSet = null;
        this.seResponse = null;
    }

    /**
     * Encapsulate a lower level reader exception but allow the current SeResponseSet to be provided
     * to the application.
     * 
     * @param message: message to add some context to the exception
     * @param cause: lower level exception
     * @param seResponseSet: current {@link SeResponseSet}
     */
    public KeypleReaderException(String message, Throwable cause, SeResponseSet seResponseSet) {
        super(message, cause);
        this.seResponseSet = seResponseSet;
        this.seResponse = null;
    }

    /**
     * Encapsulate a lower level reader exception but allow the current SeResponseSet to be provided
     * to the application.
     *
     * @param message: message to add some context to the exception
     * @param cause: lower level exception
     * @param seResponse: current {@link SeResponse}
     */
    public KeypleReaderException(String message, Throwable cause, SeResponse seResponse) {
        super(message, cause);
        this.seResponseSet = null;
        this.seResponse = seResponse;
    }

    public SeResponseSet getSeResponseSet() {
        return seResponseSet;
    }

    public SeResponse getSeResponse() {
        return seResponse;
    }
}
