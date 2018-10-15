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


import org.eclipse.keyple.seproxy.SeResponse;
import org.eclipse.keyple.seproxy.SeResponseSet;

/**
 * Base Exceptions thrown in a {@link org.eclipse.keyple.seproxy.ProxyReader} context
 */
public class KeypleReaderException extends KeypleBaseException {
    /*
     * SeResponseSet and SeResponse objects to carry partial responses in the event of a breakdown
     * in communication with the SE.
     */
    private SeResponseSet seResponseSet;
    private SeResponse seResponse;

    /**
     * New exception to be thrown
     *
     * @param message : message to identify the exception and the context
     */
    public KeypleReaderException(String message) {
        super(message);
    }

    /**
     * Encapsulate a lower level reader exception
     *
     * @param message : message to add some context to the exception
     * @param cause : lower level exception
     */
    public KeypleReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    /* Setters and Getters for SeResponseSet and SeResponse */
    public SeResponseSet getSeResponseSet() {
        return seResponseSet;
    }

    public void setSeResponseSet(SeResponseSet seResponseSet) {
        this.seResponseSet = seResponseSet;
    }

    public SeResponse getSeResponse() {
        return seResponse;
    }

    public void setSeResponse(SeResponse seResponse) {
        this.seResponse = seResponse;
    }
}
