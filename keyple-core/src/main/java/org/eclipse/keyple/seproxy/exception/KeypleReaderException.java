package org.eclipse.keyple.seproxy.exception;

/**
 *  Used when a generic checked exception occurs in reader
 */
public class KeypleReaderException extends KeypleBaseException {

    /**
     * New reader exception to be thrown
     * @param message : message to identify the exception and the context
     */
    public KeypleReaderException(String message) {
        super(message);
    }

    /**
     * Encapsulate a lower level reader exception
     * @param message : message to add some context to the exception
     * @param cause : lower level exception
     */
    public KeypleReaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
