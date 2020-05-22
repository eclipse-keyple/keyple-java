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
import org.eclipse.keyple.core.seproxy.message.SeResponse;

/**
 * The exception {@code KeypleReaderIOException} indicates that some IO operations with the reader
 * or SE have failed, generally when the reader was disconnected or card removed.
 */
public class KeypleReaderIOException extends KeypleReaderException {

    /*
     * SeResponse and list of SeResponse objects to carry partial responses in case of a breakdown
     * in communication with the SE.
     */
    private SeResponse seResponse;
    private List<SeResponse> seResponses;

    /**
     * @param message the message to identify the exception context
     */
    public KeypleReaderIOException(String message) {
        super(message);
    }

    /**
     * Encapsulates a lower level reader exception
     *
     * @param message message to identify the exception context
     * @param cause the cause
     */
    public KeypleReaderIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public List<SeResponse> getSeResponses() {
        return seResponses;
    }

    public void setSeResponses(List<SeResponse> seResponses) {
        this.seResponses = seResponses;
    }

    public SeResponse getSeResponse() {
        return seResponse;
    }

    public void setSeResponse(SeResponse seResponse) {
        this.seResponse = seResponse;
    }
}
