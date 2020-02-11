package org.eclipse.keyple.calypso.exception;

import org.eclipse.keyple.core.seproxy.exception.KeypleBaseException;

public class NoResourceAvailableException extends KeypleBaseException {


    public NoResourceAvailableException(String message) {
        super(message);
    }

    public NoResourceAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
