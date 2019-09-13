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


import java.util.List;
import org.eclipse.keyple.core.seproxy.message.ProxyReader;
import org.eclipse.keyple.core.seproxy.message.SeResponse;

/**
 * Base Exceptions thrown in a {@link ProxyReader} context
 */
public class KeypleReaderException extends KeypleBaseException {
    /*
     * SeResponseSet and SeResponse objects to carry partial responses in the event of a breakdown
     * in communication with the SE.
     */
    private List<SeResponse> seResponseList;
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
    public List<SeResponse> getSeResponseSet() {
        return seResponseList;
    }

    public void setSeResponseSet(List<SeResponse> seResponseList) {
        this.seResponseList = seResponseList;
    }

    public SeResponse getSeResponse() {
        return seResponse;
    }

    public void setSeResponse(SeResponse seResponse) {
        this.seResponse = seResponse;
    }
}
