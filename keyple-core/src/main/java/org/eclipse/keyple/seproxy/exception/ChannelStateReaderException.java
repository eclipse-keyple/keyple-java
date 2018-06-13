/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.exception;

/**
 * When the card channel cannot be opened or written to. It's just an other type of a
 * {@link IOReaderException}
 */
public class ChannelStateReaderException extends IOReaderException {
    public ChannelStateReaderException(Exception ex) {
        super(ex);
    }

    public ChannelStateReaderException(String message, Exception ex) {
        super(message, ex);
    }

    public ChannelStateReaderException(String message) {
        super(message);
    }
}
