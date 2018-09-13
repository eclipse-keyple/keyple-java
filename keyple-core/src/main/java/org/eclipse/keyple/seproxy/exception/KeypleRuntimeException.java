package org.eclipse.keyple.seproxy.exception;

public class KeypleRuntimeException extends RuntimeException{

    public KeypleRuntimeException(String message) {
        super(message);
    }

    public KeypleRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
