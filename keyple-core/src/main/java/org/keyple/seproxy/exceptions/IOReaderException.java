/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy.exceptions;

public class IOReaderException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 3679022642967524273L;

    /**
     * Instantiates a new IO reader exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public IOReaderException(String message, Throwable cause) {
        super(message, cause);
    }

}
