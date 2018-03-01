/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy.exceptions;

import java.io.IOException;

/**
 * Any IO that occur around the {@link org.keyple.seproxy.ProxyReader}
 */
public class IOReaderException extends IOException {
    public IOReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public IOReaderException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public IOReaderException(String message) {
        super(message);
    }
}
