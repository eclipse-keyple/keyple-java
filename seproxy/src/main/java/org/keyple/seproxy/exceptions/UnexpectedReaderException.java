package org.keyple.seproxy.exceptions;

public class UnexpectedReaderException extends ReaderException {

    /**
     *
     */
    private static final long serialVersionUID = -2314614836951962548L;

   
    /**
     * Instantiates a new unExpected reader exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public UnexpectedReaderException(String message, Throwable cause) {
        super(message, cause);
    }

}
